package com.example.sakartveloguide.presentation.battle

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.LogisticsProfileManager
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.data.local.dao.LocationDao
import com.example.sakartveloguide.data.manager.NavigationBridge
import com.example.sakartveloguide.domain.location.LocationManager
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import com.example.sakartveloguide.ui.manager.HapticManager
import com.example.sakartveloguide.ui.manager.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BattleViewModel @Inject constructor(
    private val repository: TripRepository,
    private val locationDao: LocationDao,
    private val preferenceManager: PreferenceManager,
    private val logisticsProfileManager: LogisticsProfileManager,
    private val locationManager: LocationManager,
    private val navigationBridge: NavigationBridge,
    private val hapticManager: HapticManager,
    private val soundManager: SoundManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = savedStateHandle.get<String>("tripId") ?: "custom_cargo"

    val userSession: Flow<UserSession> = preferenceManager.userSession
    val logisticsProfile = logisticsProfileManager.logisticsProfile.stateIn(viewModelScope, SharingStarted.Eagerly, LogisticsProfile())
    val userLocation = locationManager.locationFlow().filter { it.latitude != 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val missionState = preferenceManager.missionState.stateIn(viewModelScope, SharingStarted.Eagerly, MissionState())

    /**
     * THE BATTLE ENGINE
     * ARCHITECT'S FIX: Manual Sequence Sorting.
     * This ensures the order you set in the Briefing is the order you see in the Battle.
     */
    val currentTrip: StateFlow<TripPath?> = missionState.flatMapLatest { mState ->
        flow {
            val baseTrip = if (tripId == "custom_cargo") {
                val loadoutIds = preferenceManager.activeLoadout.first()
                val unorderedEntities = locationDao.getLocationsByIds(loadoutIds)

                // FORCE SORT: Match the specific order of loadoutIds
                val orderedEntities = loadoutIds.mapNotNull { id ->
                    unorderedEntities.find { it.id == id }
                }

                TripPath(
                    id = "custom_cargo", title = LocalizedString("CUSTOM MISSION"), description = LocalizedString("User defined."),
                    imageUrl = "https://images.pexels.com/photos/1036808/pexels-photo-1036808.jpeg",
                    category = RouteCategory.URBAN, difficulty = Difficulty.NORMAL, totalRideTimeMinutes = 0, durationDays = 1,
                    itinerary = orderedEntities.map { BattleNode(LocalizedString(it.name), LocalizedString(it.description), "D1", location = GeoPoint(it.latitude, it.longitude)) }
                )
            } else {
                repository.getTripById(tripId)
            }

            baseTrip?.let { trip ->
                val extendedItinerary = mutableListOf<BattleNode>()
                val fob = mState.fobLocation

                // 1. INJECT FOB START
                fob?.let {
                    extendedItinerary.add(BattleNode(LocalizedString("BASE CAMP (FOB)"), LocalizedString("Operation Start."), "START", location = it, imageUrl = "https://images.pexels.com/photos/271624/pexels-photo-271624.jpeg"))
                }

                extendedItinerary.addAll(trip.itinerary)

                // 2. INJECT EXTRACTION (Aware of meta-choice)
                if (mState.extractionType == ExtractionType.AIRPORT_EXTRACTION) {
                    extendedItinerary.add(BattleNode(LocalizedString("AIRPORT EXTRACTION"), LocalizedString("Returning to terminal."), "END", location = GeoPoint(41.6693, 44.9547), imageUrl = "https://upload.wikimedia.org/wikipedia/commons/7/75/Tbilisi_Airport_Terminal.jpg"))
                } else {
                    fob?.let {
                        extendedItinerary.add(BattleNode(LocalizedString("RETURN TO BASE"), LocalizedString("Mission complete."), "END", location = it, imageUrl = "https://images.pexels.com/photos/271624/pexels-photo-271624.jpeg"))
                    }
                }

                emit(trip.copy(itinerary = extendedItinerary))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * ARCHITECT'S FIX: Tactical Recon Initialization.
     * Determines where the map should open based on the current mission.
     */
    fun getInitialMapCenter(): GeoPoint {
        val trip = currentTrip.value
        // 1. Try the first location in the trip itinerary
        val firstNodeLoc = trip?.itinerary?.firstOrNull { it.location != null }?.location
        if (firstNodeLoc != null) return firstNodeLoc

        // 2. Fallback to Tbilisi Freedom Square
        return GeoPoint(41.6930, 44.8015)
    }

    fun determineAction(node: BattleNode, status: TargetStatus, distanceKm: Double, profile: LogisticsProfile): TacticalAction {
        if (status != TargetStatus.ENGAGED || node.location == null) return TacticalAction.Idle
        val start = userLocation.value ?: missionState.value.fobLocation ?: return TacticalAction.Idle
        return when {
            distanceKm < 0.2 -> TacticalAction.Execute("SECURE OBJECTIVE", Icons.Default.CheckCircle, 0xFF4CAF50)
            distanceKm < 1.5 -> TacticalAction.Execute("WALK TO TARGET", Icons.AutoMirrored.Filled.DirectionsWalk, 0xFFFFFFFF, navigationBridge.getMapsIntent(start, node.location, "walking"))
            profile.transportStrategy == TransportStrategy.DRIVER_RENTAL -> TacticalAction.Execute("DRIVE TO TARGET", Icons.Default.DirectionsCar, 0xFF2196F3, navigationBridge.getMapsIntent(start, node.location, "driving"))
            else -> TacticalAction.Execute("CALL BOLT", Icons.Default.LocalTaxi, 0xFF32BB78, getSafeBoltIntent(node.location))
        }
    }

    private fun getSafeBoltIntent(target: GeoPoint): Intent {
        val intent = navigationBridge.getBoltIntent(target)
        // We cannot easily check package manager here without context context leak risk or boilerplate.
        // Better to let the Activity launch it and catch the exception there,
        // OR return a web fallback intent if you prefer.
        // For now, let's keep it as is, but ensure the UI handles it.
        return intent
    }

    suspend fun getFreshLocation() = locationManager.getCurrentLocation()
    fun setFob(location: GeoPoint, onSuccess: () -> Unit) { viewModelScope.launch { if (location.latitude != 0.0) { preferenceManager.setFobLocation(location); hapticManager.missionCompleteSlam(); soundManager.playStampSlam(); onSuccess() } } }
    fun engageTarget(idx: Int) = viewModelScope.launch { preferenceManager.setActiveTarget(idx); hapticManager.tick() }
    fun neutralizeTarget(idx: Int) = viewModelScope.launch { preferenceManager.markTargetComplete(idx); soundManager.playTick() }
    fun abortMission() = viewModelScope.launch { preferenceManager.updateState(UserJourneyState.BROWSING, null); preferenceManager.clearCurrentMissionData() }
    fun calculateDistance(target: GeoPoint?): Double = navigationBridge.calculateDistanceKm(userLocation.value ?: missionState.value.fobLocation, target)
    fun getExfilIntent() = missionState.value.fobLocation?.let { navigationBridge.getExfilIntent(it) }
    fun getBoltIntent(target: GeoPoint): Intent = navigationBridge.getBoltIntent(target)
    fun getNavigationIntent(target: GeoPoint, mode: String): Intent = navigationBridge.getMapsIntent(userLocation.value ?: missionState.value.fobLocation!!, target, mode)
    fun getRentalUrl(): String = "https://localrent.com/en/georgia/"
}
