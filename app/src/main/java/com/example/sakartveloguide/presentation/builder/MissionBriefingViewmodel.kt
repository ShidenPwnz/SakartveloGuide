package com.example.sakartveloguide.presentation.builder

import android.util.Log
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
    val extractionType: ExtractionType = ExtractionType.RETURN_TO_FOB
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

    // Hot flow for FOB updates
    val missionState = preferenceManager.missionState
        .stateIn(viewModelScope, SharingStarted.Eagerly, MissionState())

    val uiState: StateFlow<BriefingUiState> = combine(
        _currentStops,
        missionState,
        logisticsManager.logisticsProfile,
        _tripTitle,
        _extractionType
    ) { stops, mState, profile, title, exType ->
        BriefingUiState(stops, mState.fobLocation, profile, title, exType)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BriefingUiState())

    init { loadTacticalData() }

    // ... inside MissionBriefingViewModel ...

    private fun loadTacticalData() {
        viewModelScope.launch {
            if (tripId == "custom_cargo") {
                val ids = selectedIdString.split(",").mapNotNull { it.trim().toIntOrNull() }
                _currentStops.value = locationDao.getLocationsByIds(ids)
                _tripTitle.value = "CUSTOM LOADOUT"
            } else {
                val trip = repository.getTripById(tripId)
                _tripTitle.value = trip?.title?.get("en") ?: "FIXED MISSION"

                // ARCHITECT'S FIX: Ensure these have IDs so SmartArrange can track them
                _currentStops.value = trip?.itinerary?.mapIndexed { index, node ->
                    LocationEntity(
                        id = index, // Assign temporary ID for sorting
                        name = node.title.get("en"), region = "", type = "",
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
        val fob = uiState.value.fobLocation ?: return
        viewModelScope.launch {
            val optimized = smartArrangeUseCase(fob, _currentStops.value)
            _currentStops.value = optimized.toList() // Force UI update
            hapticManager.missionCompleteSlam()
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

    fun finalizeMission(onSuccess: () -> Unit) {
        viewModelScope.launch {
            preferenceManager.saveActiveLoadout(_currentStops.value.map { it.id })
            preferenceManager.updateState(UserJourneyState.ON_THE_ROAD, tripId)
            onSuccess()
        }
    }
}