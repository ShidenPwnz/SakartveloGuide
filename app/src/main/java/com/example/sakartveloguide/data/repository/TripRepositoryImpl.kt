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
    @SerializedName("title_tr") val title_tr: String?,
    @SerializedName("title_hy") val title_hy: String?,
    @SerializedName("title_iw") val title_iw: String?,
    @SerializedName("title_ar") val title_ar: String?,
    @SerializedName("description_en") val description_en: String?,
    @SerializedName("description_ka") val description_ka: String?,
    @SerializedName("description_ru") val description_ru: String?,
    @SerializedName("description_tr") val description_tr: String?,
    @SerializedName("description_hy") val description_hy: String?,
    @SerializedName("description_iw") val description_iw: String?,
    @SerializedName("description_ar") val description_ar: String?
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
                        title = LocalizedString(en = loc.nameEn, ka = loc.nameKa, ru = loc.nameRu, tr = loc.nameTr, hy = loc.nameHy, iw = loc.nameIw, ar = loc.nameAr),
                        description = LocalizedString(en = loc.descEn, ka = loc.descKa, ru = loc.descRu, tr = loc.descTr, hy = loc.descHy, iw = loc.descIw, ar = loc.descAr),
                        timeLabel = "POI", imageUrl = loc.imageUrl, location = GeoPoint(loc.latitude, loc.longitude)
                    )
                }
            }
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
            hasSnowWarning = entity.hasSnowWarning, isLocked = entity.isLocked, isPremium = entity.isPremium, itinerary = itinerary
        )
    }

    override suspend fun refreshTrips() {
        withContext(Dispatchers.IO) {
            try {
                val gson = Gson()
                val locStream = context.assets.open("master_locations.json")
                val rawLocs: List<RawLocationDto> = gson.fromJson(InputStreamReader(locStream), object : TypeToken<List<RawLocationDto>>() {}.type)
                if (rawLocs.isNotEmpty()) {
                    locationDao.insertLocations(rawLocs.map { dto ->
                        LocationEntity(
                            id = dto.id, type = dto.category ?: "POI", region = dto.region ?: "Georgia", latitude = dto.lat ?: 0.0, longitude = dto.lng ?: 0.0, imageUrl = dto.image ?: "",
                            nameEn = dto.name ?: "Place", nameKa = dto.name ?: "", nameRu = dto.name ?: "", nameTr = dto.name ?: "", nameHy = dto.name ?: "", nameIw = dto.name ?: "", nameAr = dto.name ?: "",
                            descEn = dto.desc_en ?: "", descKa = dto.desc_ka ?: "", descRu = dto.desc_ru ?: "", descTr = dto.desc_tr ?: "", descHy = dto.desc_hy ?: "", descIw = dto.desc_iw ?: "", descAr = dto.desc_ar ?: ""
                        )
                    })
                }

                val tripStream = context.assets.open("mission_templates.json")
                val templates: List<TripTemplateDto> = gson.fromJson(InputStreamReader(tripStream), object : TypeToken<List<TripTemplateDto>>() {}.type)
                val entities = templates.mapNotNull { t ->
                    if (t.id.isNotEmpty()) {
                        TripEntity(
                            id = t.id, imageUrl = t.image ?: "", category = t.category ?: "CULTURE", difficulty = t.difficulty ?: "NORMAL", durationDays = t.duration_days ?: 1, targetIds = t.sequence ?: emptyList(),
                            titleEn = t.title_en ?: "Trip", titleKa = t.title_ka ?: "", titleRu = t.title_ru ?: "", titleTr = t.title_tr ?: "", titleHy = t.title_hy ?: "", titleIw = t.title_iw ?: "", titleAr = t.title_ar ?: "",
                            descEn = t.description_en ?: "", descKa = t.description_ka ?: "", descRu = t.description_ru ?: "", descTr = t.description_tr ?: "", descHy = t.description_hy ?: "", descIw = t.description_iw ?: "", descAr = t.description_ar ?: ""
                        )
                    } else null
                }.toMutableList()

                entities.add(0, TripEntity(
                    id = "meta_sandbox", imageUrl = "https://images.pexels.com/photos/32307/pexels-photo.jpg", category = "GUIDE", difficulty = "RELAXED", durationDays = 1, targetIds = emptyList(),
                    titleEn = "BUILD YOUR DREAM TRIP", titleKa = "ააწყე შენი ტური", titleRu = "СОЗДАЙ СВОЙ ТУР", titleTr = "HAYALİNDEKİ TURU YAP", titleHy = "ՍՏԵՂԾԻՐ ՔՈ ՏՈՒՐԸ", titleIw = "בנה את הטיול שלך", titleAr = "اصنع رحلتك",
                    descEn = "Fabricate a custom journey from 800+ locations.", descKa = "შექმენით ინდივიდუალური მარშრუტი 800+ ადგილიდან.", descRu = "Создайте свой маршрут из 800+ мест.", descTr = "800+ noktadan özel rota oluştur.", descHy = "Ստեղწեք անհատական երთუღი 800+ վայրերից:", descIw = "צור מסלול מותאם אישית מ-800+ מיקומים.", descAr = "اصنع مسارًا مخصصًا من أكثر من 800 موقع."
                ))
                tripDao.insertTrips(entities)
            } catch (e: Exception) { Log.e("REPO", "Data error", e) }
        }
    }

    override suspend fun lockTrip(tripId: String) = tripDao.updateLockStatus(tripId, true)
    override suspend fun nukeAllData() { tripDao.nukeTable(); locationDao.nukeTable() }
}