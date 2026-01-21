package com.example.sakartveloguide.presentation.builder

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
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.repository.TripRepository
import com.example.sakartveloguide.domain.usecase.SmartArrangeUseCase
import com.example.sakartveloguide.ui.manager.HapticManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context, // Inject context for Intents
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = savedStateHandle.get<String>("tripId") ?: "custom_cargo"
    private val initialIdsStr: String = savedStateHandle.get<String>("ids") ?: ""

    private val _currentStops = MutableStateFlow<List<LocationEntity>>(emptyList())
    private val _tripTitle = MutableStateFlow("MISSION BRIEFING")
    private val _extractionType = MutableStateFlow(ExtractionType.RETURN_TO_FOB)

    val missionState = preferenceManager.missionState
        .stateIn(viewModelScope, SharingStarted.Eagerly, MissionState())

    val uiState: StateFlow<BriefingUiState> = combine(
        _currentStops, missionState, logisticsManager.logisticsProfile, _tripTitle, _extractionType
    ) { stops, mState, profile, title, exType ->
        BriefingUiState(stops, mState.fobLocation, profile, title, exType, profile.entryPoint.getCoordinates())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BriefingUiState())

    init {
        restoreOrLoadData()
    }

    // --- ANTI-CRASH RESTORATION LOGIC ---
    private fun restoreOrLoadData() {
        viewModelScope.launch {
            // 1. Check for Draft
            val draftIds = preferenceManager.draftMission.first()

            if (draftIds.isNotEmpty() && tripId == "custom_cargo") {
                // RESTORE DRAFT
                _currentStops.value = locationDao.getLocationsByIds(draftIds)
                    // Sort by the order in the draft list, not DB order
                    .sortedBy { draftIds.indexOf(it.id) }
                _tripTitle.value = "RESTORED SESSION"
            } else {
                // LOAD FRESH
                loadFreshData()
            }
        }
    }

    private suspend fun loadFreshData() {
        if (tripId == "custom_cargo") {
            val ids = initialIdsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
            _currentStops.value = locationDao.getLocationsByIds(ids)
            _tripTitle.value = "CUSTOM LOADOUT"
        } else {
            val trip = repository.getTripById(tripId)
            _tripTitle.value = trip?.title?.get("en") ?: "FIXED MISSION"
            _currentStops.value = trip?.itinerary?.mapIndexed { index, node ->
                LocationEntity(
                    id = index + 9000,
                    name = node.title.get("en"),
                    region = "Objective", type = "Target", description = node.description.get("en"),
                    latitude = node.location?.latitude ?: 0.0, longitude = node.location?.longitude ?: 0.0, imageUrl = node.imageUrl ?: ""
                )
            } ?: emptyList()
        }
        autoSave()
    }

    // --- TACTICAL ACTIONS ---

    fun optimizeLoadout() {
        val fob = uiState.value.fobLocation ?: uiState.value.airportLocation
        viewModelScope.launch {
            val optimized = smartArrangeUseCase(fob, _currentStops.value)
            _currentStops.value = optimized.toList()
            hapticManager.missionCompleteSlam()
            autoSave()
        }
    }

    // Improved Move Logic for Drag & Drop
    fun moveStop(fromIndex: Int, toIndex: Int) {
        val list = _currentStops.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _currentStops.value = list
            // Note: We do NOT auto-save on every micro-drag frame, only on drop (handled in UI)
        }
    }

    fun onDragComplete() { autoSave() }

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

    // --- EXTERNAL LINKS ---
    fun openExternalLink(type: String) {
        val url = when(type) {
            "skyscanner" -> "https://www.skyscanner.net"
            "booking" -> "https://www.booking.com/city/ge/tbilisi.html"
            "airbnb" -> "https://www.airbnb.com/s/Tbilisi--Georgia"
            "bolt" -> "https://bolt.eu" // Fallback web
            "localrent" -> "https://localrent.com/en/georgia/"
            else -> "https://google.com"
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) { /* safe fail */ }
    }

    private fun autoSave() = viewModelScope.launch {
        preferenceManager.saveDraftMission(_currentStops.value.map { it.id }, _tripTitle.value)
    }

    // --- FINAL EXECUTION ---
    fun finalizeMission(onLaunch: () -> Unit) {
        viewModelScope.launch {
            // FIX: Explicitly grab the IDs from the current UI state
            val finalIds = _currentStops.value.map { it.id }

            // 1. Save Real Mission Data
            preferenceManager.saveActiveLoadout(finalIds)
            preferenceManager.saveExtractionType(_extractionType.value)
            preferenceManager.updateState(UserJourneyState.ON_THE_ROAD, tripId)

            // 2. Clear Anti-Crash Draft (Mission is now live)
            preferenceManager.clearDraft()

            onLaunch()
        }
    }
}