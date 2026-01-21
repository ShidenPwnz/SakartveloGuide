package com.example.sakartveloguide.presentation.builder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.LogisticsProfileManager
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.data.local.dao.LocationDao
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import com.example.sakartveloguide.domain.usecase.SmartArrangeUseCase
import com.example.sakartveloguide.ui.manager.HapticManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BriefingUiState(
    val stops: List<LocationEntity> = emptyList(),
    val fobLocation: GeoPoint? = null,
    val profile: LogisticsProfile = LogisticsProfile(),
    val tripTitle: String = "PREPARING MISSION",
    val extractionType: ExtractionType = ExtractionType.RETURN_TO_FOB,
    val airportLocation: GeoPoint = GeoPoint(41.6693, 44.9547)
)

@HiltViewModel
class MissionBriefingViewModel @Inject constructor(
    private val repository: TripRepository,
    private val locationDao: LocationDao,
    private val preferenceManager: PreferenceManager,
    private val logisticsManager: LogisticsProfileManager,
    private val smartArrangeUseCase: SmartArrangeUseCase,
    private val hapticManager: HapticManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = savedStateHandle.get<String>("tripId") ?: "custom_cargo"
    private val selectedIdString: String = savedStateHandle.get<String>("ids") ?: ""

    private val _currentStops = MutableStateFlow<List<LocationEntity>>(emptyList())
    private val _tripTitle = MutableStateFlow("MISSION BRIEFING")
    private val _extractionType = MutableStateFlow(ExtractionType.RETURN_TO_FOB)

    val missionState = preferenceManager.missionState
        .stateIn(viewModelScope, SharingStarted.Eagerly, MissionState())

    val uiState: StateFlow<BriefingUiState> = combine(
        _currentStops,
        missionState,
        logisticsManager.logisticsProfile,
        _tripTitle,
        _extractionType
    ) { stops, mState, profile, title, exType ->
        BriefingUiState(
            stops = stops,
            fobLocation = mState.fobLocation,
            profile = profile,
            tripTitle = title,
            extractionType = exType,
            airportLocation = profile.entryPoint.getCoordinates()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BriefingUiState())

    init { loadTacticalData() }

    private fun loadTacticalData() {
        viewModelScope.launch {
            if (tripId == "custom_cargo") {
                // Scenario A: Custom Loadout coming from the Builder Screen
                val ids = selectedIdString.split(",").mapNotNull { it.trim().toIntOrNull() }
                _currentStops.value = locationDao.getLocationsByIds(ids)
                _tripTitle.value = "CUSTOM LOADOUT"
            } else {
                // Scenario B: Fixed Trip Configuration (Intel Migration Logic)
                val trip = repository.getTripById(tripId)
                _tripTitle.value = trip?.title?.get("en") ?: "FIXED MISSION"

                // Map the Domain BattleNodes back to simple LocationEntities for the drag-and-drop list
                _currentStops.value = trip?.itinerary?.mapIndexed { index, node ->
                    LocationEntity(
                        id = index + 9000, // Temporary ID for list stability
                        name = node.title.get("en"),
                        region = "Objective",
                        type = "Target",
                        description = node.description.get("en"),
                        latitude = node.location?.latitude ?: 0.0,
                        longitude = node.location?.longitude ?: 0.0,
                        imageUrl = node.imageUrl ?: ""
                    )
                } ?: emptyList()
            }
        }
    }

    fun optimizeLoadout() {
        // TACTICAL ANCHOR: Smart Arrange starts calculation from the User's Hotel (FOB)
        val fob = uiState.value.fobLocation ?: uiState.value.airportLocation
        viewModelScope.launch {
            val optimized = smartArrangeUseCase(fob, _currentStops.value)
            _currentStops.value = optimized.toList()
            hapticManager.missionCompleteSlam()
        }
    }

    fun moveStop(fromIndex: Int, toIndex: Int) {
        val list = _currentStops.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _currentStops.value = list
            hapticManager.tick()
        }
    }

    fun toggleExtraction() {
        _extractionType.value = if (_extractionType.value == ExtractionType.RETURN_TO_FOB)
            ExtractionType.AIRPORT_EXTRACTION else ExtractionType.RETURN_TO_FOB
        hapticManager.tick()
    }

    fun updateTransport(strategy: TransportStrategy) {
        viewModelScope.launch {
            logisticsManager.saveFullProfile(uiState.value.profile.copy(transportStrategy = strategy))
        }
    }

    fun finalizeMission(onLaunch: () -> Unit) {
        viewModelScope.launch {
            // 1. Capture the EXACT order determined by the user (or Smart Arrange)
            // Note: For Fixed Trips using temp IDs (9000+), this saves the temp IDs.
            // Ideally, we map them back to real IDs if editing is fully supported,
            // but for now, this preserves the sequence for the current session.
            val orderedIds = _currentStops.value.map { it.id }
            preferenceManager.saveActiveLoadout(orderedIds)

            // 2. Capture the Extraction choice
            preferenceManager.saveExtractionType(_extractionType.value)

            // 3. Set Operational State -> Battle Dashboard
            preferenceManager.updateState(UserJourneyState.ON_THE_ROAD, tripId)

            onLaunch()
        }
    }
}