package com.example.sakartveloguide.presentation.home

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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
import com.example.sakartveloguide.ui.manager.HapticManager
import com.example.sakartveloguide.ui.manager.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*
import kotlin.time.Duration.Companion.milliseconds

// --- UI STATE DEFINITIONS (KEEP THESE HERE) ---
data class Category(val name: String)
data class HomeUiState(
    val groupedPaths: Map<Category, List<TripPath>> = emptyMap(),
    val isLoading: Boolean = true
)
// ----------------------------------------------

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

    private val _pendingLogisticsTripId = MutableStateFlow<String?>(null)
    val pendingLogisticsTripId: StateFlow<String?> = _pendingLogisticsTripId.asStateFlow()

    private val _stampingTrip = MutableStateFlow<TripPath?>(null)
    val stampingTrip: StateFlow<TripPath?> = _stampingTrip.asStateFlow()

    private val _showOutOfRangeDialog = MutableStateFlow(false)
    val showOutOfRangeDialog: StateFlow<Boolean> = _showOutOfRangeDialog.asStateFlow()

    private val _activeStepIndex = MutableStateFlow(0)
    val activeStepIndex: StateFlow<Int> = _activeStepIndex.asStateFlow()

    // REACTIVE THREADS
    val missionThread: StateFlow<List<MissionStep>> = userSession
        .map { it.activePathId to it.language }
        .distinctUntilChanged()
        .combine(_logisticsProfile) { (pathId, lang), profile ->
            if (pathId != null) {
                val trips = _uiState.value.groupedPaths.values.flatten()
                val trip = trips.find { it.id == pathId }
                // Safe fallback for language
                val safeLang = if (lang.isEmpty()) "en" else lang
                if (trip != null) generateThread(trip, profile, true, safeLang) else emptyList()
            } else emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val previewThread: StateFlow<List<MissionStep>> = combine(
        _logisticsProfile, _pendingLogisticsTripId, userSession
    ) { profile, tripId, session ->
        val trips = _uiState.value.groupedPaths.values.flatten()
        val trip = trips.find { it.id == tripId }
        val safeLang = if (session.language.isEmpty()) "en" else session.language
        if (trip != null) generateThread(trip, profile, false, safeLang) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { initSakartveloEngine() }

    private fun initSakartveloEngine() {
        viewModelScope.launch {
            try {
                repository.refreshTrips()

                _logisticsProfile.value = logisticsProfileManager.logisticsProfile
                    .timeout(2000.milliseconds).catch { emit(LogisticsProfile()) }.first()

                val session = preferenceManager.userSession
                    .timeout(2000.milliseconds).catch { emit(UserSession()) }.first()

                if (session.state == UserJourneyState.ON_THE_ROAD && session.activePathId != null) {
                    _activeStepIndex.value = session.activeStepIndex
                    _initialDestination.value = "battle/${session.activePathId}"
                } else {
                    _initialDestination.value = "home"
                }

                launch {
                    repository.getAvailableTrips().collectLatest { trips ->
                        if (trips.isNotEmpty()) {
                            val grouped = trips.groupBy { Category(it.category.name) }.toMutableMap()
                            val sortedMap = grouped.keys.sortedByDescending { it.name == "GUIDE" }
                                .associateWith { grouped[it]!! }
                            _uiState.update { it.copy(groupedPaths = sortedMap, isLoading = false) }
                            _isSplashReady.value = true
                        }
                    }
                }
            } catch (e: Exception) {
                _isSplashReady.value = true
                _initialDestination.value = "home"
            }
        }
    }

    fun onConfirmLogistics(profile: LogisticsProfile) {
        _logisticsProfile.value = profile
        viewModelScope.launch {
            logisticsProfileManager.saveFullProfile(profile)
            _pendingLogisticsTripId.value?.let { _navigationEvent.emit("mission_protocol/$it") }
        }
    }

    fun startMission(trip: TripPath) {
        viewModelScope.launch {
            preferenceManager.updateState(UserJourneyState.ON_THE_ROAD, trip.id)
            preferenceManager.updateStepIndex(0)
            _activeStepIndex.value = 0
            hapticManager.tick()
        }
    }

    fun onLanguageChange(code: String) {
        viewModelScope.launch {
            preferenceManager.updateLanguage(code)
            hapticManager.tick()
        }
    }

    private fun generateThread(trip: TripPath, profile: LogisticsProfile, includeDebriefing: Boolean, lang: String): List<MissionStep> {
        // Use safe fallback if language is somehow empty
        val safeLang = if (lang.isEmpty()) "en" else lang
        val locContext = LocaleUtils.getLocalizedContext(context, safeLang)

        val thread = mutableListOf<MissionStep>()
        thread.add(MissionStep.AirportProtocol(locContext.getString(R.string.step_infiltration), locContext.getString(R.string.step_secure_assets), profile.entryPoint))

        trip.itinerary.forEachIndexed { index, node ->
            thread.add(MissionStep.Activity(node.title.get(safeLang), node.description.get(safeLang), node))
            if (index < trip.itinerary.size - 1) {
                val nextNode = trip.itinerary[index + 1]
                if (node.location != null && nextNode.location != null) {
                    thread.add(calculateTacticalBridge(node, nextNode, profile, safeLang))
                }
            }
        }

        thread.add(MissionStep.Extraction(locContext.getString(R.string.step_extraction), locContext.getString(R.string.step_return_base)))

        if (includeDebriefing) {
            thread.add(MissionStep.PremiumExperience(locContext.getString(R.string.step_debriefing), locContext.getString(R.string.step_rate_us), "https://play.google.com"))
        }
        return thread
    }

    private fun calculateTacticalBridge(startNode: BattleNode, endNode: BattleNode, profile: LogisticsProfile, lang: String): MissionStep.TacticalBridge {
        val locContext = LocaleUtils.getLocalizedContext(context, lang)
        val distance = calculateDistanceKm(startNode.location!!, endNode.location!!)
        val hasCar = profile.transportType == TransportType.RENTAL_4X4 || profile.transportType == TransportType.OWN_CAR
        val baseMaps = "https://www.google.com/maps/dir/?api=1&origin=${startNode.location.latitude},${startNode.location.longitude}&destination=${endNode.location.latitude},${endNode.location.longitude}"

        return when {
            distance < 0.3 -> MissionStep.TacticalBridge(locContext.getString(R.string.mode_walk), locContext.getString(R.string.desc_walk), walkUrl = "$baseMaps&travelmode=walking", distanceKm = distance, primaryMode = "WALK")
            else -> MissionStep.TacticalBridge(locContext.getString(R.string.mode_fleet), locContext.getString(R.string.desc_fleet), driveUrl = "$baseMaps&travelmode=driving", distanceKm = distance, primaryMode = if(hasCar) "DRIVE" else "CHARTER")
        }
    }

    fun onObjectiveSecured() { viewModelScope.launch { val newIdx = _activeStepIndex.value + 1; _activeStepIndex.value = newIdx; preferenceManager.updateStepIndex(newIdx); hapticManager.tick(); soundManager.playTick() } }
    fun onCompleteTrip(trip: TripPath) { viewModelScope.launch { val loc = try { locationManager.getCurrentLocation() } catch (e: Exception) { null }; val target = trip.itinerary.lastOrNull { it.location != null }?.location ?: GeoPoint(41.71, 44.82); val dist = if (loc != null) calculateDistanceKm(loc, target) else 999.0; if (dist > 500.0) { _showOutOfRangeDialog.value = true; return@launch }; _stampingTrip.value = trip; hapticManager.missionCompleteSlam(); soundManager.playStampSlam(); addPassportStampUseCase(trip); preferenceManager.updateState(UserJourneyState.BROWSING, null) } }
    private fun calculateDistanceKm(p1: GeoPoint, p2: GeoPoint): Double { val r = 6371.0; val dLat = Math.toRadians(p2.latitude - p1.latitude); val dLon = Math.toRadians(p2.longitude - p1.longitude); val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) * sin(dLon / 2).pow(2.0); return r * 2 * atan2(sqrt(a), sqrt(1 - a)) }
    fun onSlamAnimationFinished() { viewModelScope.launch { _stampingTrip.value = null; _navigationEvent.emit("home") } }
    fun triggerHapticTick() { hapticManager.tick() }
    fun dismissOutOfRangeDialog() { _showOutOfRangeDialog.value = false }
    fun onHideTutorialPermanent() { viewModelScope.launch { preferenceManager.setHasSeenTutorial(true) } }
    fun initiateLogistics(tripId: String) { _pendingLogisticsTripId.value = tripId }
    fun updateEntryPoint(point: EntryPoint) { _logisticsProfile.update { it.copy(entryPoint = point) } }
    fun updateExitPoint(point: EntryPoint) { _logisticsProfile.update { it.copy(exitPoint = point) } }
    fun wipeAllUserData() { viewModelScope.launch { preferenceManager.updateState(UserJourneyState.BROWSING, null); repository.nukeAllData(); repository.refreshTrips(); _navigationEvent.emit("home"); hapticManager.missionCompleteSlam() } }
    fun onAbortTrip() { viewModelScope.launch { preferenceManager.updateState(UserJourneyState.BROWSING, null); _navigationEvent.emit("home") } }
    override fun onCleared() { super.onCleared(); soundManager.release() }
}