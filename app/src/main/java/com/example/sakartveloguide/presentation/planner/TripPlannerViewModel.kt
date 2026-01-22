package com.example.sakartveloguide.presentation.planner

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.LogisticsProfileManager
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.data.local.dao.LocationDao
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.data.manager.NavigationBridge
import com.example.sakartveloguide.domain.location.LocationManager
import com.example.sakartveloguide.domain.model.*
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

    // --- DISCOVERY ENGINE ---
    val searchQuery: String = "",
    val activeCategory: String? = null,
    val nearbyRecs: List<LocationEntity> = emptyList(),
    val regionalRecs: List<LocationEntity> = emptyList(), // Replaces Epic
    val searchResults: List<LocationEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AdventureViewModel @Inject constructor(
    private val repository: TripRepository,
    private val locationDao: LocationDao,
    private val prefs: PreferenceManager,
    private val logisticsManager: LogisticsProfileManager,
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
    private val _tripTitle = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    private val _activeCategory = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _fullInventory = MutableStateFlow<List<LocationEntity>>(emptyList())

    val uiState: StateFlow<TripPlannerState> = combine(
        listOf(
            _route, _tripTitle, prefs.userSession.onStart { emit(UserSession()) },
            prefs.missionState.onStart { emit(MissionState()) },
            logisticsManager.logisticsProfile.onStart { emit(LogisticsProfile()) },
            locationManager.locationFlow().onStart { emit(GeoPoint(0.0, 0.0)) },
            _searchQuery, _isLoading, _activeCategory, _fullInventory
        )
    ) { args ->
        val route = args[0] as List<LocationEntity>
        val title = args[1] as String
        val session = args[2] as UserSession
        val mState = args[3] as MissionState
        val profile = args[4] as LogisticsProfile
        val userLoc = args[5] as GeoPoint
        val query = args[6] as String
        val loading = args[7] as Boolean
        val category = args[8] as String?
        val inventory = args[9] as List<LocationEntity>

        val isLive = session.state == UserJourneyState.ON_THE_ROAD && session.activePathId == tripId
        val baseLoc = mState.fobLocation

        // 1. ROUTE CONSTRUCTION
        val displayRoute = mutableListOf<LocationEntity>()
        if (baseLoc != null) displayRoute.add(createBaseEntity(baseLoc, -1, "HOME (START)"))
        displayRoute.addAll(route)
        if (baseLoc != null && route.isNotEmpty()) displayRoute.add(createBaseEntity(baseLoc, -2, "RETURN HOME"))

        // 2. DISTANCE CHAIN
        val distMap = mutableMapOf<Int, Double>()
        var prevPoint = baseLoc ?: userLoc
        displayRoute.forEach { node ->
            val currPoint = GeoPoint(node.latitude, node.longitude)
            if (node.id != -1 && prevPoint.latitude != 0.0) {
                distMap[node.id] = TacticalMath.calculateDistanceKm(prevPoint, currPoint)
            }
            prevPoint = currPoint
        }

        // 3. DISCOVERY ENGINE (The Filter)
        val currentIds = route.map { it.id }.toSet()
        val availablePool = inventory.filter { it.id !in currentIds } // ARCHITECT'S FIX: Exclude existing

        // Reference: Last stop -> Base -> User
        val proximityRef = route.lastOrNull()?.let { GeoPoint(it.latitude, it.longitude) } ?: baseLoc ?: userLoc

        // SORT BY PROXIMITY
        val proximitySorted = availablePool.sortedBy {
            TacticalMath.calculateDistanceKm(proximityRef, GeoPoint(it.latitude, it.longitude))
        }

        // A. NEARBY GEMS (Top 10 closest)
        val nearby = proximitySorted.take(10)

        // B. REGIONAL HIGHLIGHTS (The next 10 closest - broader region)
        val regional = proximitySorted.drop(10).take(10)

        // C. SEARCH RESULTS (The Vertical List)
        var filteredList = if (query.isEmpty()) availablePool else availablePool.filter {
            it.nameEn.contains(query, true) || it.region.contains(query, true)
        }
        if (category != null) {
            filteredList = filteredList.filter { isLocationInCategory(it, category) }
        }
        // Always sort search results by proximity
        val finalSearchResults = filteredList.sortedBy {
            TacticalMath.calculateDistanceKm(proximityRef, GeoPoint(it.latitude, it.longitude))
        }.take(50)

        // 4. ACTIVE STATE
        val activeId = if (isLive && displayRoute.isNotEmpty()) {
            val idx = mState.activeNodeIndex ?: 0
            if (idx in displayRoute.indices) displayRoute[idx].id else null
        } else null

        TripPlannerState(
            tripId = tripId, title = title, mode = if (isLive) TripMode.LIVE else TripMode.EDITING,
            profile = profile, baseLocation = baseLoc, route = displayRoute, distances = distMap,
            activeNodeId = activeId, completedIds = mState.completedNodeIndices,
            userLocation = if (userLoc.latitude != 0.0) userLoc else null,
            searchQuery = query, activeCategory = category,
            nearbyRecs = nearby, regionalRecs = regional, searchResults = finalSearchResults, isLoading = loading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TripPlannerState(isLoading = true))

    init {
        loadData()
        viewModelScope.launch { locationDao.getAllLocations().collect { _fullInventory.value = it } }
    }

    // ... (Helper Methods) ...
    fun onCategorySelect(cat: String) { if (_activeCategory.value == cat) _activeCategory.value = null else _activeCategory.value = cat; haptic.tick() }
    private fun isLocationInCategory(loc: LocationEntity, cat: String): Boolean {
        return when (cat) {
            "Nature" -> loc.type in listOf("NATURE", "MOUNTAIN", "HIKING", "LAKE", "NATIONAL_PARK")
            "History" -> loc.type in listOf("CULTURE", "HISTORICAL", "RELIGIOUS", "CASTLE")
            "Caves" -> loc.nameEn.contains("Cave", true) || loc.type == "CAVE"
            "Relax" -> loc.type in listOf("RELAXED", "WINE", "WINE_CELLAR", "COASTAL")
            else -> true
        }
    }
    private fun loadData() = viewModelScope.launch {
        _isLoading.value = true
        if (_route.value.isNotEmpty()) { _isLoading.value = false; return@launch }
        val session = prefs.userSession.first()
        if (session.state == UserJourneyState.ON_THE_ROAD && session.activePathId == tripId) {
            val savedIds = prefs.activeLoadout.first()
            if (savedIds.isNotEmpty()) {
                val entities = locationDao.getLocationsByIds(savedIds)
                _route.value = savedIds.mapNotNull { id -> entities.find { it.id == id } }
                _tripTitle.value = repository.getTripById(tripId)?.title?.en ?: "ACTIVE MISSION"
            } else { loadTemplate() }
        } else { loadTemplate() }
        _isLoading.value = false
    }
    private suspend fun loadTemplate() {
        if (tripId == "custom_cargo") {
            if (paramIds.isEmpty()) { _route.value = emptyList(); _tripTitle.value = "NEW ADVENTURE" }
            else { val ids = paramIds.split(",").mapNotNull { it.toIntOrNull() }; _route.value = locationDao.getLocationsByIds(ids); _tripTitle.value = "CUSTOM TRIP" }
        } else {
            val trip = repository.getTripById(tripId)
            _tripTitle.value = trip?.title?.en ?: "ADVENTURE"
            _route.value = trip?.itinerary?.mapIndexed { idx, node -> LocationEntity(id = idx + 10000, nameEn = node.title.en, descEn = node.description.en, imageUrl = node.imageUrl ?: "", latitude = node.location?.latitude ?: 0.0, longitude = node.location?.longitude ?: 0.0, type = "POI", region = "Georgia") } ?: emptyList()
        }
    }
    fun onBackCleanup() = viewModelScope.launch { if (uiState.value.mode == TripMode.EDITING) prefs.clearCurrentMissionData() }
    fun onCardClicked(nodeId: Int) = viewModelScope.launch { if (uiState.value.mode == TripMode.LIVE) { val idx = uiState.value.route.indexOfFirst { it.id == nodeId }; if (idx != -1) { prefs.setActiveTarget(idx); haptic.tick() } } }
    fun optimizeRoute() = viewModelScope.launch { val start = uiState.value.baseLocation ?: GeoPoint(41.7125, 44.7930); _route.value = smartArrangeUseCase(start, _route.value); haptic.tick() }
    fun markCheckIn(nodeId: Int) = viewModelScope.launch { prefs.markTargetComplete(nodeId); val idx = uiState.value.route.indexOfFirst { it.id == nodeId }; if (idx != -1 && idx < uiState.value.route.size - 1) prefs.setActiveTarget(idx + 1) else prefs.setActiveTarget(null); haptic.missionCompleteSlam() }
    private fun createBaseEntity(loc: GeoPoint, id: Int, name: String): LocationEntity { return LocationEntity(id = id, type = "HOME", region = "HQ", latitude = loc.latitude, longitude = loc.longitude, imageUrl = "https://images.pexels.com/photos/271624/pexels-photo-271624.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1", nameEn = name, nameKa = name, nameRu = name, nameTr = name, nameHy = name, nameIw = name, nameAr = name, descEn = "Your secured home base.", descKa = "", descRu = "", descTr = "", descHy = "", descIw = "", descAr = "") }
    fun setBaseCamp(location: GeoPoint) = viewModelScope.launch { prefs.setFobLocation(location); logisticsManager.saveFullProfile(uiState.value.profile.copy(needsAccommodation = false)); haptic.tick() }
    fun toggleMode() = viewModelScope.launch { if (uiState.value.mode == TripMode.EDITING) { val realIds = _route.value.map { it.id }; prefs.saveActiveLoadout(realIds); prefs.updateState(UserJourneyState.ON_THE_ROAD, tripId); prefs.setActiveTarget(0); haptic.missionCompleteSlam() } else { prefs.updateState(UserJourneyState.BROWSING, tripId) } }
    fun onSearchQuery(q: String) { _searchQuery.value = q }
    fun addStop(loc: LocationEntity) { if (!_route.value.any { it.id == loc.id }) { _route.value = _route.value + loc; haptic.tick() } }
    fun removeStop(id: Int) { _route.value = _route.value.filter { it.id != id } }
    fun launchNavigation(dest: GeoPoint, mode: String) { context.startActivity(navigationBridge.getMapsIntent(null, dest, mode)) }
    fun openBookingLink() { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.booking.com/searchresults.html?ss=Georgia")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) }
    fun openFlightLink() { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.skyscanner.net")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) }
}