package com.example.sakartveloguide.domain.model

data class MissionState(
    val tripId: String = "",
    val fobLocation: GeoPoint? = null,
    val completedNodeIndices: Set<Int> = emptySet(),
    val activeNodeIndex: Int? = null,
    val extractionType: ExtractionType = ExtractionType.RETURN_TO_FOB
)

enum class ExtractionType { RETURN_TO_FOB, AIRPORT_EXTRACTION }
enum class TargetStatus { AVAILABLE, ENGAGED, NEUTRALIZED }