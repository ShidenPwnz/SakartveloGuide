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

// --- TOP LEVEL CLASSES ---
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

    // --- UI STATES ---
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isSplashReady = MutableStateFlow(false)
    val isSplashReady: StateFlow<Boolean> = _isSplashReady.asStateFlow()

    // --- MISSION STATES ---
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

    init {
        initSakartveloEngine()
    }

    private fun initSakartveloEngine() {
        viewModelScope.launch {
            // 1. Refresh Data
            repository.refreshTrips()

            // 2. Hydrate Profile
            val savedProfile = logisticsProfileManager.logisticsProfile.first()
            _logisticsProfile.value = savedProfile

            // 3. Session Recovery
            val session = preferenceManager.userSession.first()
            if (session.state == UserJourneyState.ON_THE_ROAD && session.activePathId != null) {
                val activeTrip = repository.getTripById(session.activePathId)
                if (activeTrip != null) {
                    _dynamicBattlePlan.value = activeTrip.itinerary
                    // Restore Thread & Index
                    _missionThread.value = generateThread(activeTrip, savedProfile)
                    _activeStepIndex.value = session.activeStepIndex

                    _initialDestination.value = "battle/${activeTrip.id}"
                    assetCacheManager.cacheMissionAssets(activeTrip)
                } else {
                    _initialDestination.value = "home"
                }
            } else {
                _initialDestination.value = "home"
            }

            // 4. Load Matrix
            repository.getAvailableTrips().collectLatest { trips ->
                if (trips.isNotEmpty()) {
                    // ARCHITECT'S FIX: Explicitly convert Enum to String for UI Category
                    val grouped = trips.groupBy { Category(it.category.name) }
                    _uiState.update { it.copy(groupedPaths = grouped, isLoading = false) }
                    _isSplashReady.value = true
                }
            }
        }
    }

    // --- WIZARD LOGIC ---
    fun initiateLogistics(tripId: String) { _pendingLogisticsTripId.value = tripId }
    fun dismissWizard() { _pendingLogisticsTripId.value = null }

    fun onConfirmLogistics(profile: LogisticsProfile) {
        _logisticsProfile.value = profile
        viewModelScope.launch {
            logisticsProfileManager.saveFullProfile(profile)

            val id = _pendingLogisticsTripId.value
            if (id != null) {
                _navigationEvent.emit("logistics/$id")
            }
            _pendingLogisticsTripId.value = null
        }
    }

    // --- MISSION EXECUTION ---
    fun startMission(trip: TripPath) {
        viewModelScope.launch {
            val profile = _logisticsProfile.value

            // Generate Thread
            val thread = generateThread(trip, profile)
            _missionThread.value = thread
            _activeStepIndex.value = 0
            _dynamicBattlePlan.value = trip.itinerary

            // Persistence
            preferenceManager.updateState(UserJourneyState.ON_THE_ROAD, trip.id)
            preferenceManager.updateStepIndex(0)

            hapticManager.tick()
            assetCacheManager.cacheMissionAssets(trip)
        }
    }

    // --- THREAD GENERATOR (AFFILIATE NEXUS) ---
    private fun generateThread(trip: TripPath, profile: LogisticsProfile): List<MissionStep> {
        val thread = mutableListOf<MissionStep>()

        // 1. Insertion: Pre-Arrival Protocol
        // STRATEGY: Capture users before they even land with essentials (Insurance/Flights check)
        thread.add(MissionStep.LogisticsAnchor(
            title = "PRE-FLIGHT CHECK",
            description = "Secure Nomad Insurance & Flight Status.",
            actionUrl = affiliateManager.getInsuranceLink(),
            iconType = "INSURANCE"
        ))

        val arrivalTitle = when (profile.entryPoint) {
            EntryPoint.AIRPORT_TBS -> "TBS: AIRPORT INFILTRATION"
            EntryPoint.AIRPORT_KUT -> "KUT: FLIGHT LANDING"
            EntryPoint.CITY_CENTER -> "DEPLOYMENT: CITY CENTER"
        }
        val arrivalDesc = when (profile.entryPoint) {
            EntryPoint.AIRPORT_TBS -> "Locate Departure Level (2nd Floor) for pickup."
            EntryPoint.AIRPORT_KUT -> "Locate Georgian Bus counter."
            EntryPoint.CITY_CENTER -> "Proceed to start point."
        }

        thread.add(MissionStep.AirportProtocol(
            title = arrivalTitle,
            description = arrivalDesc,
            entryPoint = profile.entryPoint
        ))

        // High-Conversion Utility: eSIM
        if (profile.needsEsim) {
            thread.add(MissionStep.AcquireEsim(
                actionUrl = affiliateManager.getEsimLink()
            ))
        }

        // Smart Transport Logic
        val categoryEnum = trip.category
        val tripCity = trip.title.split(":").firstOrNull()?.trim() ?: ""

        if (profile.transportType == TransportType.TAXI) {
            // STRATEGY: Use GoTrip for intercity (High Commission) vs Bolt for local
            val taxiUrl = affiliateManager.getTaxiLink(categoryEnum, tripCity)
            val isGoTrip = taxiUrl.contains("gotrip")

            thread.add(MissionStep.LogisticsAnchor(
                title = if (isGoTrip) "SECURE INTERCITY TRANSFER" else "INITIAL DEPLOYMENT",
                description = if (isGoTrip) "Book private driver to $tripCity (GoTrip)." else "Call Bolt.",
                actionUrl = taxiUrl,
                iconType = "BOLT"
            ))

            // Cross-Sell: Train for Batumi
            if (tripCity.equals("Batumi", ignoreCase = true)) {
                 thread.add(MissionStep.LogisticsAnchor(
                    title = "ALTERNATIVE: RAILWAY",
                    description = "Book Stadler train tickets to Batumi.",
                    actionUrl = affiliateManager.getTrainLink(),
                    iconType = "TRAIN"
                ))
            }
        } else if (profile.transportType == TransportType.RENTAL_4X4) {
            thread.add(MissionStep.LogisticsAnchor(
                "ACQUIRE FLEET", "Pick up 4x4 vehicle (No Deposit).", affiliateManager.getRentalLink(), "RENTAL"
            ))
        }

        // Accommodation
        if (profile.needsAccommodation) {
            thread.add(MissionStep.LogisticsAnchor(
                "SECURE BASE CAMP", "Find guesthouses in $tripCity.", affiliateManager.getBookingLink(trip.title), "HOTEL"
            ))
        }

        // Luggage Storage (Utility)
        thread.add(MissionStep.LogisticsAnchor(
            "GEAR STASH", "Store excess luggage securely.", affiliateManager.getLuggageStorageLink(), "LOCKER"
        ))

        // 2. Campaign: Experiential Up-Sells
        if (categoryEnum == RouteCategory.WINE_CELLAR || categoryEnum == RouteCategory.WINE_REGION) {
            thread.add(MissionStep.PremiumExperience(
                actionUrl = affiliateManager.getWineTourLink()
            ))
        }

        // Dynamic Tour Insertion based on Trip Type
        val tourLink = affiliateManager.getTourLink(categoryEnum, tripCity)
        thread.add(MissionStep.LogisticsAnchor(
            "RECONNAISSANCE", "Book guided tours & experiences.", tourLink, "TOUR"
        ))

        trip.itinerary.forEach { node ->
            thread.add(MissionStep.Activity(
                node = node,
                taxiBridgeUrl = if (profile.transportType == TransportType.TAXI)
                    affiliateManager.getTaxiLink(categoryEnum) else null
            ))
        }

        // 3. Extraction
        thread.add(MissionStep.Extraction(
            actionUrl = if (profile.transportType == TransportType.TAXI)
                affiliateManager.getTaxiLink(categoryEnum) else null
        ))

        return thread
    }

    // --- PROGRESSION ---
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

    // --- COMPLETION ---
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
            _dynamicBattlePlan.value = emptyList()
            _missionThread.value = emptyList()
            _navigationEvent.emit("passport")
        }
    }

    fun onAbortTrip() {
        viewModelScope.launch {
            preferenceManager.updateState(UserJourneyState.BROWSING, null)
            preferenceManager.updateStepIndex(0)
            _pendingLogisticsTripId.value = null
            _dynamicBattlePlan.value = emptyList()
            _missionThread.value = emptyList()
            _initialDestination.value = "home"
            _navigationEvent.emit("home")
        }
    }

    // Legacy Stubs
    fun onCallFleet(title: String) {}
    fun onOpenBolt() {}
    fun onBookAccommodation(city: String) {}
}