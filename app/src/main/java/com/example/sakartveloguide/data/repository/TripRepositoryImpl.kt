package com.example.sakartveloguide.data.repository

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import com.example.sakartveloguide.data.local.dao.LocationDao
import com.example.sakartveloguide.data.local.dao.TripDao
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Keep
data class RawLocationDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lng") val lng: Double?,
    @SerializedName("image") val image: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("name_ka") val name_ka: String?,
    @SerializedName("name_ru") val name_ru: String?,
    @SerializedName("desc_en") val desc_en: String?,
    @SerializedName("desc_ka") val desc_ka: String?,
    @SerializedName("desc_ru") val desc_ru: String?,
    @SerializedName("priority") val priority: Int?,
    @SerializedName("popularity") val popularity: Int?,
    @SerializedName("is_landmark") val is_landmark: Boolean?,
    @SerializedName("tags") val tags: List<String>?
)

@Keep
data class TripTemplateDto(
    @SerializedName("id") val id: String,
    @SerializedName("image") val image: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("duration_days") val duration_days: Int?,
    @SerializedName("sequence") val sequence: List<Int>?,
    @SerializedName("title_en") val title_en: String?,
    @SerializedName("title_ka") val title_ka: String?,
    @SerializedName("title_ru") val title_ru: String?,
    @SerializedName("description_en") val description_en: String?,
    @SerializedName("description_ka") val description_ka: String?,
    @SerializedName("description_ru") val description_ru: String?
)

@Singleton
class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val locationDao: LocationDao,
    @ApplicationContext private val context: Context
) : TripRepository {

    override fun getAvailableTrips(): Flow<List<TripPath>> = tripDao.getAllTrips().map { entities ->
        entities.map { mapEntityToDomain(it, loadFullDetails = false) }
    }

    override suspend fun getTripById(id: String): TripPath? {
        val entity = tripDao.getTripById(id) ?: return null
        return mapEntityToDomain(entity, loadFullDetails = true)
    }

    private suspend fun mapEntityToDomain(entity: TripEntity, loadFullDetails: Boolean): TripPath {
        val itinerary = if (loadFullDetails) {
            val locations = locationDao.getLocationsByIds(entity.targetIds)
            entity.targetIds.mapNotNull { id ->
                locations.find { it.id == id }?.let { loc ->
                    BattleNode(
                        title = LocalizedString(en = loc.nameEn, ka = loc.nameKa, ru = loc.nameRu),
                        description = LocalizedString(en = loc.descEn, ka = loc.descKa, ru = loc.descRu),
                        timeLabel = "POI", imageUrl = loc.imageUrl, location = GeoPoint(loc.latitude, loc.longitude)
                    )
                }
            }
        } else emptyList()

        return TripPath(
            id = entity.id,
            title = LocalizedString(en = entity.titleEn, ka = entity.titleKa, ru = entity.titleRu),
            description = LocalizedString(en = entity.descEn, ka = entity.descKa, ru = entity.descRu),
            imageUrl = entity.imageUrl,
            category = try { RouteCategory.valueOf(entity.category) } catch (e: Exception) { RouteCategory.CULTURE },
            difficulty = try { Difficulty.valueOf(entity.difficulty) } catch (e: Exception) { Difficulty.NORMAL },
            totalRideTimeMinutes = entity.durationDays * 60,
            durationDays = entity.durationDays,
            hasSnowWarning = entity.hasSnowWarning, isLocked = entity.isLocked, isPremium = entity.isPremium, itinerary = itinerary
        )
    }

    override suspend fun refreshTrips() {
        withContext(Dispatchers.IO) {
            try {
                if (tripDao.getTripCount() > 0) return@withContext

                val gson = Gson() // FIXED: Reference added

                // 1. INGEST LOCATIONS
                val locStream = context.assets.open("master_locations.json")
                val rawLocs: List<RawLocationDto?> = gson.fromJson(InputStreamReader(locStream), object : TypeToken<List<RawLocationDto?>>() {}.type)
                val locationEntities = rawLocs.mapNotNull { it }.map { dto ->
                    LocationEntity(
                        id = dto.id, type = dto.category ?: "POI", region = dto.region ?: "Georgia",
                        latitude = dto.lat ?: 0.0, longitude = dto.lng ?: 0.0, imageUrl = dto.image ?: "",
                        priority = dto.priority ?: 1, popularity = dto.popularity ?: 0,
                        isLandmark = dto.is_landmark ?: false, tags = dto.tags ?: emptyList(),
                        nameEn = dto.name ?: "Place", nameKa = dto.name_ka ?: "", nameRu = dto.name_ru ?: "",
                        descEn = dto.desc_en ?: "", descKa = dto.desc_ka ?: "", descRu = dto.desc_ru ?: ""
                    )
                }
                locationDao.insertLocations(locationEntities)

                // 2. INGEST TEMPLATES
                val tripStream = context.assets.open("mission_templates.json")
                val templates: List<TripTemplateDto?> = gson.fromJson(InputStreamReader(tripStream), object : TypeToken<List<TripTemplateDto?>>() {}.type)
                val tripEntities = templates.mapNotNull { it }.map { t ->
                    TripEntity(
                        id = t.id, imageUrl = t.image ?: "", category = t.category ?: "CULTURE",
                        difficulty = t.difficulty ?: "NORMAL", durationDays = t.duration_days ?: 1,
                        targetIds = t.sequence ?: emptyList(),
                        titleEn = t.title_en ?: "Trip", titleKa = t.title_ka ?: "", titleRu = t.title_ru ?: "",
                        descEn = t.description_en ?: "", descKa = t.description_ka ?: "", descRu = t.description_ru ?: ""
                    )
                }.toMutableList()

                // 3. INJECT SANDBOX
                tripEntities.add(0, TripEntity(
                    id = "meta_sandbox", imageUrl = "https://images.pexels.com/photos/32307/pexels-photo-32307.jpeg",
                    category = "GUIDE", difficulty = "RELAXED", durationDays = 1, targetIds = emptyList(),
                    titleEn = "BUILD YOUR DREAM TRIP", titleKa = "ააწყე შენი ტური", titleRu = "СОЗДАЙ СВОЙ ТУР",
                    descEn = "Fabricate a custom journey from 800+ locations."
                ))

                // 4. INJECT BOOTCAMP
                tripEntities.add(0, TripEntity(
                    id = "meta_tutorial", imageUrl = "https://images.pexels.com/photos/1252983/pexels-photo-1252983.jpeg",
                    category = "GUIDE", difficulty = "EASY", durationDays = 1, targetIds = emptyList(),
                    titleEn = "OPERATOR BOOTCAMP", titleKa = "ოპერატორის წვრთნა", titleRu = "КУРС ОПЕРАТОРА",
                    descEn = "Interactive training. Learn the Adventure Engine protocols in 2 minutes."
                ))

                tripDao.insertTrips(tripEntities)
            } catch (e: Exception) {
                Log.e("SAKARTVELO_REPO", "INGESTION ERROR", e)
            }
        }
    }

    override suspend fun lockTrip(tripId: String) = tripDao.updateLockStatus(tripId, true)
    override suspend fun nukeAllData() { tripDao.nukeTable(); locationDao.nukeTable() }
}