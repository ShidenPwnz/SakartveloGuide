package com.example.sakartveloguide.presentation.mission

import androidx.compose.runtime.Composable
import com.example.sakartveloguide.domain.model.GeoPoint

@Composable
fun LiveAssistantScreen(
    currentLocation: GeoPoint,
    destination: GeoPoint?, // Can be null if no active destination
    isNearDestination: Boolean, // New state for proximity
    assistantResponse: String,
    onArrived: () -> Unit
) {
    // Implement your UI here, for now it's a placeholder
}
