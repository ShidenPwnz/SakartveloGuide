package com.example.sakartveloguide.domain.model

data class MissionState(
    val tripId: String = "",
    val fobLocation: GeoPoint? = null,
    val completedNodeIndices: Set<Int> = emptySet(),
    val activeNodeIndex: Int? = null
)

// ARCHITECT'S FIX: Global enum for mission progress
enum class TargetStatus { AVAILABLE, ENGAGED, NEUTRALIZED }