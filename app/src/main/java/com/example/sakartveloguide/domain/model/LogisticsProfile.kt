package com.example.sakartveloguide.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.ui.graphics.vector.ImageVector

data class LogisticsProfile(
    val isByAir: Boolean = true,
    val entryPoint: EntryPoint = EntryPoint.AIRPORT_TBS,
    val exitPoint: EntryPoint = EntryPoint.AIRPORT_TBS,
    val transportType: TransportType = TransportType.RENTAL_4X4,
    val needsAccommodation: Boolean = false, // ARCHITECT'S FIX: Default OFF
    val needsEsim: Boolean = false,          // ARCHITECT'S FIX: Default OFF
    val needsFlight: Boolean = false,        // ARCHITECT'S FIX: New Field
    val startDate: Long? = null,
    val endDate: Long? = null

)

enum class EntryPoint(val title: String) {
    AIRPORT_TBS("Tbilisi Airport"),
    AIRPORT_KUT("Kutaisi Airport"),
    AIRPORT_BUS("Batumi Airport"),
    LAND_TURKEY("Turkey Border"),
    LAND_ARMENIA("Armenia Border"),
    LAND_AZERBAIJAN("Azerbaijan Border"),
    LAND_RUSSIA("Lars Border"),
    CITY_CENTER("City Center")
}

enum class TransportType(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    RENTAL_4X4("Rent a Car", "Self-drive 4x4", Icons.Default.DirectionsCar),
    TAXI("Order TAXI", "Ride-hail / Private Driver", Icons.Default.LocalTaxi),
    PUBLIC_TRANSPORT("Public Transport", "Bus / Marshrutka", Icons.Default.DirectionsBus),
    OWN_CAR("Own Vehicle", "I have a car", Icons.Default.TimeToLeave)
}