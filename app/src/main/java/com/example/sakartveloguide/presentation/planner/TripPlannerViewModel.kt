package com.example.sakartveloguide.presentation.planner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.LogisticsProfileManager
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.data.local.dao.LocationDao
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.data.local.entity.PassportEntity
import com.example.sakartveloguide.data.manager.AffiliateManager
import com.example.sakartveloguide.data.manager.NavigationBridge
import com.example.sakartveloguide.domain.location.LocationManager
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.PassportRepository
import com.example.sakartveloguide.domain.repository.TripRepository
import com.example.sakartveloguide.domain.usecase.SmartArrangeUseCase
import com.example.sakartveloguide.domain.util.TacticalMath
import com.example.sakartveloguide.ui.manager.HapticManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TripMode { EDITING, LIVE }

data class TripPlannerState(
    val tripId: String = "",
    val title: String = "",
    val mode: TripMode = TripMode.EDITING,
    val profile: LogisticsProfile = LogisticsProfile(),
    val baseLocation: GeoPoint? = null,
    val route: List<LocationEntity> = emptyList(),
    val distances: Map<Int, Double> = emptyMap(),
    val activeNodeId: Int? = null,
    val completedIds: Set<Int> = emptySet(),
    val userLocation: GeoPoint? = null,
    val searchQuery: String = "",
    val nearbyRecs: List<LocationEntity> = emptyList(),
    val searchResults: List<LocationEntity> = emptyList(),
    val isLoading: Boolean = true,
    val showSlamAnimation: Boolean = false
)

