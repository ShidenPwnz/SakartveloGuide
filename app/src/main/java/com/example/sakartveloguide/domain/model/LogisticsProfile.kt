package com.example.sakartveloguide.domain.model

import com.example.sakartveloguide.R

data class LogisticsProfile(
    // --- NEW STRATEGIC FIELDS ---
    val transportStrategy: TransportStrategy = TransportStrategy.PASSENGER_URBAN,
    val vehicleStatus: VehicleStatus = VehicleStatus.NONE,

    // --- CORE FIELDS ---
    val entryPoint: EntryPoint = EntryPoint.AIRPORT_TBS,
    val exitPoint: EntryPoint = EntryPoint.AIRPORT_TBS, // Restored
    val startDate: Long? = null,
    val endDate: Long? = null,

    // --- LEGACY / HELPER FIELDS (Restored to fix compilation) ---
    val isByAir: Boolean = true,
    val needsFlight: Boolean = false,
    val needsTransport: Boolean = false,
    val needsAccommodation: Boolean = false,
    val needsEsim: Boolean = false,
    val transportType: TransportType = TransportType.TAXI
)
// ... inside LogisticsProfile.kt or as an extension ...

fun EntryPoint.getCoordinates(): GeoPoint {
    return when (this) {
        EntryPoint.AIRPORT_TBS -> GeoPoint(41.6693, 44.9547)
        EntryPoint.AIRPORT_KUT -> GeoPoint(42.1764, 42.4826)
        EntryPoint.AIRPORT_BUS -> GeoPoint(41.5991, 41.5996)
        else -> GeoPoint(41.7125, 44.7930) // Default Tbilisi Center
    }
}
enum class TransportStrategy {
    PASSENGER_URBAN,   // Taxi/Bolt
    PASSENGER_BUDGET,  // Public Transport
    DRIVER_RENTAL,     // Rental Car
    DRIVER_OWNER       // Own Car
}

enum class VehicleStatus {
    NONE, TO_BE_ACQUIRED, ACTIVE, PARKED_NEARBY
}

enum class EntryPoint {
    AIRPORT_TBS, AIRPORT_KUT, AIRPORT_BUS, LAND_TURKEY, LAND_ARMENIA, LAND_AZERBAIJAN, LAND_RUSSIA, CITY_CENTER
}

enum class TransportType(val titleRes: Int, val subRes: Int) {
    RENTAL_4X4(R.string.trans_rental, R.string.trans_rental_sub),
    TAXI(R.string.trans_taxi, R.string.trans_taxi_sub),
    PUBLIC_TRANSPORT(R.string.trans_public, R.string.trans_public_sub),
    OWN_CAR(R.string.trans_own, R.string.trans_own_sub)
}