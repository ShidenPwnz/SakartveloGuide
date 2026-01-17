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
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = savedStateHandle.get<String>("tripId") ?: ""

    val uiState: StateFlow<PathDetailsUiState> = preferenceManager.userSession
        .map { it.language }
        .distinctUntilChanged()
        .combine(repository.getAvailableTrips()) { lang, trips ->
            val trip = trips.find { it.id == tripId }
            if (trip != null) {
                Log.d("INTEL_SYNC", "Found Trip: ${trip.id} for Language: $lang")
                mapToUiState(trip, lang)
            } else {
                Log.e("INTEL_SYNC", "CRITICAL: Trip ID $tripId not found in repository.")
                PathDetailsUiState(isLoading = true)
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
                    shortSummary = if (node.description.get(lang).length > 60) {
                        node.description.get(lang).take(60) + "..."
                    } else {
                        node.description.get(lang)
                    },
                    fullDescription = node.description.get(lang),
                    imageUrl = node.imageUrl ?: trip.imageUrl,
                    highlights = listOfNotNull(node.alertType)
                )
            },
            isLoading = false
        )
    }
}