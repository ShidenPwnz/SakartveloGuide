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

    // Internal state to track if the background refresh task is done
    private val isEngineReady = MutableStateFlow(false)

    init {
        initAdventureEngine()
    }

    private fun initAdventureEngine() {
        // 1. SPLASH & NAV LOGIC
        viewModelScope.launch {
            preferenceManager.userSession
                .catch { emit(UserSession()) }
                .collect { session ->
                    if (session.state == UserJourneyState.ON_THE_ROAD && session.activePathId != null) {
                        _initialDestination.value = "briefing/${session.activePathId}?ids="
                    } else {
                        _initialDestination.value = "home"
                    }
                    _isSplashReady.value = true
                }
        }

        // 2. BACKGROUND SYNC
        viewModelScope.launch {
            repository.refreshTrips()
            isEngineReady.value = true // ARCHITECT'S MOVE: Data is now "Ready"
        }

        // 3. UI PIPELINE
        viewModelScope.launch {
            combine(
                authRepository.currentUser,
                repository.getAvailableTrips(),
                isEngineReady
            ) { user, trips, ready ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    if (trips.isNotEmpty()) {
                        val grouped = trips.groupBy { Category(it.category.name) }
                        val sortedMap = grouped.keys
                            .sortedByDescending { it.name == "GUIDE" }
                            .associateWith { grouped[it]!! }
                        _uiState.update { it.copy(groupedPaths = sortedMap, isLoading = false) }
                    } else {
                        // If trips are empty but engine is ready, stop loading (shows error fallback)
                        _uiState.update { it.copy(isLoading = !ready) }
                    }
                }
            }.collect()
        }
    }

    fun signIn(context: Context) = viewModelScope.launch { authRepository.signIn(context) }
    fun onGuestSignIn() = viewModelScope.launch { authRepository.continueAsGuest() }
    fun signOut() = viewModelScope.launch { authRepository.signOut(); _navigationEvent.emit("home") }

    fun wipeAllUserData() = viewModelScope.launch {
        isEngineReady.value = false
        _uiState.update { it.copy(isLoading = true) }
        repository.nukeAllData()
        preferenceManager.clearCurrentMissionData()
        preferenceManager.updateState(UserJourneyState.BROWSING, null)
        repository.refreshTrips()
        isEngineReady.value = true
        _navigationEvent.emit("home")
    }

    fun triggerHapticTick() { hapticManager.tick() }
    fun onLanguageChange(code: String) = viewModelScope.launch { preferenceManager.updateLanguage(code) }
    fun onHideTutorialPermanent() { viewModelScope.launch { preferenceManager.setHasSeenTutorial(true) } }
    fun prepareForNewMission() { viewModelScope.launch { preferenceManager.clearCurrentMissionData() } }

    private val _stampingTrip = MutableStateFlow<TripPath?>(null)
    val stampingTrip: StateFlow<TripPath?> = _stampingTrip.asStateFlow()
    fun onCompleteTrip(trip: TripPath) { /* existing logic */ }
    fun onSlamAnimationFinished() { _stampingTrip.value = null }
}