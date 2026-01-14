package com.example.sakartveloguide.presentation.detail

import com.example.sakartveloguide.domain.model.Difficulty

data class PathDetailsUiState(
    val tripId: String = "",
    val title: String = "",
    val stats: PathStats = PathStats(),
    val timelineItems: List<TimelineUiModel> = emptyList(),
    val isLoading: Boolean = true
)

data class PathStats(
    val driveTime: String = "",
    val intensity: Difficulty = Difficulty.RELAXED,
    val hasSnowWarning: Boolean = false,
    val durationDays: Int = 0 // ARCHITECT'S ADDITION
)

data class TimelineUiModel(
    val id: String,
    val title: String,
    val shortSummary: String,
    val fullDescription: String,
    val imageUrl: String,
    val highlights: List<String>
)