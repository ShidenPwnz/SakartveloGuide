package com.example.sakartveloguide.domain.model

data class UserSession(
    val state: UserJourneyState = UserJourneyState.BROWSING,
    val activePathId: String? = null,
    val isProUser: Boolean = false,
    val activeStepIndex: Int = 0,
    val hasSeenTutorial: Boolean = false,
    val language: String = "" // ARCHITECT'S FIX: Empty default prevents premature override
)