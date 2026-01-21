package com.example.sakartveloguide.presentation.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.R
import com.example.sakartveloguide.data.local.LogisticsProfileManager
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.data.manager.AffiliateManager
import com.example.sakartveloguide.data.manager.AssetCacheManager
import com.example.sakartveloguide.domain.location.LocationManager
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import com.example.sakartveloguide.domain.usecase.AddPassportStampUseCase
import com.example.sakartveloguide.domain.util.LocaleUtils
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
    private val logisticsProfileManager: LogisticsProfileManager,
    private val assetCacheManager: AssetCacheManager,
    private val affiliateManager: AffiliateManager,
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

    private val _logisticsProfile = MutableStateFlow(LogisticsProfile())
    val logisticsProfile: StateFlow<LogisticsProfile> = _logisticsProfile.asStateFlow()

    val userSession: Flow<UserSession> = preferenceManager.userSession
    val missionState: Flow<MissionState> = preferenceManager.missionState

    private val _stampingTrip = MutableStateFlow<TripPath?>(null)
    val stampingTrip: StateFlow<TripPath?> = _stampingTrip.asStateFlow()

    private val _showOutOfRangeDialog = MutableStateFlow(false)
    val showOutOfRangeDialog: StateFlow<Boolean> = _showOutOfRangeDialog.asStateFlow()

    // --- BRIEFING THREAD GENERATION ---
    val missionThread: StateFlow<List<MissionStep>> = combine(
        userSession, _logisticsProfile, missionState
    ) { session, profile, mState ->
        val trip = _uiState.value.groupedPaths.values.flatten().find { it.id == session.activePathId }
        if (trip != null) generateThread(trip, profile, true, session.language, mState) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val previewThread: StateFlow<List<MissionStep>> = combine(
        _logisticsProfile, MutableStateFlow("custom_cargo"), userSession, missionState
    ) { profile, tripId, session, mState ->
        val trip = _uiState.value.groupedPaths.values.flatten().find { it.id == tripId }
        if (trip != null) generateThread(trip, profile, false, session.language, mState) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { initSakartveloEngine() }

    private fun initSakartveloEngine() {
        viewModelScope.launch {
            try {
                repository.refreshTrips()
                val session = preferenceManager.userSession.firstOrNull() ?: UserSession()
                if (session.state == UserJourneyState.ON_THE_ROAD && session.activePathId != null) {
                    _initialDestination.value = "battle/${session.activePathId}"
                } else {
                    _initialDestination.value = "home"
                }

                repository.getAvailableTrips().collectLatest { trips ->
                    if (trips.isNotEmpty()) {
                        val grouped = trips.groupBy { Category(it.category.name) }.toMutableMap()
                        val sortedMap = grouped.keys.sortedByDescending { it.name == "GUIDE" }.associateWith { grouped[it]!! }
                        _uiState.update { it.copy(groupedPaths = sortedMap, isLoading = false) }
                        _isSplashReady.value = true
                    }
                }
            } catch (e: Exception) {
                _isSplashReady.value = true
            }
        }
    }

    // --- TACTICAL ACTIONS ---

    /**
     * ARCHITECT'S RESTORATION: The Final Extraction Sequence
     * Validates GPS location (Must be within 500km of target) and triggers Passport Stamp.
     */
    fun onCompleteTrip(trip: TripPath) {
        viewModelScope.launch {
            val loc = try { locationManager.getCurrentLocation() } catch (e: Exception) { null }
            val target = trip.itinerary.lastOrNull { it.location != null }?.location ?: GeoPoint(41.71, 44.82)

            val dist = if (loc != null) TacticalMath.calculateDistanceKm(loc, target) else 999.0

            if (dist > 500.0) {
                _showOutOfRangeDialog.value = true
                return@launch
            }

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
            preferenceManager.saveActiveLoadout(emptyList())
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

    private fun generateThread(trip: TripPath, profile: LogisticsProfile, includeDebriefing: Boolean, lang: String, mState: MissionState): List<MissionStep> {
        val safeLang = lang.ifEmpty { "en" }
        val locContext = LocaleUtils.getLocalizedContext(context, safeLang)
        val thread = mutableListOf<MissionStep>()
        thread.add(MissionStep.AirportProtocol(locContext.getString(R.string.step_infiltration), locContext.getString(R.string.step_secure_assets), profile.entryPoint))
        mState.fobLocation?.let { thread.add(MissionStep.SecureBase("BASE CAMP SECURED", "FOB established.", it)) }
        trip.itinerary.forEach { node -> thread.add(MissionStep.Activity(node.title.get(safeLang), node.description.get(safeLang), node)) }
        thread.add(MissionStep.Extraction(locContext.getString(R.string.step_extraction), locContext.getString(R.string.step_return_base)))
        return thread
    }

    fun triggerHapticTick() { hapticManager.tick() }
    fun onLanguageChange(code: String) = viewModelScope.launch { preferenceManager.updateLanguage(code) }
    fun onHideTutorialPermanent() { viewModelScope.launch { preferenceManager.setHasSeenTutorial(true) } }
    fun onSlamAnimationFinished() { viewModelScope.launch { _stampingTrip.value = null; _navigationEvent.emit("home") } }
    fun initiateLogistics(tripId: String) { /* Logic handled in NavGraph briefing route */ }
}