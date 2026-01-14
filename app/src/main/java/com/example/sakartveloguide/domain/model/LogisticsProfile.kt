package com.example.sakartveloguide.domain.model

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

enum class TransportType(val title: String, val subtitle: String) {
    RENTAL_4X4("Rent a Car", "Self-drive 4x4"),
    TAXI("Order TAXI", "Ride-hail / Private Driver"),
    PUBLIC_TRANSPORT("Public Transport", "Bus / Marshrutka"),
    OWN_CAR("Own Vehicle", "I have a car")
}