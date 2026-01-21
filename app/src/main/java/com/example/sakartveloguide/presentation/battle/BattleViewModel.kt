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

    // ... inside BattleViewModel class ...
    val currentTrip: StateFlow<TripPath?> = missionState.flatMapLatest { mState ->
        flow {
            val baseTrip = if (tripId == "custom_cargo") {
                val loadoutIds = preferenceManager.activeLoadout.first()
                val entities = locationDao.getLocationsByIds(loadoutIds)
                TripPath(
                    id = "custom_cargo", title = LocalizedString("CUSTOM MISSION"), description = LocalizedString("Personal itinerary."),
                    imageUrl = "", category = RouteCategory.URBAN, difficulty = Difficulty.NORMAL, totalRideTimeMinutes = 0, durationDays = 1,
                    itinerary = entities.map { BattleNode(LocalizedString(it.name), LocalizedString(it.description), "D1", location = GeoPoint(it.latitude, it.longitude)) }
                )
            } else {
                repository.getTripById(tripId)
            }

            baseTrip?.let { trip ->
                val extended = mutableListOf<BattleNode>()

                // PREPEND HOME
                mState.fobLocation?.let { extended.add(BattleNode(LocalizedString("BASE CAMP (FOB)"), LocalizedString("Mission start point."), "ST", location = it)) }

                extended.addAll(trip.itinerary)

                // APPEND EXTRACTION
                mState.fobLocation?.let { extended.add(BattleNode(LocalizedString("MISSION EXTRACTION"), LocalizedString("Sorties complete. Returning."), "EN", location = it)) }

                emit(trip.copy(itinerary = extended))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun onCompleteTrip(trip: TripPath) {
        viewModelScope.launch {
            preferenceManager.updateState(UserJourneyState.BROWSING, null)
            preferenceManager.clearCurrentMissionData()
        }
    }
// ...

    fun determineAction(node: BattleNode, status: TargetStatus, distanceKm: Double, profile: LogisticsProfile): TacticalAction {
        if (status != TargetStatus.ENGAGED || node.location == null) return TacticalAction.Idle
        val start = userLocation.value ?: missionState.value.fobLocation ?: return TacticalAction.Idle

        return when {
            distanceKm < 0.2 -> TacticalAction.Execute("SECURE OBJECTIVE", Icons.Default.CheckCircle, 0xFF4CAF50)
            distanceKm < 1.5 -> TacticalAction.Execute("WALK TO TARGET", Icons.AutoMirrored.Filled.DirectionsWalk, 0xFFFFFFFF, navigationBridge.getMapsIntent(start, node.location, "walking"))
            profile.transportStrategy == TransportStrategy.DRIVER_RENTAL -> TacticalAction.Execute("DRIVE TO TARGET", Icons.Default.DirectionsCar, 0xFF2196F3, navigationBridge.getMapsIntent(start, node.location, "driving"))
            else -> TacticalAction.Execute("CALL BOLT", Icons.Default.LocalTaxi, 0xFF32BB78, navigationBridge.getBoltIntent(node.location))
        }
    }

    suspend fun getFreshLocation() = locationManager.getCurrentLocation()

    // ARCHITECT'S FIX: The Critical Wait-State
    fun setFob(location: GeoPoint, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (location.latitude != 0.0) {
                // Suspends until disk write is confirmed
                preferenceManager.setFobLocation(location)

                hapticManager.missionCompleteSlam()
                soundManager.playStampSlam()

                // Only NOW do we tell the UI to leave
                onSuccess()
            }
        }
    }

    fun engageTarget(idx: Int) = viewModelScope.launch { preferenceManager.setActiveTarget(idx); hapticManager.tick() }
    fun neutralizeTarget(idx: Int) = viewModelScope.launch { preferenceManager.markTargetComplete(idx); soundManager.playTick() }
    // ... inside BattleViewModel class ...

    fun abortMission() {
        viewModelScope.launch {
            // 1. Reset State
            preferenceManager.updateState(UserJourneyState.BROWSING, null)

            // 2. Wipe Tactical Data
            preferenceManager.clearCurrentMissionData()

            hapticManager.missionCompleteSlam()
        }
    }

    // ...
    fun calculateDistance(target: GeoPoint?): Double = navigationBridge.calculateDistanceKm(userLocation.value ?: missionState.value.fobLocation, target)
    fun getExfilIntent() = missionState.value.fobLocation?.let { navigationBridge.getExfilIntent(it) }
    fun getBoltIntent(target: GeoPoint): Intent = navigationBridge.getBoltIntent(target)
    fun getNavigationIntent(target: GeoPoint, mode: String): Intent = navigationBridge.getMapsIntent(userLocation.value ?: missionState.value.fobLocation!!, target, mode)
    fun getRentalUrl(): String = "https://localrent.com/en/georgia/"

    /**
     * ARCHITECT'S FIX: Provide a "Best Guess" starting point for the map.
     * If it's a premade trip, we center on the first location of that trip.
     */
    fun getInitialMapCenter(): GeoPoint {
        val trip = currentTrip.value
        return trip?.itinerary?.firstOrNull { it.location != null }?.location 
            ?: GeoPoint(41.6930, 44.8015) // Default Tbilisi center
    }
}