package com.example.sakartveloguide.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.LogisticsProfileManager
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.data.manager.AffiliateManager
import com.example.sakartveloguide.data.manager.AssetCacheManager
import com.example.sakartveloguide.domain.location.LocationManager
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import com.example.sakartveloguide.domain.usecase.AddPassportStampUseCase
import com.example.sakartveloguide.ui.manager.HapticManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class Category(val name: String)
data class HomeUiState(val groupedPaths: Map<Category, List<TripPath>> = emptyMap(), val isLoading: Boolean = true)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TripRepository,
    private val addPassportStampUseCase: AddPassportStampUseCase,
    private val hapticManager: HapticManager,
    private val preferenceManager: PreferenceManager,
    private val logisticsProfileManager: LogisticsProfileManager,
    private val assetCacheManager: AssetCacheManager,
    private val affiliateManager: AffiliateManager,
    private val locationManager: LocationManager
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

    val userSession: Flow<UserSession> = preferenceManager.userSession

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
            if (trip != null) generateThread(trip, profile, false) else emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        initSakartveloEngine()
    }

    private fun initSakartveloEngine() {
        viewModelScope.launch {
            try {
                repository.refreshTrips()
                val savedProfile = logisticsProfileManager.logisticsProfile.timeout(2000.milliseconds).catch { emit(LogisticsProfile()) }.first()
                _logisticsProfile.value = savedProfile

                val session = preferenceManager.userSession.first()
                if (session.state == UserJourneyState.ON_THE_ROAD && session.activePathId != null) {
                    val activeTrip = repository.getTripById(session.activePathId)
                    if (activeTrip != null) {
                        _dynamicBattlePlan.value = activeTrip.itinerary
                        _missionThread.value = generateThread(activeTrip, savedProfile, true)
                        _activeStepIndex.value = session.activeStepIndex
                        _initialDestination.value = "battle/${activeTrip.id}"
                        assetCacheManager.cacheMissionAssets(activeTrip)
                    } else { _initialDestination.value = "home" }
                } else { _initialDestination.value = "home" }

                repository.getAvailableTrips().collectLatest { trips ->
                    if (trips.isNotEmpty()) {
                        val grouped = trips.groupBy { Category(it.category.name) }.toMutableMap()
                        val sortedKeys = grouped.keys.sortedByDescending { it.name == "GUIDE" }
                        val sortedMap = sortedKeys.associateWith { grouped[it]!! }
                        _uiState.update { it.copy(groupedPaths = sortedMap, isLoading = false) }
                    }
                    _isSplashReady.value = true
                }
            } catch (e: Exception) {
                _isSplashReady.value = true
            }
        }
    }

    fun onHideTutorialPermanent() {
        viewModelScope.launch { preferenceManager.setHasSeenTutorial(true) }
    }

    fun initiateLogistics(tripId: String) {
        _pendingLogisticsTripId.value = tripId
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
    fun toggleTransport(enabled: Boolean) { _logisticsProfile.update { it.copy(needsTransport = enabled) } }
    fun updateTransport(type: TransportType) { _logisticsProfile.update { it.copy(transportType = type) } }

    fun startMission(trip: TripPath) {
        viewModelScope.launch {
            val profile = _logisticsProfile.value
            _missionThread.value = generateThread(trip, profile, true)
            _activeStepIndex.value = 0
            preferenceManager.updateState(UserJourneyState.ON_THE_ROAD, trip.id)
            preferenceManager.updateStepIndex(0)
            hapticManager.tick()
            assetCacheManager.cacheMissionAssets(trip)
        }
    }

    private fun generateThread(trip: TripPath, profile: LogisticsProfile, includeDebriefing: Boolean): List<MissionStep> {
        val thread = mutableListOf<MissionStep>()

        // 1. Infiltration (Logistics)
        val arrivalTitle = when (profile.entryPoint) {
            EntryPoint.AIRPORT_TBS -> "TBS: INFILTRATION"
            EntryPoint.AIRPORT_KUT -> "KUT: INFILTRATION"
            EntryPoint.AIRPORT_BUS -> "BUS: INFILTRATION"
            else -> "BORDER: INFILTRATION"
        }
        thread.add(MissionStep.AirportProtocol(arrivalTitle, "Secure arrival assets.", profile.entryPoint))
        if (profile.needsEsim) thread.add(MissionStep.AcquireEsim("CONNECTIVITY", "Magti 4G eSIM.", affiliateManager.getEsimLink()))
        if (profile.needsAccommodation) thread.add(MissionStep.LogisticsAnchor("SECURE LODGING", "Booking.com check-in.", affiliateManager.getBookingLink(trip.title), "HOTEL"))
        if (profile.needsTransport) {
            thread.add(MissionStep.LogisticsAnchor("SECURE TRANSPORT", "Order ride.", affiliateManager.getTaxiLink(trip.category), "BOLT"))
        }

        // 2. The "Common Sense" Itinerary
        trip.itinerary.forEachIndexed { index, node ->
            thread.add(MissionStep.Activity(node))

            if (index < trip.itinerary.size - 1) {
                val nextNode = trip.itinerary[index + 1]
                if (node.location != null && nextNode.location != null) {
                    val distance = calculateDistanceKm(node.location, nextNode.location)
                    val hasCar = profile.transportType == TransportType.RENTAL_4X4 || profile.transportType == TransportType.OWN_CAR

                    // BASE MAPS URL
                    val baseMaps = "https://www.google.com/maps/dir/?api=1&origin=${node.location.latitude},${node.location.longitude}&destination=${nextNode.location.latitude},${nextNode.location.longitude}"

                    val walkUrl = "$baseMaps&travelmode=walking"
                    val driveUrl = "$baseMaps&travelmode=driving"
                    val busUrl = "$baseMaps&travelmode=transit"
                    val boltUrl = "bolt://ride?destination_lat=${nextNode.location.latitude}&destination_lng=${nextNode.location.longitude}"

                    // --- LOGIC GATES ---

                    val bridge: MissionStep.TacticalBridge = when {
                        // GATE 1: MICRO-RANGE (< 400m) -> FORCE WALK
                        // (Ignores rental car because parking 300m away is stupid)
                        distance < 0.4 -> {
                            MissionStep.TacticalBridge(
                                title = "IMMEDIATE VICINITY",
                                description = "Target is ${String.format("%.0f", distance * 1000)}m away. Proceed on foot.",
                                walkUrl = walkUrl, // Primary is Walk
                                driveUrl = null,   // Hide Drive
                                boltUrl = null,    // Hide Bolt
                                distanceKm = distance,
                                primaryMode = "WALK"
                            )
                        }

                        // GATE 2: SHORT-RANGE (400m - 2.5km) -> HYBRID
                        distance < 2.5 -> {
                            if (hasCar) {
                                // Have Car? Offer Drive OR Walk
                                MissionStep.TacticalBridge(
                                    title = "SHORT RANGE TRANSIT",
                                    description = "Walkable distance, or reposition vehicle.",
                                    walkUrl = walkUrl,
                                    driveUrl = driveUrl,
                                    distanceKm = distance,
                                    primaryMode = "HYBRID_CAR" // Custom mode for UI
                                )
                            } else {
                                // No Car? Offer Walk OR Taxi/Bus
                                MissionStep.TacticalBridge(
                                    title = "URBAN TRANSIT",
                                    description = "Proceed on foot or secure transport.",
                                    walkUrl = walkUrl,
                                    boltUrl = boltUrl,
                                    busUrl = busUrl,
                                    distanceKm = distance,
                                    primaryMode = "HYBRID_FOOT" // Custom mode for UI
                                )
                            }
                        }

                        // GATE 3: MID-RANGE (2.5km - 30km) -> VEHICLE
                        distance < 30.0 -> {
                            if (hasCar) {
                                MissionStep.TacticalBridge(
                                    title = "DRIVE PROTOCOL",
                                    description = "Navigate vehicle to ${nextNode.title}.",
                                    driveUrl = driveUrl,
                                    distanceKm = distance,
                                    primaryMode = "DRIVE"
                                )
                            } else {
                                MissionStep.TacticalBridge(
                                    title = "RAPID EXTRACTION",
                                    description = "Distance exceeds walk limit. Request transport.",
                                    boltUrl = boltUrl,
                                    busUrl = busUrl,
                                    distanceKm = distance,
                                    primaryMode = "TAXI"
                                )
                            }
                        }

                        // GATE 4: LONG-RANGE (> 30km) -> FLEET
                        else -> {
                            MissionStep.TacticalBridge(
                                title = "FLEET NAVIGATION",
                                description = "Long range transit. Vehicle required.",
                                driveUrl = driveUrl,
                                distanceKm = distance,
                                primaryMode = "DRIVE"
                            )
                        }
                    }

                    thread.add(bridge)
                }
            }
        }

        // 3. Extraction
        thread.add(MissionStep.Extraction("MISSION EXTRACTION", "Return to departure point."))
        if (includeDebriefing) {
            thread.add(MissionStep.PremiumExperience("DEBRIEFING", "Mission success. Support the guide.", "https://play.google.com"))
        }
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
            val loc = try { locationManager.getCurrentLocation() } catch (e: Exception) { null }
            // ARCHITECT'S FIX: 500km anti-cheat gate
            val dist = if (loc != null) calculateDistanceKm(loc, GeoPoint(41.7, 44.8)) else 999.0
            if (dist > 500.0) {
                _navigationEvent.emit("error_out_of_range")
                return@launch
            }
            _stampingTrip.value = trip
            hapticManager.missionCompleteSlam()
            addPassportStampUseCase(trip)
            preferenceManager.updateState(UserJourneyState.BROWSING, null)
            preferenceManager.updateStepIndex(0)
        }
    }

    private fun calculateDistanceKm(p1: GeoPoint, p2: GeoPoint): Double {
        val r = 6371.0
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(p1.latitude)) * Math.cos(Math.toRadians(p2.latitude)) * Math.sin(dLon/2) * Math.sin(dLon/2)
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
    }

    fun onSlamAnimationFinished() {
        viewModelScope.launch {
            _stampingTrip.value = null
            _navigationEvent.emit("home")
        }
    }

    fun wipeAllUserData() {
        viewModelScope.launch {
            preferenceManager.updateState(UserJourneyState.BROWSING, null)
            preferenceManager.setHasSeenTutorial(false)
            repository.nukeAllData()
            repository.refreshTrips()
            _navigationEvent.emit("home")
        }
    }

    fun onAbortTrip() {
        viewModelScope.launch {
            preferenceManager.updateState(UserJourneyState.BROWSING, null)
            _navigationEvent.emit("home")
        }
    }

    fun onCallFleet(title: String) {}
    fun onOpenBolt() {}
    fun onBookAccommodation(city: String) {}
}