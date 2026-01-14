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
import com.example.sakartveloguide.ui.manager.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

data class Category(val name: String)
data class HomeUiState(val groupedPaths: Map<Category, List<TripPath>> = emptyMap(), val isLoading: Boolean = true)

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

    // Anti-Cheat State
    private val _showOutOfRangeDialog = MutableStateFlow(false)
    val showOutOfRangeDialog = _showOutOfRangeDialog.asStateFlow()

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
                val savedProfile = logisticsProfileManager.logisticsProfile
                    .timeout(2000.milliseconds)
                    .catch { emit(LogisticsProfile()) }
                    .first()
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

    fun dismissOutOfRangeDialog() { _showOutOfRangeDialog.value = false }

    fun onHideTutorialPermanent() {
        viewModelScope.launch { preferenceManager.setHasSeenTutorial(true) }
    }

    fun initiateLogistics(tripId: String) {
        _pendingLogisticsTripId.value = tripId
        _logisticsProfile.update { it.copy(needsAccommodation = false, needsEsim = false, needsFlight = false, needsTransport = false) }
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
            soundManager.playTick()
            assetCacheManager.cacheMissionAssets(trip)
        }
    }

    private fun generateThread(trip: TripPath, profile: LogisticsProfile, includeDebriefing: Boolean): List<MissionStep> {
        val thread = mutableListOf<MissionStep>()
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

        trip.itinerary.forEachIndexed { index, node ->
            thread.add(MissionStep.Activity(node))
            if (index < trip.itinerary.size - 1) {
                val nextNode = trip.itinerary[index + 1]
                if (node.location != null && nextNode.location != null) {
                    val bridge = calculateTacticalBridge(node, nextNode, profile, trip.category)
                    thread.add(bridge)
                }
            }
        }

        thread.add(MissionStep.Extraction("MISSION EXTRACTION", "Return to departure point."))
        if (includeDebriefing) {
            thread.add(MissionStep.PremiumExperience("SUCCESS DEBRIEFING", "Mission success. Support the guide.", "https://play.google.com/store/apps/details?id=com.example.sakartveloguide"))
        }
        return thread
    }

    private fun calculateTacticalBridge(startNode: BattleNode, endNode: BattleNode, profile: LogisticsProfile, tripCategory: RouteCategory): MissionStep.TacticalBridge {
        val distance = calculateDistanceKm(startNode.location!!, endNode.location!!)
        val hasOwnVehicle = profile.transportType == TransportType.RENTAL_4X4 || profile.transportType == TransportType.OWN_CAR
        val elevationDelta = abs(endNode.elevationMeters - startNode.elevationMeters)
        val isSteep = elevationDelta > 50

        // ARCHITECT'S FIX: Pre-format the distance to prevent string template syntax errors
        val distStr = "%.1f".format(distance)

        val baseMaps = "https://www.google.com/maps/dir/?api=1&origin=${startNode.location.latitude},${startNode.location.longitude}&destination=${endNode.location.latitude},${endNode.location.longitude}"
        val walkUrl = "$baseMaps&travelmode=walking"
        val driveUrl = "$baseMaps&travelmode=driving"
        val busUrl = "$baseMaps&travelmode=transit"
        val boltUrl = "bolt://ride?destination_lat=${endNode.location.latitude}&destination_lng=${endNode.location.longitude}"

        return when {
            distance < 0.2 && (endNode.zoneType == ZoneType.URBAN_CORE || endNode.zoneType == ZoneType.RURAL_HUB) -> {
                MissionStep.TacticalBridge("IMMEDIATE VICINITY", if (hasOwnVehicle) "Park vehicle nearby. Target is pedestrian-only." else "Target is steps away.", walkUrl = walkUrl, distanceKm = distance, primaryMode = "WALK", specialNote = endNode.specialLogisticsNote)
            }
            distance < 2.5 -> {
                if (isSteep && !hasOwnVehicle) {
                    MissionStep.TacticalBridge("ELEVATION ALERT", "Short distance (${distStr}km) but steep ascent (${elevationDelta}m). Taxi advised.", walkUrl = walkUrl, boltUrl = boltUrl, distanceKm = distance, primaryMode = "HYBRID_FOOT", warningTag = "STEEP CLIMB")
                } else if (hasOwnVehicle) {
                    MissionStep.TacticalBridge("SHORT RANGE", "Walkable distance, or reposition vehicle.", walkUrl = walkUrl, driveUrl = driveUrl, distanceKm = distance, primaryMode = "HYBRID_CAR", specialNote = endNode.specialLogisticsNote)
                } else {
                    MissionStep.TacticalBridge("URBAN TRANSIT", "Proceed on foot.", walkUrl = walkUrl, boltUrl = boltUrl, busUrl = busUrl, distanceKm = distance, primaryMode = "WALK", specialNote = endNode.specialLogisticsNote)
                }
            }
            distance < 35.0 -> {
                val isDeadZone = endNode.zoneType == ZoneType.REMOTE_WILDERNESS || endNode.zoneType == ZoneType.ALPINE_PASS
                if (!hasOwnVehicle && isDeadZone) {
                    MissionStep.TacticalBridge("LOGISTICS WARNING", "Destination is in a service dead-zone. Charter a driver.", actionUrl = "https://gotrip.ge", distanceKm = distance, primaryMode = "CHARTER", warningTag = "NO RETURN TAXI")
                } else if (hasOwnVehicle) {
                    MissionStep.TacticalBridge("DRIVE PROTOCOL", "Navigate to ${endNode.title}.", driveUrl = driveUrl, distanceKm = distance, primaryMode = "DRIVE")
                } else {
                    MissionStep.TacticalBridge("RAPID DEPLOYMENT", "Request transport.", busUrl = busUrl, boltUrl = boltUrl, distanceKm = distance, primaryMode = "TAXI")
                }
            }
            else -> {
                MissionStep.TacticalBridge("FLEET NAVIGATION", if(hasOwnVehicle) "Long range transit." else "Fleet required.", driveUrl = if(hasOwnVehicle) driveUrl else null, actionUrl = if(!hasOwnVehicle) "https://gotrip.ge" else null, distanceKm = distance, primaryMode = if(hasOwnVehicle) "DRIVE" else "CHARTER")
            }
        }
    }

    fun onObjectiveSecured() {
        viewModelScope.launch {
            if (_activeStepIndex.value < _missionThread.value.size - 1) {
                val newIndex = _activeStepIndex.value + 1
                _activeStepIndex.value = newIndex
                hapticManager.tick()
                soundManager.playTick()
                preferenceManager.updateStepIndex(newIndex)
            }
        }
    }

    fun onCompleteTrip(trip: TripPath) {
        viewModelScope.launch {
            val loc = try {
                locationManager.getCurrentLocation()
            } catch (e: Exception) { null }

            // USE THE LAST NODE LOCATION AS THE TARGET
            val lastNodeLocation = trip.itinerary.lastOrNull { it.location != null }?.location
                ?: GeoPoint(41.7128, 44.8271) // Tbilisi Fallback

            if (loc == null) {
                Log.e("TACTICAL_ERROR", "Could not acquire GPS lock.")
                _showOutOfRangeDialog.value = true
                return@launch
            }

            val dist = calculateDistanceKm(loc, lastNodeLocation)
            Log.d("ANTI_CHEAT", "Distance to extraction point: ${dist}km")

            // LOGIC: If distance is ridiculously high (>1000km) or
            // if you're actually in-country (radius 500km), allow it.
            if (dist > 500.0) {
                _showOutOfRangeDialog.value = true
                return@launch
            }

            // SUCCESS PROTOCOL
            _stampingTrip.value = trip
            hapticManager.missionCompleteSlam()
            soundManager.playStampSlam()
            addPassportStampUseCase(trip)
            preferenceManager.updateState(UserJourneyState.BROWSING, null)
            preferenceManager.updateStepIndex(0)
        }
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
            preferenceManager.updateStepIndex(0)
            preferenceManager.setHasSeenTutorial(false)
            logisticsProfileManager.saveFullProfile(LogisticsProfile())
            repository.nukeAllData()
            repository.refreshTrips()
            _activeStepIndex.value = 0
            _missionThread.value = emptyList()
            _navigationEvent.emit("home")
            hapticManager.missionCompleteSlam()
        }
    }

    fun onAbortTrip() {
        viewModelScope.launch {
            preferenceManager.updateState(UserJourneyState.BROWSING, null)
            preferenceManager.updateStepIndex(0)
            _pendingLogisticsTripId.value = null
            _initialDestination.value = "home"
            _navigationEvent.emit("home")
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }

    fun onCallFleet(title: String) {}
    fun onOpenBolt() {}
    fun onBookAccommodation(city: String) {}

    fun triggerHapticTick() {
        hapticManager.tick()
    }

    // ARCHITECT'S FIX: Refined Haversine for Anti-Cheat accuracy
    private fun calculateDistanceKm(p1: GeoPoint, p2: GeoPoint): Double {
        val r = 6371.0 // Radius of Earth in KM
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}