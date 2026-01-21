package com.example.sakartveloguide.presentation.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.data.local.PreferenceManager
import com.example.sakartveloguide.domain.model.TripPath
import com.example.sakartveloguide.domain.repository.TripRepository
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

    // ARCHITECT'S FIX: Fetch the specific trip ID to get full 'itinerary' details
    // instead of filtering the 'availableTrips' list which lacks nested data.
    val uiState: StateFlow<PathDetailsUiState> = flow {
        // 1. Get User Language Preference
        val lang = preferenceManager.userSession.first().language

        // 2. Fetch the FULL Trip Data (Heavy Load)
        val trip = repository.getTripById(tripId)

        if (trip != null) {
            emit(mapToUiState(trip, lang))
        } else {
            // Error handling or empty state
            emit(PathDetailsUiState(isLoading = false, title = "Mission Data Unavailable"))
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
            timelineItems = trip.itinerary.mapIndexed { index, node ->
                TimelineUiModel(
                    id = "node_$index",
                    title = node.title.get(lang),
                    shortSummary = node.description.get(lang).take(60),
                    fullDescription = node.description.get(lang),
                    imageUrl = node.imageUrl ?: trip.imageUrl,
                    highlights = listOfNotNull(node.alertType)
                )
            },
            isLoading = false
        )
    }
}