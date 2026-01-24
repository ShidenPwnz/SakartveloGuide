package com.example.sakartveloguide.presentation.planner

import android.content.Context
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
        val proximitySorted = inventory.filter { loc -> !route.any { it.id == loc.id } }
            .sortedBy { TacticalMath.calculateDistanceKm(proximityRef, GeoPoint(it.latitude, it.longitude)) }

        val finalSearchResults = if (query.isEmpty()) proximitySorted else {
            proximitySorted.filter { loc ->
                loc.nameEn.contains(query, true) || loc.nameKa.contains(query, true) ||
                        loc.nameRu.contains(query, true) || loc.region.contains(query, true)
            }
        }

        val distMap = mutableMapOf<Int, Double>()
        var prevPoint = baseLoc ?: actualUserLoc
        route.forEach { node ->
            val curr = GeoPoint(node.latitude, node.longitude)
            distMap[node.id] = TacticalMath.calculateDistanceKm(prevPoint, curr)
            prevPoint = curr
        }
        if (baseLoc != null && route.isNotEmpty()) distMap[-2] = TacticalMath.calculateDistanceKm(prevPoint, baseLoc)

        val activeId = if (isLive) {
            val idx = mState.activeNodeIndex ?: 0
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
        loadInitialData()
        viewModelScope.launch { locationDao.getAllLocations().collect { _fullInventory.value = it } }
    }

    private fun loadInitialData() = viewModelScope.launch {
        _isLoading.value = true
        val trip = repository.getTripById(tripId)
        _localizedTitleData.value = trip?.title
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
        _isLoading.value = false
    }

    fun toggleMode() = viewModelScope.launch {
        if (uiState.value.mode == TripMode.EDITING) {
            prefs.saveActiveLoadout(_route.value.map { it.id })
            prefs.updateState(UserJourneyState.ON_THE_ROAD, tripId)
            prefs.setActiveTarget(0)
        } else {
            prefs.updateState(UserJourneyState.BROWSING, tripId)
        }
    }

    fun completeMission() = viewModelScope.launch {
        val currentState = uiState.value
        haptic.missionCompleteSlam()
        _showSlamAnimation.value = true
        val stamp = PassportEntity(
            regionId = tripId + "_" + System.currentTimeMillis(),
            regionName = currentState.title,
            dateUnlocked = System.currentTimeMillis(),
            tripTitle = currentState.title
        )
        passportRepository.addStamp(stamp)
        prefs.updateState(UserJourneyState.BROWSING, null)
        prefs.clearCurrentMissionData()
    }

    fun onSlamAnimationComplete() = viewModelScope.launch {
        _showSlamAnimation.value = false
        _navigationEvent.emit("passport")
    }

    fun onBackCleanup() = viewModelScope.launch { if (uiState.value.mode == TripMode.EDITING) prefs.clearCurrentMissionData() }
    fun onCardClicked(nodeId: Int) = viewModelScope.launch {
        val idx = when (nodeId) { -1 -> -1; -2 -> uiState.value.route.size; -3 -> uiState.value.route.size; else -> uiState.value.route.indexOfFirst { it.id == nodeId } }
        prefs.setActiveTarget(idx)
        haptic.tick()
    }
    fun markCheckIn(nodeId: Int) = viewModelScope.launch {
        if (nodeId > 0) prefs.markTargetComplete(nodeId)
        val currentIdx = uiState.value.route.indexOfFirst { it.id == nodeId }
        prefs.setActiveTarget(currentIdx + 1)
        haptic.missionCompleteSlam()
    }
    fun onSearchQuery(q: String) { _searchQuery.value = q }
    fun addStop(loc: LocationEntity) { if (!_route.value.any { it.id == loc.id }) _route.value = _route.value + loc }
    fun removeStop(id: Int) { _route.value = _route.value.filter { it.id != id } }
    fun launchNavigation(dest: GeoPoint, mode: String) { context.startActivity(navigationBridge.getMapsIntent(null, dest, mode)) }
    fun onStayAction(p: String) { if (p == "airbnb") affiliateManager.launchAirbnb() else affiliateManager.launchBooking() }
    fun onTransportAction(p: String) { affiliateManager.launchBolt(uiState.value.baseLocation) }
    fun onFlightAction(p: String) { affiliateManager.launchSkyscanner() }
    fun setBaseCamp(location: GeoPoint) = viewModelScope.launch { prefs.setFobLocation(location) }
    fun optimizeRoute() = viewModelScope.launch { _route.value = smartArrangeUseCase(uiState.value.baseLocation ?: GeoPoint(41.7, 44.8), _route.value); haptic.tick() }
}