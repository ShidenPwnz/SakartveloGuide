package com.example.sakartveloguide.data.repository

import android.content.Context
import android.util.Log
import com.example.sakartveloguide.data.local.dao.LocationDao
import com.example.sakartveloguide.data.local.dao.TripDao
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

// DEFENSIVE DTOs
data class RawLocationDto(
    val id: Int, val name: String?, val region: String?, val lat: Double?, val lng: Double?, val image: String?, val category: String?,
    val desc_en: String?, val desc_ka: String?, val desc_ru: String?, val desc_tr: String?, val desc_hy: String?, val desc_iw: String?, val desc_ar: String?
)

data class TripTemplateDto(
    val id: String, val image: String?, val category: String?, val difficulty: String?, val duration_days: Int?, val sequence: List<Int>?,
    val title_en: String?, val title_ka: String?, val title_ru: String?, val title_tr: String?, val title_hy: String?, val title_iw: String?, val title_ar: String?,
    val description_en: String?, val description_ka: String?, val description_ru: String?, val description_tr: String?, val description_hy: String?, val description_iw: String?, val description_ar: String?
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
                // 1. Locations
                val locStream = context.assets.open("master_locations.json")
                val rawLocs: List<RawLocationDto> = gson.fromJson(InputStreamReader(locStream), object : TypeToken<List<RawLocationDto>>() {}.type)
                locationDao.insertLocations(rawLocs.map { dto ->
                    LocationEntity(
                        id = dto.id, type = dto.category ?: "POI", region = dto.region ?: "Georgia", latitude = dto.lat ?: 0.0, longitude = dto.lng ?: 0.0, imageUrl = dto.image ?: "",
                        nameEn = dto.name ?: "Place", nameKa = dto.name ?: "", nameRu = dto.name ?: "", nameTr = dto.name ?: "", nameHy = dto.name ?: "", nameIw = dto.name ?: "", nameAr = dto.name ?: "",
                        descEn = dto.desc_en ?: "", descKa = dto.desc_ka ?: "", descRu = dto.desc_ru ?: "", descTr = dto.desc_tr ?: "", descHy = dto.desc_hy ?: "", descIw = dto.desc_iw ?: "", descAr = dto.desc_ar ?: ""
                    )
                })

                // 2. Templates
                val tripStream = context.assets.open("mission_templates.json")
                val templates: List<TripTemplateDto> = gson.fromJson(InputStreamReader(tripStream), object : TypeToken<List<TripTemplateDto>>() {}.type)
                val entities = templates.map { t ->
                    TripEntity(
                        id = t.id, imageUrl = t.image ?: "", category = t.category ?: "CULTURE", difficulty = t.difficulty ?: "NORMAL", durationDays = t.duration_days ?: 1, targetIds = t.sequence ?: emptyList(),
                        titleEn = t.title_en ?: "Trip", titleKa = t.title_ka ?: "", titleRu = t.title_ru ?: "", titleTr = t.title_tr ?: "", titleHy = t.title_hy ?: "", titleIw = t.title_iw ?: "", titleAr = t.title_ar ?: "",
                        descEn = t.description_en ?: "", descKa = t.description_ka ?: "", descRu = t.description_ru ?: "", descTr = t.description_tr ?: "", descHy = t.description_hy ?: "", descIw = t.description_iw ?: "", descAr = t.description_ar ?: ""
                    )
                }.toMutableList()

                // 3. Sandbox
                entities.add(0, TripEntity(
                    id = "meta_sandbox", imageUrl = "https://images.pexels.com/photos/32307/pexels-photo.jpg", category = "GUIDE", difficulty = "RELAXED", durationDays = 1, targetIds = emptyList(),
                    titleEn = "BUILD YOUR DREAM TRIP", titleKa = "ააწყე შენი ტური", titleRu = "СОЗДАЙ СВОЙ ТУР", titleTr = "HAYALİNDEKİ TURU YAP", titleHy = "ՍՏԵՂԾԻՐ ՔՈ ՏՈՒՐԸ", titleIw = "בנה את הטיול שלך", titleAr = "اصنع رحلتك",
                    descEn = "Fabricate a custom journey from 800+ locations.", descKa = "შექმენით ინდივიდუალური მარშრუტი 800+ ადგილიდან.", descRu = "Создайте свой маршрут из 800+ мест.", descTr = "800+ noktadan özel rota oluştur.", descHy = "Ստեղწեք անհატական երთუღი 800+ վայրերից:", descIw = "צור מסלול מותאם אישית מ-800+ מיקומים.", descAr = "اصنع مسارًا مخصصًا من أكثر من 800 موقع."
                ))
                tripDao.insertTrips(entities)
            } catch (e: Exception) { Log.e("REPO", "Data error", e) }
        }
    }

    override suspend fun lockTrip(tripId: String) = tripDao.updateLockStatus(tripId, true)
    override suspend fun nukeAllData() { tripDao.nukeTable(); locationDao.nukeTable() }
}