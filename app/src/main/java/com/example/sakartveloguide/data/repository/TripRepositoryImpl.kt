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

// Helper DTO
data class TripTemplateDto(
    val id: String, val title: String, val description: String,
    val category: String, val difficulty: String, val durationDays: Int,
    val image: String, val targets: List<Int>
)

@Singleton
class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val locationDao: LocationDao,
    @ApplicationContext private val context: Context
) : TripRepository {

    // HOME SCREEN: Returns "Light" objects (No itinerary) for speed
    override fun getAvailableTrips(): Flow<List<TripPath>> = tripDao.getAllTrips().map { entities ->
        entities.map { mapEntityToDomain(it, loadFullDetails = false) }
    }

    // DETAIL SCREEN: Returns "Heavy" object (With resolved JSON itinerary)
    override suspend fun getTripById(id: String): TripPath? {
        val entity = tripDao.getTripById(id) ?: return null
        return mapEntityToDomain(entity, loadFullDetails = true)
    }

    // --- MAPPING LOGIC ---
    private suspend fun mapEntityToDomain(entity: TripEntity, loadFullDetails: Boolean): TripPath {
        val itinerary = if (loadFullDetails) {
            // 1. Get the real location data
            val locations = locationDao.getLocationsByIds(entity.targetIds)

            // 2. Map IDs back to Objects in the correct sequence
            entity.targetIds.mapNotNull { id ->
                locations.find { it.id == id }?.let { loc ->
                    BattleNode(
                        title = LocalizedString(en = loc.name),
                        description = LocalizedString(en = loc.description),
                        timeLabel = "TGT",
                        imageUrl = loc.imageUrl,
                        location = GeoPoint(loc.latitude, loc.longitude)
                    )
                }
            }
        } else {
            emptyList()
        }

        return TripPath(
            id = entity.id,
            title = LocalizedString(en = entity.title), // Simplified for now
            description = LocalizedString(en = entity.description),
            imageUrl = entity.imageUrl,
            category = try { RouteCategory.valueOf(entity.category) } catch(e:Exception) { RouteCategory.CULTURE },
            difficulty = try { Difficulty.valueOf(entity.difficulty) } catch(e:Exception) { Difficulty.NORMAL },
            totalRideTimeMinutes = entity.durationDays * 60,
            durationDays = entity.durationDays,
            hasSnowWarning = entity.hasSnowWarning,
            isLocked = entity.isLocked,
            isPremium = entity.isPremium,
            itinerary = itinerary
        )
    }

    override suspend fun refreshTrips() {
        withContext(Dispatchers.IO) {
            try {
                // 1. INJECT MASTER INTEL (Locations)
                val locStream = context.assets.open("master_locations.json")
                val locListType = object : TypeToken<List<LocationEntity>>() {}.type
                val locations: List<LocationEntity> = Gson().fromJson(InputStreamReader(locStream), locListType)
                locationDao.insertLocations(locations)

                // 2. INJECT TEMPLATES (JSON Trips)
                val tripStream = context.assets.open("mission_templates.json")
                val tripListType = object : TypeToken<List<TripTemplateDto>>() {}.type
                val templates: List<TripTemplateDto> = Gson().fromJson(InputStreamReader(tripStream), tripListType)

                val entities = templates.map { t ->
                    TripEntity(
                        id = t.id,
                        title = t.title,
                        description = t.description,
                        imageUrl = t.image,
                        category = t.category,
                        difficulty = t.difficulty,
                        durationDays = t.durationDays,
                        targetIds = t.targets
                    )
                }.toMutableList()

                // 3. RESTORE THE "BUILD YOUR DREAM TRIP" CARD (Programmatic Injection)
                val sandboxCard = TripEntity(
                    id = "meta_sandbox",
                    title = "BUILD YOUR DREAM TRIP",
                    description = "Fabricate a custom mission from 800+ locations.",
                    imageUrl = "https://images.pexels.com/photos/32307/pexels-photo.jpg",
                    category = "GUIDE", // Keeps it at the top
                    difficulty = "RELAXED",
                    durationDays = 1,
                    targetIds = emptyList(), // No fixed targets
                    isLocked = false
                )
                // Add to the front of the list
                entities.add(0, sandboxCard)

                tripDao.insertTrips(entities)
                Log.d("SAKARTVELO_REPO", "Refreshed ${entities.size} trips (JSON + Sandbox).")

            } catch (e: Exception) {
                Log.e("SAKARTVELO_REPO", "Refresh Failed: ${e.message}")
            }
        }
    }

    override suspend fun lockTrip(tripId: String) = tripDao.updateLockStatus(tripId, true)
    override suspend fun nukeAllData() { tripDao.nukeTable(); locationDao.nukeTable() }
}