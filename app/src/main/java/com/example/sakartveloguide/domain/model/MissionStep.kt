package com.example.sakartveloguide.domain.model

sealed class MissionStep {
    abstract val title: String
    abstract val description: String
    abstract val actionUrl: String?

    data class AirportProtocol(
        override val title: String = "TOUCHDOWN PROTOCOL",
        override val description: String,
        val entryPoint: EntryPoint,
        override val actionUrl: String? = null
    ) : MissionStep()

    data class AcquireEsim(
        override val title: String = "ACQUIRE COMMS",
        override val description: String = "Establish 4G connectivity via Magti eSIM.",
        override val actionUrl: String
    ) : MissionStep()

    data class LogisticsAnchor(
        override val title: String,
        override val description: String,
        override val actionUrl: String,
        val iconType: String // "BOLT", "RENTAL", "HOTEL"
    ) : MissionStep()

    // ARCHITECT'S ADDITION: High-Yield Experiential Card
    data class PremiumExperience(
        override val title: String = "CULINARY EXPEDITION",
        override val description: String = "Exclusive artisan wine & food experience.",
        override val actionUrl: String
    ) : MissionStep()

    data class Activity(
        val node: BattleNode,
        override val title: String = node.title,
        override val description: String = node.description,
        override val actionUrl: String? = null,
        val taxiBridgeUrl: String? = null
    ) : MissionStep()

    data class Extraction(
        override val title: String = "MISSION EXTRACTION",
        override val description: String = "Request transport back to departure point.",
        override val actionUrl: String? = null
    ) : MissionStep()
}

enum class StepStatus { SECURED, ACTIVE, PLANNED }