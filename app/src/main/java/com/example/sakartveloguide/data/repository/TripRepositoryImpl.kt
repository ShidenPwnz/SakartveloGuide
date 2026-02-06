package com.example.sakartveloguide.data.repository

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Keep
data class RawLocationDto(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lng") val lng: Double?,
    @SerializedName("image") val image: String?,
    @SerializedName("priority") val priority: Int?,
    @SerializedName("popularity") val popularity: Int?,
    @SerializedName("is_landmark") val is_landmark: Boolean?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("name") val name: String?,
    @SerializedName("name_en") val name_en: String?,
    @SerializedName("name_ka") val name_ka: String?,
    @SerializedName("name_ru") val name_ru: String?,
    @SerializedName("name_tr") val name_tr: String?,
    @SerializedName("name_hy") val name_hy: String?,
    @SerializedName("name_iw") val name_iw: String?,
    @SerializedName("name_ar") val name_ar: String?,
    @SerializedName("desc_en") val desc_en: String?,
    @SerializedName("desc_ka") val desc_ka: String?,
    @SerializedName("desc_ru") val desc_ru: String?,
    @SerializedName("desc_tr") val desc_tr: String?,
    @SerializedName("desc_hy") val desc_hy: String?,
    @SerializedName("desc_iw") val desc_iw: String?,
    @SerializedName("desc_ar") val desc_ar: String?
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
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) : TripRepository {

    private val KEY_DB_VERSION = intPreferencesKey("last_seeded_db_version")
    private val DATABASE_PAYLOAD_VERSION = 8 // Hard Reset for ID Alignment

    override fun getAvailableTrips(): Flow<List<TripPath>> = tripDao.getAllTrips().map { entities ->
        entities.map { mapEntityToDomain(it, false) }
    }

    override suspend fun getTripById(id: String): TripPath? {
        val entity = tripDao.getTripById(id) ?: return null
        return mapEntityToDomain(entity, true)
    }

    override suspend fun getTripCount(): Int = tripDao.getTripCount()

    private suspend fun mapEntityToDomain(entity: TripEntity, loadFullDetails: Boolean): TripPath {
        val itinerary = if (loadFullDetails) {
            val locations = locationDao.getLocationsByIds(entity.targetIds)
            entity.targetIds.mapNotNull { id -> locations.find { it.id == id } }
        } else emptyList()

        return TripPath(
            id = entity.id,
            title = LocalizedString(en = entity.titleEn, ka = entity.titleKa, ru = entity.titleRu, tr = entity.titleTr, hy = entity.titleHy, iw = entity.titleIw, ar = entity.titleAr),
            description = LocalizedString(en = entity.descEn, ka = entity.descKa, ru = entity.descRu, tr = entity.descTr, hy = entity.descHy, iw = entity.descIw, ar = entity.descAr),
            imageUrl = entity.imageUrl,
            category = try { RouteCategory.valueOf(entity.category) } catch (e: Exception) { RouteCategory.CULTURE },
            difficulty = try { Difficulty.valueOf(entity.difficulty) } catch (e: Exception) { Difficulty.NORMAL },
            totalRideTimeMinutes = entity.durationDays * 60,
            durationDays = entity.durationDays,
            itinerary = itinerary
        )
    }

    override suspend fun refreshTrips() {
        val currentVersion = dataStore.data.map { it[KEY_DB_VERSION] ?: 0 }.first()

        withContext(Dispatchers.IO) {
            try {
                val gson = Gson()
                val existingTrips = tripDao.getAllTrips().firstOrNull() ?: emptyList()
                val lockStateMap = existingTrips.associate { it.id to it.isLocked }

                // 1. ALWAYS INJECT META CARDS
                val metaSandbox = TripEntity(id = "meta_sandbox", imageUrl = "https://lp-cms-production.imgix.net/2023-02/shutterstockRF_629182610.jpg", category = "GUIDE", difficulty = "RELAXED", durationDays = 1, titleEn = "BUILD YOUR DREAM TRIP", titleKa = "ააწყე შენი ტური", titleRu = "СОЗДАЙ СВОЙ ТУР", titleTr = "", titleHy = "", titleIw = "", titleAr = "", descEn = "Fabricate a custom journey from 800+ verified locations.", descKa = "შექმენი შენი მოგზაურობა 800-ზე მეტი ადგილიდან.", descRu = "Создайте свой уникальный маршрут.", descTr = "", descHy = "", descIw = "", descAr = "", isLocked = false, targetIds = emptyList())
                val metaTutorial = TripEntity(id = "meta_tutorial", imageUrl = "https://images.pexels.com/photos/1252983/pexels-photo-1252983.jpeg", category = "GUIDE", difficulty = "EASY", durationDays = 1, titleEn = "WELCOME ABOARD", titleKa = "მოგესალმებით", titleRu = "ДОБРО ПОЖАЛОВАТЬ", titleTr = "", titleHy = "", titleIw = "", titleAr = "", descEn = "Take a tour of the Adventure Engine.", descKa = "ისწავლეთ აპლიკაცია 2 წუთში.", descRu = "Узнайте, как пользоваться.", descTr = "", descHy = "", descIw = "", descAr = "", isLocked = false, targetIds = emptyList())
                tripDao.insertTrips(listOf(metaSandbox, metaTutorial))

                if (currentVersion < DATABASE_PAYLOAD_VERSION) {
                    val locStream = context.assets.open("master_locations.json")
                    val rawLocs: List<RawLocationDto> = gson.fromJson(InputStreamReader(locStream), object : TypeToken<List<RawLocationDto>>() {}.type)
                    locationDao.insertLocations(rawLocs.map { dto ->
                        LocationEntity(
                            id = dto.id, type = dto.type ?: dto.category ?: "POI", region = dto.region ?: "Georgia",
                            latitude = dto.lat ?: 0.0, longitude = dto.lng ?: 0.0, imageUrl = dto.image ?: "",
                            priority = dto.priority ?: 1, popularity = dto.popularity ?: 0, isLandmark = dto.is_landmark ?: false, tags = dto.tags ?: emptyList(),
                            nameEn = dto.name_en ?: dto.name ?: "Place", nameKa = dto.name_ka ?: "", nameRu = dto.name_ru ?: "", nameTr = dto.name_tr ?: "", nameHy = dto.name_hy ?: "", nameIw = dto.name_iw ?: "", nameAr = dto.name_ar ?: "",
                            descEn = dto.desc_en ?: "", descKa = dto.desc_ka ?: "", descRu = dto.desc_ru ?: "", descTr = dto.desc_tr ?: "", descHy = dto.desc_hy ?: "", descIw = dto.desc_iw ?: "", descAr = dto.desc_ar ?: ""
                        )
                    })

                    val tripStream = context.assets.open("mission_templates.json")
                    val templates: List<TripTemplateDto> = gson.fromJson(InputStreamReader(tripStream), object : TypeToken<List<TripTemplateDto>>() {}.type)
                    tripDao.insertTrips(templates.map { t ->
                        TripEntity(id = t.id, imageUrl = t.image ?: "", category = t.category ?: "CULTURE", difficulty = t.difficulty ?: "NORMAL", durationDays = t.duration_days ?: 1, titleEn = t.title_en ?: "", titleKa = t.title_ka ?: "", titleRu = t.title_ru ?: "", titleTr = "", titleHy = "", titleIw = "", titleAr = "", descEn = t.description_en ?: "", descKa = t.description_ka ?: "", descRu = t.description_ru ?: "", descTr = "", descHy = "", descIw = "", descAr = "", isLocked = lockStateMap[t.id] ?: false, targetIds = t.sequence ?: emptyList())
                    })
                    dataStore.edit { it[KEY_DB_VERSION] = DATABASE_PAYLOAD_VERSION }
                }
            } catch (e: Exception) { Log.e("SAKARTVELO_REPO", "Sync Error", e) }
        }
    }

    override suspend fun lockTrip(tripId: String) = tripDao.updateLockStatus(tripId, true)
    override suspend fun nukeAllData() { withContext(Dispatchers.IO) { tripDao.nukeTable(); locationDao.nukeTable(); dataStore.edit { it[KEY_DB_VERSION] = 0 } } }
}