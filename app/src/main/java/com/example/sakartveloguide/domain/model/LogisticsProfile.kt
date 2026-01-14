package com.example.sakartveloguide.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.ui.graphics.vector.ImageVector

data class LogisticsProfile(
    val entryPoint: EntryPoint = EntryPoint.AIRPORT_TBS,
    val transportType: TransportType = TransportType.RENTAL_4X4,
    val needsAccommodation: Boolean = true,
    val needsEsim: Boolean = false, // NEW: Magti eSIM status
    val startDate: Long? = null,    // NEW: Temporal anchor
    val endDate: Long? = null
)

enum class EntryPoint(val title: String) {
    AIRPORT_TBS("Tbilisi Airport"),
    AIRPORT_KUT("Kutaisi Airport"),
    CITY_CENTER("City Center")
}

enum class TransportType(
    val title: String, 
    val subtitle: String, 
    val icon: ImageVector
) {
    // FIX: Renamed "Rent a 4x4" to "Rent a Car"
    RENTAL_4X4("Rent a Car", "Recommended for this path", Icons.Default.DirectionsCar),
    TAXI("Private Driver", "Relax and ride", Icons.Default.LocalTaxi),
    // FIX: Renamed "Marshrutka" to "Public Transport"
    PUBLIC_TRANSPORT("Public Transport", "Budget friendly bus", Icons.Default.DirectionsBus),
    OWN_CAR("My Own Car", "I have a vehicle", Icons.Default.TimeToLeave)
}
