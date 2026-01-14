package com.example.sakartveloguide.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.LogisticsProfileManager
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.data.manager.AffiliateManager
import com.example.sakartveloguide.data.manager.AssetCacheManager
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import com.example.sakartveloguide.domain.usecase.AddPassportStampUseCase
import com.example.sakartveloguide.ui.manager.HapticManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val preferenceManager: PreferenceManager,
    private val logisticsProfileManager: LogisticsProfileManager,
    private val assetCacheManager: AssetCacheManager,
    private val affiliateManager: AffiliateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isSplashReady = MutableStateFlow(false)
    val isSplashReady: StateFlow<Boolean> = _isSplashReady.asStateFlow()

    private val _initialDestination = MutableStateFlow<String?>(null)
    val initialDestination: StateFlow<String?> = _initialDestination.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _dynamicBattlePlan = MutableStateFlow<List<BattleNode>>(emptyList())
    val dynamicBattlePlan: StateFlow<List<BattleNode>> = _dynamicBattlePlan.asStateFlow()

    private val _logisticsProfile = MutableStateFlow(LogisticsProfile())
    val logisticsProfile: StateFlow<LogisticsProfile> = _logisticsProfile.asStateFlow()

    private val _pendingLogisticsTripId = MutableStateFlow<String?>(null)
    val pendingLogisticsTripId: StateFlow<String?> = _pendingLogisticsTripId.asStateFlow()

    private val _stampingTrip = MutableStateFlow<TripPath?>(null)
    val stampingTrip: StateFlow<TripPath?> = _stampingTrip.asStateFlow()

    private val _missionThread = MutableStateFlow<List<MissionStep>>(emptyList())
    val missionThread = _missionThread.asStateFlow()

    private val _activeStepIndex = MutableStateFlow(0)
    val activeStepIndex = _activeStepIndex.asStateFlow()

    val previewThread: StateFlow<List<MissionStep>> = _logisticsProfile
        .combine(_pendingLogisticsTripId) { profile, tripId ->
            val trips = _uiState.value.groupedPaths.values.flatten()
            val trip = trips.find { it.id == tripId }
            if (trip != null) generateThread(trip, profile) else emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        initSakartveloEngine()
    }

    private fun initSakartveloEngine() {
        viewModelScope.launch {
            try {
                repository.refreshTrips()

                val savedProfile = logisticsProfileManager.logisticsProfile.first()
                _logisticsProfile.value = savedProfile

                val session = preferenceManager.userSession.first()
                if (session.state == UserJourneyState.ON_THE_ROAD && session.activePathId != null) {
                    val activeTrip = repository.getTripById(session.activePathId)
                    if (activeTrip != null) {
                        _dynamicBattlePlan.value = activeTrip.itinerary
                        _missionThread.value = generateThread(activeTrip, savedProfile)
                        _activeStepIndex.value = session.activeStepIndex
                        _initialDestination.value = "battle/${activeTrip.id}"
                        assetCacheManager.cacheMissionAssets(activeTrip)
                    } else { _initialDestination.value = "home" }
                } else { _initialDestination.value = "home" }

                repository.getAvailableTrips()
                    .catch { _isSplashReady.value = true }
                    .collectLatest { trips ->
                        if (trips.isNotEmpty()) {
                            val grouped = trips.groupBy { Category(it.category.name) }
                            _uiState.update { it.copy(groupedPaths = grouped, isLoading = false) }
                        }
                        _isSplashReady.value = true
                    }
            } catch (e: Exception) {
                _isSplashReady.value = true
            }
        }
    }

    fun initiateLogistics(tripId: String) {
        _pendingLogisticsTripId.value = tripId
        _logisticsProfile.update { it.copy(needsAccommodation = false, needsEsim = false, needsFlight = false) }
    }

    fun onConfirmLogistics(profile: LogisticsProfile) {
        _logisticsProfile.value = profile
        viewModelScope.launch {
            logisticsProfileManager.saveFullProfile(profile)
            val id = _pendingLogisticsTripId.value
            if (id != null) { _navigationEvent.emit("mission_protocol/$id") }
        }
    }

    fun updateEntryPoint(point: EntryPoint) { _logisticsProfile.update { it.copy(entryPoint = point) } }
    fun updateExitPoint(point: EntryPoint) { _logisticsProfile.update { it.copy(exitPoint = point) } }
    fun toggleEsim(enabled: Boolean) { _logisticsProfile.update { it.copy(needsEsim = enabled) } }
    fun toggleLodging(enabled: Boolean) { _logisticsProfile.update { it.copy(needsAccommodation = enabled) } }
    fun toggleFlight(enabled: Boolean) { _logisticsProfile.update { it.copy(needsFlight = enabled) } }
    fun updateTransport(type: TransportType) { _logisticsProfile.update { it.copy(transportType = type) } }

    fun startMission(trip: TripPath) {
        viewModelScope.launch {
            val profile = _logisticsProfile.value
            _missionThread.value = generateThread(trip, profile)
            _activeStepIndex.value = 0
            preferenceManager.updateState(UserJourneyState.ON_THE_ROAD, trip.id)
            preferenceManager.updateStepIndex(0)
            hapticManager.tick()
            assetCacheManager.cacheMissionAssets(trip)
        }
    }

    private fun generateThread(trip: TripPath, profile: LogisticsProfile): List<MissionStep> {
        val thread = mutableListOf<MissionStep>()
        val arrivalTitle = when (profile.entryPoint) {
            EntryPoint.AIRPORT_TBS -> "TBS: INFILTRATION"
            EntryPoint.AIRPORT_KUT -> "KUT: INFILTRATION"
            EntryPoint.AIRPORT_BUS -> "BUS: INFILTRATION"
            else -> "BORDER: INFILTRATION"
        }
        thread.add(MissionStep.AirportProtocol(arrivalTitle, "Secure arrival assets.", profile.entryPoint))
        if (profile.needsEsim) thread.add(MissionStep.AcquireEsim(actionUrl = affiliateManager.getEsimLink()))
        if (profile.needsAccommodation) thread.add(MissionStep.LogisticsAnchor("SECURE LODGING", "Booking.com check-in.", affiliateManager.getBookingLink(trip.title), "HOTEL"))
        if (profile.transportType == TransportType.TAXI) {
            thread.add(MissionStep.LogisticsAnchor("SECURE TRANSPORT", "Order ride.", affiliateManager.getTaxiLink(trip.category), "BOLT"))
        } else if (profile.transportType == TransportType.RENTAL_4X4) {
            thread.add(MissionStep.LogisticsAnchor("ACQUIRE FLEET", "Pick up vehicle.", affiliateManager.getRentalLink(), "RENTAL"))
        }
        trip.itinerary.forEach { thread.add(MissionStep.Activity(it)) }
        val exitTitle = when (profile.exitPoint) {
            EntryPoint.AIRPORT_TBS -> "TBS: EXTRACTION"
            EntryPoint.AIRPORT_KUT -> "KUT: EXTRACTION"
            EntryPoint.AIRPORT_BUS -> "BUS: EXTRACTION"
            else -> "BORDER: EXTRACTION"
        }
        thread.add(MissionStep.Extraction(exitTitle, "Return to departure point.", actionUrl = if(profile.transportType == TransportType.TAXI) "https://bolt.eu" else null))
        return thread
    }

    fun onObjectiveSecured() {
        viewModelScope.launch {
            if (_activeStepIndex.value < _missionThread.value.size - 1) {
                val newIndex = _activeStepIndex.value + 1
                _activeStepIndex.value = newIndex
                hapticManager.tick()
                preferenceManager.updateStepIndex(newIndex)
            }
        }
    }

    fun onCompleteTrip(trip: TripPath) {
        viewModelScope.launch {
            _stampingTrip.value = trip
            hapticManager.missionCompleteSlam()
            addPassportStampUseCase(trip)
            preferenceManager.updateState(UserJourneyState.COMPLETED, null)
        }
    }

    fun onSlamAnimationFinished() {
        viewModelScope.launch {
            _stampingTrip.value = null
            _navigationEvent.emit("passport")
        }
    }

    fun onAbortTrip() {
        viewModelScope.launch {
            preferenceManager.updateState(UserJourneyState.BROWSING, null)
            preferenceManager.updateStepIndex(0)
            _initialDestination.value = "home"
            _navigationEvent.emit("home")
        }
    }

    fun onCallFleet(title: String) {}
    fun onOpenBolt() {}
    fun onBookAccommodation(city: String) {}
}