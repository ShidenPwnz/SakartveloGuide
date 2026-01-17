package com.example.sakartveloguide.domain.model

import com.example.sakartveloguide.R

data class LogisticsProfile(
    val isByAir: Boolean = true,
    val entryPoint: EntryPoint = EntryPoint.AIRPORT_TBS,
    val exitPoint: EntryPoint = EntryPoint.AIRPORT_TBS,
    val transportType: TransportType = TransportType.RENTAL_4X4,
    val needsAccommodation: Boolean = false,
    val needsEsim: Boolean = false,
    val needsFlight: Boolean = false,
    val needsTransport: Boolean = false,
    val startDate: Long? = null,
    val endDate: Long? = null
)

enum class EntryPoint {
    AIRPORT_TBS, AIRPORT_KUT, AIRPORT_BUS, LAND_TURKEY, LAND_ARMENIA, LAND_AZERBAIJAN, LAND_RUSSIA, CITY_CENTER
}

enum class TransportType(val titleRes: Int, val subRes: Int) {
    RENTAL_4X4(R.string.trans_rental, R.string.trans_rental_sub),
    TAXI(R.string.trans_taxi, R.string.trans_taxi_sub),
    PUBLIC_TRANSPORT(R.string.trans_public, R.string.trans_public_sub),
    OWN_CAR(R.string.trans_own, R.string.trans_own_sub)
}