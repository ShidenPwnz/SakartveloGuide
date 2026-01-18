package com.example.sakartveloguide.presentation.battle

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.LogisticsProfileManager
import com.example.sakartveloguide.data.local.PreferenceManager
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
    private val preferenceManager: PreferenceManager,
    private val logisticsProfileManager: LogisticsProfileManager,
    private val locationManager: LocationManager,
    private val navigationBridge: NavigationBridge,
    private val hapticManager: HapticManager,
    private val soundManager: SoundManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = savedStateHandle.get<String>("tripId") ?: ""

    val userSession: Flow<UserSession> = preferenceManager.userSession

    val userLocation: StateFlow<GeoPoint?> = locationManager.locationFlow()
        .filter { it.latitude != 0.0 && it.longitude != 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val missionState: StateFlow<MissionState> = preferenceManager.missionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MissionState())

    val currentTrip: StateFlow<TripPath?> = flow {
        emit(repository.getTripById(tripId))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // --- ACTIONS ---

    suspend fun getFreshLocation(): GeoPoint? = locationManager.getCurrentLocation()

    fun setFob(location: GeoPoint) {
        viewModelScope.launch {
            if (location.latitude != 0.0) {
                preferenceManager.setFobLocation(location)
                hapticManager.missionCompleteSlam()
                soundManager.playStampSlam()
            }
        }
    }

    fun engageTarget(index: Int) {
        viewModelScope.launch {
            preferenceManager.setActiveTarget(index)
            hapticManager.tick()
        }
    }

    fun neutralizeTarget(index: Int) {
        viewModelScope.launch {
            preferenceManager.markTargetComplete(index)
            hapticManager.tick()
            soundManager.playTick()
        }
    }

    fun abortMission() {
        viewModelScope.launch {
            preferenceManager.updateState(UserJourneyState.BROWSING, null)
            hapticManager.missionCompleteSlam()
        }
    }

    fun calculateDistance(target: GeoPoint?): Double {
        val current = userLocation.value ?: missionState.value.fobLocation
        return navigationBridge.calculateDistanceKm(current, target)
    }

    fun getNavigationIntent(target: GeoPoint, mode: String): Intent {
        val start = userLocation.value ?: missionState.value.fobLocation ?: GeoPoint(41.71, 44.82)
        return navigationBridge.getMapsIntent(start, target, mode)
    }

    fun getExfilIntent(): Intent? {
        val fob = missionState.value.fobLocation ?: return null
        return navigationBridge.getExfilIntent(fob)
    }

    /**
     * RESOLVED: Now points to the fixed NavigationBridge method.
     */
    fun getBoltIntent(target: GeoPoint): Intent {
        return navigationBridge.getBoltIntent(target)
    }

    fun getRentalUrl(): String = "https://localrent.com/en/georgia/"
}