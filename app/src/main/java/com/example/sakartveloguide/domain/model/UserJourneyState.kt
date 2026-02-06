package com.example.sakartveloguide.domain.model

enum class UserJourneyState {
    BROWSING,    // Home Screen
    PATH_LOCKED,  // In the Planner (Editing Mode)
    ON_THE_ROAD, // In the Planner (Live Mode)
    COMPLETED    // Mission Finished
}