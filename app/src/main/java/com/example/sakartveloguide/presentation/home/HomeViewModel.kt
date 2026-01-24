package com.example.sakartveloguide.presentation.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.domain.location.LocationManager
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.AuthRepository
import com.example.sakartveloguide.domain.repository.TripRepository
import com.example.sakartveloguide.domain.usecase.AddPassportStampUseCase
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
    private val authRepository: AuthRepository,
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
    val currentUser = authRepository.currentUser

    private val _stampingTrip = MutableStateFlow<TripPath?>(null)
    val stampingTrip: StateFlow<TripPath?> = _stampingTrip.asStateFlow()

    init {
        initAdventureEngine()
    }

    private fun initAdventureEngine() {
        // 1. Trigger Data Refresh (Non-Blocking)
        viewModelScope.launch {
            try {
                repository.refreshTrips()
            } catch (e: Exception) {
                Log.e("SAKARTVELO_INIT", "Data Refresh Failed", e)
            }
        }

        // 2. Observe Session for Navigation (Non-Blocking)
        viewModelScope.launch {
            preferenceManager.userSession
                .catch { emit(UserSession()) } // Safety fallback
                .collect { session ->
                    if (session.state == UserJourneyState.ON_THE_ROAD && session.activePathId != null) {
                        _initialDestination.value = "briefing/${session.activePathId}?ids="
                    } else {
                        _initialDestination.value = "home"
                    }
                }
        }

        // 3. Observe Trips for UI (Non-Blocking)
        viewModelScope.launch {
            repository.getAvailableTrips()
                .catch { Log.e("SAKARTVELO_UI", "Flow Error", it) }
                .collectLatest { trips ->
                    if (trips.isNotEmpty()) {
                        val grouped = trips.groupBy { Category(it.category.name) }.toMutableMap()
                        val sortedMap = grouped.keys
                            .sortedByDescending { it.name == "GUIDE" }
                            .associateWith { grouped[it]!! }

                        _uiState.update { it.copy(groupedPaths = sortedMap, isLoading = false) }
                        _isSplashReady.value = true
                    } else {
                        // Keep loading or show empty state, but don't crash
                        _uiState.update { it.copy(isLoading = false) } // Stop spinner even if empty
                        _isSplashReady.value = true
                    }
                }
        }
    }

    fun signIn(context: Context) {
        viewModelScope.launch {
            authRepository.signIn(context)
        }
    }

    fun onGuestSignIn() {
        viewModelScope.launch {
            authRepository.continueAsGuest()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun onCompleteTrip(trip: TripPath) {
        viewModelScope.launch {
            _stampingTrip.value = trip
            hapticManager.missionCompleteSlam()
            soundManager.playStampSlam()
            addPassportStampUseCase(trip)
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

    fun onLanguageChange(code: String) {
        viewModelScope.launch { preferenceManager.updateLanguage(code) }
    }

    fun onHideTutorialPermanent() {
        viewModelScope.launch { preferenceManager.setHasSeenTutorial(true) }
    }

    fun onSlamAnimationFinished() {
        viewModelScope.launch {
            _stampingTrip.value = null
            _navigationEvent.emit("home")
        }
    }
}