@HiltViewModel
class AdventureViewModel @Inject constructor(
    private val repository: TripRepository,
    private val passportRepository: PassportRepository,
    private val locationDao: LocationDao,
    private val prefs: PreferenceManager,
    private val logisticsManager: LogisticsProfileManager,
    private val affiliateManager: AffiliateManager,
    private val smartArrangeUseCase: SmartArrangeUseCase,
    private val haptic: HapticManager,
    private val locationManager: LocationManager,
    private val navigationBridge: NavigationBridge,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId = savedStateHandle.get<String>("tripId") ?: ""
    private val paramIds = savedStateHandle.get<String>("ids") ?: ""

    private val _route = MutableStateFlow<List<LocationEntity>>(emptyList())
    private val _localizedTitleData = MutableStateFlow<LocalizedString?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    private val _fullInventory = MutableStateFlow<List<LocationEntity>>(emptyList())
    private val _showSlamAnimation = MutableStateFlow(false)

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    val userSession: Flow<UserSession> = prefs.userSession

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<TripPlannerState> = combine(
        _route, _localizedTitleData, prefs.userSession, prefs.missionState,
        logisticsManager.logisticsProfile, locationManager.locationFlow().onStart { emit(GeoPoint(41.7125, 44.7930)) },
        _searchQuery, _isLoading, _fullInventory, _showSlamAnimation
    ) { args ->
        val route = args[0] as List<LocationEntity>
        val locTitle = args[1] as? LocalizedString
        val session = args[2] as UserSession
        val mState = args[3] as MissionState
        val profile = args[4] as LogisticsProfile
        val userLoc = args[5] as GeoPoint
        val query = args[6] as String
        val loading = args[7] as Boolean
        val inventory = args[8] as List<LocationEntity>
        val showSlam = args[9] as Boolean

        val isLive = session.state == UserJourneyState.ON_THE_ROAD && session.activePathId == tripId
        val baseLoc = mState.fobLocation
        val actualUserLoc = if (userLoc.latitude != 0.0) userLoc else GeoPoint(41.7125, 44.7930)

        val proximityRef = route.lastOrNull()?.let { GeoPoint(it.latitude, it.longitude) } ?: baseLoc ?: actualUserLoc
        val proximitySorted = inventory.filter { it.id !in route.map { r -> r.id } }
            .sortedBy { TacticalMath.calculateDistanceKm(proximityRef, GeoPoint(it.latitude, it.longitude)) }

        val finalSearchResults = if (query.isEmpty()) proximitySorted else {
            proximitySorted.filter { loc ->
                loc.nameEn.contains(query, true) || loc.nameKa.contains(query, true) ||
                        loc.nameRu.contains(query, true) || loc.region.contains(query, true)
            }
        }

        val distMap = mutableMapOf<Int, Double>()
        if (baseLoc != null) distMap[-1] = TacticalMath.calculateRoadEstimateKm(actualUserLoc, baseLoc)
        var prevPoint = baseLoc ?: actualUserLoc
        route.forEach { node ->
            val curr = GeoPoint(node.latitude, node.longitude)
            distMap[node.id] = TacticalMath.calculateRoadEstimateKm(prevPoint, curr)
            prevPoint = curr
        }
        if (baseLoc != null && route.isNotEmpty()) distMap[-2] = TacticalMath.calculateRoadEstimateKm(prevPoint, baseLoc)

        val activeId = if (isLive) {
            val idx = mState.activeNodeIndex ?: (if (baseLoc != null) -1 else 0)
            if (idx == -1) -1
            else if (idx in route.indices) route[idx].id
            else if (idx == route.size) { if (baseLoc != null) -2 else -3 }
            else null
        } else null

        TripPlannerState(
            tripId = tripId, title = locTitle?.get(session.language) ?: "ADVENTURE",
            mode = if (isLive) TripMode.LIVE else TripMode.EDITING,
            profile = profile, baseLocation = baseLoc, route = route, distances = distMap,
            activeNodeId = activeId, completedIds = mState.completedNodeIndices,
            userLocation = actualUserLoc, searchQuery = query,
            nearbyRecs = proximitySorted.take(15), searchResults = finalSearchResults.take(60),
            isLoading = loading, showSlamAnimation = showSlam
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TripPlannerState(isLoading = true))

    init {
        viewModelScope.launch { locationDao.getAllLocations().collect { _fullInventory.value = it } }
        loadDataWithRecovery()
    }

    private fun loadDataWithRecovery() {
        viewModelScope.launch {
            _isLoading.value = true
            val trip = repository.getTripById(tripId)
            _localizedTitleData.value = trip?.title
            val session = prefs.userSession.first()
            val savedLoadout = prefs.activeLoadout.first()
            if (savedLoadout.isNotEmpty() && (session.activePathId == tripId || tripId == "custom_cargo")) {
                val dbEntities = locationDao.getLocationsByIds(savedLoadout.filter { it < 10000 })
                val templateEntities = trip?.itinerary?.mapIndexed { idx, node ->
                    LocationEntity(
                        id = idx + 10000, nameEn = node.title.en, descEn = node.description.en,
                        imageUrl = node.imageUrl ?: "", latitude = node.location?.latitude ?: 0.0,
                        longitude = node.location?.longitude ?: 0.0, type = "POI", region = "Georgia"
                    )
                } ?: emptyList()
                _route.value = savedLoadout.mapNotNull { id ->
                    if (id >= 10000) templateEntities.find { it.id == id }
                    else dbEntities.find { it.id == id }
                }
            } else { loadTemplate(trip) }
            _isLoading.value = false
        }
    }

    private suspend fun loadTemplate(trip: TripPath?) {
        if (tripId == "custom_cargo") {
            val ids = paramIds.split(",").mapNotNull { it.toIntOrNull() }
            _route.value = locationDao.getLocationsByIds(ids)
            _localizedTitleData.value = LocalizedString(en="CUSTOM TRIP", ka="ინდივიდუალური ტური", ru="ИНДИВИДУАЛЬНЫЙ ТУР")
        } else {
            _route.value = trip?.itinerary?.mapIndexed { idx, node ->
                LocationEntity(
                    id = idx + 10000, nameEn = node.title.en, descEn = node.description.en,
                    imageUrl = node.imageUrl ?: "", latitude = node.location?.latitude ?: 0.0,
                    longitude = node.location?.longitude ?: 0.0, type = "POI", region = "Georgia"
                )
            } ?: emptyList()
        }
    }

    /**
     * ARCHITECT'S REFINED DIRECTION LOGIC
     * Handles "No Home" scenario by shifting PoI 1 to the Origin.
     */
    fun launchFullTripIntent() {
        val state = uiState.value
        val completed = state.completedIds

        // Filter out visited POIs
        val unvisitedStops = state.route.filter { it.id !in completed }

        // --- 1. DETERMINE ORIGIN ---
        val origin: String = when {
            // Case A: Home is set and unvisited
            state.baseLocation != null && -1 !in completed ->
                "${state.baseLocation.latitude},${state.baseLocation.longitude}"

            // Case B: No Home (or visited), use the first unvisited POI as the starting point
            unvisitedStops.isNotEmpty() ->
                "${unvisitedStops.first().latitude},${unvisitedStops.first().longitude}"

            // Case C: Absolute fallback (Current GPS)
            else -> state.userLocation?.let { "${it.latitude},${it.longitude}" } ?: "Current+Location"
        }

        // --- 2. DETERMINE DESTINATION ---
        val destination: String = when {
            // Priority 1: If "Return Home" is unvisited
            state.baseLocation != null && -2 !in completed && unvisitedStops.isEmpty() ->
                "${state.baseLocation.latitude},${state.baseLocation.longitude}"

            // Priority 2: The last unvisited POI
            unvisitedStops.size > 1 ->
                "${unvisitedStops.last().latitude},${unvisitedStops.last().longitude}"

            // Priority 3: If only one POI remains and we started from Home
            unvisitedStops.size == 1 && origin != "${unvisitedStops.first().latitude},${unvisitedStops.first().longitude}" ->
                "${unvisitedStops.first().latitude},${unvisitedStops.first().longitude}"

            else -> origin // Trip already essentially finished
        }

        // --- 3. DETERMINE WAYPOINTS (The "Stops" in between) ---
        // We take everything between the chosen Origin and the chosen Destination
        val waypointsList = unvisitedStops.filter {
            val coord = "${it.latitude},${it.longitude}"
            coord != origin && coord != destination
        }

        val waypoints = waypointsList.joinToString("|") { "${it.latitude},${it.longitude}" }

        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$destination&waypoints=$waypoints&travelmode=driving")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { context.startActivity(intent) } catch (e: Exception) { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    }

    fun toggleMode() = viewModelScope.launch {
        if (uiState.value.mode == TripMode.EDITING) {
            prefs.saveActiveLoadout(_route.value.map { it.id })
            prefs.updateState(UserJourneyState.ON_THE_ROAD, tripId)
            prefs.setActiveTarget(if (uiState.value.baseLocation != null) -1 else 0)
        } else { prefs.updateState(UserJourneyState.BROWSING, tripId) }
    }

    fun markCheckIn(nodeId: Int) = viewModelScope.launch {
        prefs.markTargetComplete(nodeId)
        val nextIdx = if (nodeId == -1) 0 else uiState.value.route.indexOfFirst { it.id == nodeId } + 1
        prefs.setActiveTarget(nextIdx)
        haptic.missionCompleteSlam()
    }

    fun onCardClicked(nodeId: Int) = viewModelScope.launch {
        val idx = when (nodeId) { -1 -> -1; -2 -> uiState.value.route.size; -3 -> uiState.value.route.size; else -> uiState.value.route.indexOfFirst { it.id == nodeId } }
        prefs.setActiveTarget(idx)
        haptic.tick()
    }

    fun completeMission() = viewModelScope.launch {
        val currentState = uiState.value
        haptic.missionCompleteSlam()
        _showSlamAnimation.value = true
        val stamp = PassportEntity(tripId + "_" + System.currentTimeMillis(), currentState.title, System.currentTimeMillis(), currentState.title)
        passportRepository.addStamp(stamp)
        prefs.updateState(UserJourneyState.BROWSING, null)
        prefs.clearCurrentMissionData()
    }

    fun onSlamAnimationComplete() = viewModelScope.launch { _showSlamAnimation.value = false; _navigationEvent.emit("passport") }
    fun onBackCleanup() = viewModelScope.launch { if (uiState.value.mode == TripMode.EDITING) prefs.clearCurrentMissionData() }
    fun onSearchQuery(q: String) { _searchQuery.value = q }
    fun addStop(loc: LocationEntity) { if (!_route.value.any { it.id == loc.id }) _route.value = _route.value + loc }
    fun removeStop(id: Int) { _route.value = _route.value.filter { it.id != id } }
    fun launchNavigation(dest: GeoPoint, mode: String) { context.startActivity(navigationBridge.getMapsIntent(null, dest, mode)) }
    fun onStayAction(p: String) { if (p == "airbnb") affiliateManager.launchAirbnb() else affiliateManager.launchBooking() }
    fun onTransportAction(p: String) { affiliateManager.launchBolt(uiState.value.baseLocation) }
    fun onFlightAction(p: String) { affiliateManager.launchSkyscanner() }
    fun onRentCarAction() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://localrent.com/en/georgia/?r=6716"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    fun setBaseCamp(location: GeoPoint) = viewModelScope.launch { prefs.setFobLocation(location) }
    fun optimizeRoute() = viewModelScope.launch { _route.value = smartArrangeUseCase(uiState.value.baseLocation ?: GeoPoint(41.7, 44.8), _route.value); haptic.tick() }
}