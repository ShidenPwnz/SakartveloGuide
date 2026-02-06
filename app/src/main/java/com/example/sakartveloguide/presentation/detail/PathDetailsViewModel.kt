package com.example.sakartveloguide.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.domain.model.TripPath
import com.example.sakartveloguide.domain.repository.TripRepository
import com.example.sakartveloguide.domain.util.getDisplayName
import com.example.sakartveloguide.domain.util.getDisplayDesc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PathDetailsViewModel @Inject constructor(
    private val repository: TripRepository,
    private val preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = savedStateHandle.get<String>("tripId") ?: ""

    val uiState: StateFlow<PathDetailsUiState> = flow {
        // 1. Determine User Language
        val session = preferenceManager.userSession.first()
        val lang = session.language.ifEmpty { "en" }

        // 2. Fetch Full Trip Data (with Itinerary as List<LocationEntity>)
        val trip = repository.getTripById(tripId)

        if (trip != null) {
            emit(mapToUiState(trip, lang))
        } else {
            emit(PathDetailsUiState(isLoading = false, title = "Data Unavailable"))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PathDetailsUiState(isLoading = true)
    )

    private fun mapToUiState(trip: TripPath, lang: String): PathDetailsUiState {
        return PathDetailsUiState(
            tripId = trip.id,
            title = trip.title.get(lang),
            stats = PathStats(
                driveTime = "${trip.totalRideTimeMinutes / 60}h",
                intensity = trip.difficulty,
                hasSnowWarning = trip.hasSnowWarning,
                durationDays = trip.durationDays
            ),
            // ARCHITECT'S FIX: Use LocationEntity helpers for localized mapping
            timelineItems = trip.itinerary.mapIndexed { index, node ->
                TimelineUiModel(
                    id = "node_${node.id}_$index",
                    title = node.getDisplayName(lang),
                    shortSummary = node.getDisplayDesc(lang).take(80) + "...",
                    fullDescription = node.getDisplayDesc(lang),
                    imageUrl = node.imageUrl.ifEmpty { trip.imageUrl },
                    highlights = if (node.isLandmark) listOf("Landmark") else emptyList()
                )
            },
            isLoading = false
        )
    }
}