package com.example.sakartveloguide.domain.model

sealed class MissionStep {
    abstract val title: String
    abstract val description: String
    abstract val actionUrl: String?

    data class TacticalBridge(
        override val title: String = "TRANSIT PROTOCOL",
        override val description: String,
        override val actionUrl: String? = null, // Not used, we use specific urls below
        val walkUrl: String? = null,
        val driveUrl: String? = null,
        val busUrl: String? = null,
        val boltUrl: String? = null,
        val distanceKm: Double,
        val primaryMode: String // "WALK", "DRIVE", "TAXI", "TRANSIT"
    ) : MissionStep()

    // ... Keep other classes (AirportProtocol, Activity, etc.) exactly as they were
    data class AirportProtocol(override val title: String, override val description: String, val entryPoint: EntryPoint, override val actionUrl: String? = null) : MissionStep()
    data class AcquireEsim(override val title: String, override val description: String, override val actionUrl: String) : MissionStep()
    data class LogisticsAnchor(override val title: String, override val description: String, override val actionUrl: String, val iconType: String) : MissionStep()
    data class PremiumExperience(override val title: String, override val description: String, override val actionUrl: String) : MissionStep()
    data class Activity(val node: BattleNode, override val title: String = node.title, override val description: String = node.description, override val actionUrl: String? = null) : MissionStep()
    data class Extraction(override val title: String, override val description: String, override val actionUrl: String? = null) : MissionStep()
}

enum class StepStatus { SECURED, ACTIVE, PLANNED }