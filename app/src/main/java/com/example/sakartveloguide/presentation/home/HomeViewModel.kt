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

    init {
        initAdventureEngine()
    }

    private fun initAdventureEngine() {
        Log.d("SAKARTVELO_VM", "Initializing Engine...")

        // 1. Navigation & Splash Control
        viewModelScope.launch {
            preferenceManager.userSession
                .catch { emit(UserSession()) }
                .collect { session ->
                    Log.d("SAKARTVELO_VM", "Session detected, target: ${session.activePathId}")
                    if (session.state == UserJourneyState.ON_THE_ROAD && session.activePathId != null) {
                        _initialDestination.value = "briefing/${session.activePathId}?ids="
                    } else {
                        _initialDestination.value = "home"
                    }
                    _isSplashReady.value = true
                }
        }

        // 2. Background Data Sync (Non-blocking)
        viewModelScope.launch {
            Log.d("SAKARTVELO_VM", "Triggering Data Refresh...")
            repository.refreshTrips()
        }

        // 3. UI State Pipeline
        viewModelScope.launch {
            // Combine Auth state with Database state
            combine(
                authRepository.currentUser,
                repository.getAvailableTrips()
            ) { user, trips ->
                Log.d("SAKARTVELO_VM", "Syncing: User=${user?.id}, TripsCount=${trips.size}")

                if (user != null) {
                    if (trips.isNotEmpty()) {
                        val grouped = trips.groupBy { Category(it.category.name) }
                        val sortedMap = grouped.keys
                            .sortedByDescending { it.name == "GUIDE" }
                            .associateWith { grouped[it]!! }

                        _uiState.update { it.copy(groupedPaths = sortedMap, isLoading = false) }
                    } else {
                        // DB is empty, keep loading but allow AuthGatekeeper to be hidden
                        _uiState.update { it.copy(isLoading = true) }
                    }
                } else {
                    // Not logged in: Spinner MUST stop so Auth screen can show
                    _uiState.update { it.copy(isLoading = false) }
                }
            }.catch { e ->
                Log.e("SAKARTVELO_VM", "Engine Stream Failed", e)
                _uiState.update { it.copy(isLoading = false) }
            }.collect()
        }
    }

    fun signIn(context: Context) = viewModelScope.launch { authRepository.signIn(context) }
    fun onGuestSignIn() = viewModelScope.launch { authRepository.continueAsGuest() }
    fun signOut() = viewModelScope.launch {
        authRepository.signOut()
        _navigationEvent.emit("home")
    }

    fun wipeAllUserData() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        repository.nukeAllData()
        preferenceManager.clearCurrentMissionData()
        preferenceManager.updateState(UserJourneyState.BROWSING, null)
        repository.refreshTrips()
        _navigationEvent.emit("home")
    }

    fun triggerHapticTick() { hapticManager.tick() }
    fun onLanguageChange(code: String) = viewModelScope.launch { preferenceManager.updateLanguage(code) }
    fun onHideTutorialPermanent() { viewModelScope.launch { preferenceManager.setHasSeenTutorial(true) } }

    // Stamping
    private val _stampingTrip = MutableStateFlow<TripPath?>(null)
    val stampingTrip: StateFlow<TripPath?> = _stampingTrip.asStateFlow()
    fun onCompleteTrip(trip: TripPath) { /* ... */ }
    fun onSlamAnimationFinished() { _stampingTrip.value = null }
    fun prepareForNewMission() { viewModelScope.launch { preferenceManager.clearCurrentMissionData() } }
}