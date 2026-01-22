package com.example.sakartveloguide.domain.model

import com.example.sakartveloguide.R // CRITICAL IMPORT

data class LogisticsProfile(
    val transportStrategy: TransportStrategy = TransportStrategy.PASSENGER_URBAN,
    val vehicleStatus: VehicleStatus = VehicleStatus.NONE,
    val entryPoint: EntryPoint = EntryPoint.AIRPORT_TBS,
    val exitPoint: EntryPoint = EntryPoint.AIRPORT_TBS,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val isByAir: Boolean = true,
    val needsFlight: Boolean = false,
    val needsTransport: Boolean = false,
    val needsAccommodation: Boolean = false,
    val needsEsim: Boolean = false,
    val transportType: TransportType = TransportType.TAXI
)

fun EntryPoint.getCoordinates(): GeoPoint {
    return when (this) {
        EntryPoint.AIRPORT_TBS -> GeoPoint(41.6693, 44.9547)
        EntryPoint.AIRPORT_KUT -> GeoPoint(42.1764, 42.4826)
        EntryPoint.AIRPORT_BUS -> GeoPoint(41.5991, 41.5996)
        else -> GeoPoint(41.7125, 44.7930)
    }
}

enum class TransportStrategy {
    PASSENGER_URBAN, PASSENGER_BUDGET, DRIVER_RENTAL, DRIVER_OWNER
}

enum class VehicleStatus {
    NONE, TO_BE_ACQUIRED, ACTIVE, PARKED_NEARBY
}

enum class EntryPoint {
    AIRPORT_TBS, AIRPORT_KUT, AIRPORT_BUS, LAND_TURKEY, LAND_ARMENIA, LAND_AZERBAIJAN, LAND_RUSSIA, CITY_CENTER
}

// ARCHITECT'S FIX: Explicitly mapped to verified R.string IDs
enum class TransportType(val titleRes: Int, val subRes: Int) {
    RENTAL_4X4(R.string.trans_driver, R.string.trans_driver_sub),
    TAXI(R.string.trans_passenger, R.string.trans_passenger_sub),
    PUBLIC_TRANSPORT(R.string.trans_passenger, R.string.trans_passenger_sub),
    OWN_CAR(R.string.trans_driver, R.string.trans_driver_sub)
}