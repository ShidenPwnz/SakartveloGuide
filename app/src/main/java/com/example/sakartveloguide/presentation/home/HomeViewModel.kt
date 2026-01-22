package com.example.sakartveloguide.presentation.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.domain.location.LocationManager
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import com.example.sakartveloguide.domain.usecase.AddPassportStampUseCase
import com.example.sakartveloguide.domain.util.TacticalMath
import com.example.sakartveloguide.ui.manager.HapticManager
import com.example.sakartveloguide.ui.manager.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Category(val name: String)

data class HomeUiState(
    val groupedPaths: Map<Category, List<TripPath>> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TripRepository,
    private val addPassportStampUseCase: AddPassportStampUseCase,
    private val hapticManager: HapticManager,
    private val soundManager: SoundManager,
    private val preferenceManager: PreferenceManager,
    private val locationManager: LocationManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isSplashReady = MutableStateFlow(false)
    val isSplashReady: StateFlow<Boolean> = _isSplashReady.asStateFlow()

    private val _initialDestination = MutableStateFlow<String?>(null)
    val initialDestination: StateFlow<String?> = _initialDestination.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    val userSession: Flow<UserSession> = preferenceManager.userSession

    private val _stampingTrip = MutableStateFlow<TripPath?>(null)
    val stampingTrip: StateFlow<TripPath?> = _stampingTrip.asStateFlow()

    init { initAdventureEngine() }

    private fun initAdventureEngine() {
        viewModelScope.launch {
            try {
                repository.refreshTrips()

                val session = preferenceManager.userSession.first()
                if (session.state == UserJourneyState.ON_THE_ROAD && session.activePathId != null) {
                    _initialDestination.value = "briefing/${session.activePathId}"
                } else {
                    _initialDestination.value = "home"
                }

                repository.getAvailableTrips().collectLatest { trips ->
                    val grouped = trips.groupBy { Category(it.category.name) }.toMutableMap()
                    val sortedMap = grouped.keys
                        .sortedByDescending { it.name == "GUIDE" }
                        .associateWith { grouped[it]!! }

                    _uiState.update { it.copy(groupedPaths = sortedMap, isLoading = false) }
                    _isSplashReady.value = true
                }
            } catch (e: Exception) {
                Log.e("SAKARTVELO_INIT", "Startup Error", e)
                _initialDestination.value = "home"
                _isSplashReady.value = true
            }
        }
    }

    /**
     * ARCHITECT'S RESTORATION: The Journey Completion Sequence.
     * Checks if the user is at the final destination and awards a stamp.
     */
    fun onCompleteTrip(trip: TripPath) {
        viewModelScope.launch {
            // Trigger visual "Slam" animation
            _stampingTrip.value = trip
            hapticManager.missionCompleteSlam()
            soundManager.playStampSlam()

            // Persist the stamp in the database
            addPassportStampUseCase(trip)

            // Journey logic is reset to browsing
            preferenceManager.updateState(UserJourneyState.BROWSING, null)
            preferenceManager.clearCurrentMissionData()
        }
    }

    fun prepareForNewMission() {
        viewModelScope.launch {
            preferenceManager.clearCurrentMissionData()
        }
    }

    fun wipeAllUserData() {
        viewModelScope.launch {
            repository.nukeAllData()
            preferenceManager.clearCurrentMissionData()
            preferenceManager.updateState(UserJourneyState.BROWSING, null)
            repository.refreshTrips()
            _navigationEvent.emit("home")
        }
    }

    fun triggerHapticTick() { hapticManager.tick() }
    fun onLanguageChange(code: String) = viewModelScope.launch { preferenceManager.updateLanguage(code) }
    fun onHideTutorialPermanent() { viewModelScope.launch { preferenceManager.setHasSeenTutorial(true) } }
    fun onSlamAnimationFinished() {
        viewModelScope.launch {
            _stampingTrip.value = null
            _navigationEvent.emit("home")
        }
    }
}