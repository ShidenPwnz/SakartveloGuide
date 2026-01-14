package com.example.sakartveloguide.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakartveloguide.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PathDetailsViewModel @Inject constructor(
    private val repository: TripRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PathDetailsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val tripId = savedStateHandle.get<String>("tripId") ?: return@launch
            val trips = repository.getAvailableTrips().first()
            val trip = trips.find { it.id == tripId }

            if (trip != null) {
                _uiState.value = PathDetailsUiState(
                    tripId = trip.id,
                    title = trip.title,
                    stats = PathStats(
                        driveTime = "${trip.totalRideTimeMinutes / 60}h",
                        intensity = trip.difficulty,
                        hasSnowWarning = trip.hasSnowWarning,
                        ),
                    // Mapping the domain itinerary to the UI timeline model
                    timelineItems = trip.itinerary.mapIndexed { index, node ->
                        TimelineUiModel(
                            id = "day_$index",
                            title = node.title,
                            shortSummary = node.description.take(50) + "...",
                            fullDescription = node.description,
                            imageUrl = node.imageUrl ?: trip.imageUrl,
                            highlights = listOfNotNull(node.alertType)
                        )
                    },
                    isLoading = false
                )
            }
        }
    }
}