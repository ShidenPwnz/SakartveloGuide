package com.example.sakartveloguide.domain.model

data class TripPath(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val category: RouteCategory,
    val difficulty: Difficulty,
    val totalRideTimeMinutes: Int,
    val durationDays: Int,
    val hasSnowWarning: Boolean = false,
    val isLocked: Boolean = false,
    val isPremium: Boolean = false,
    val route: List<GeoPoint> = emptyList(),
    val itinerary: List<BattleNode> = emptyList()
)

data class BattleNode(
    val title: String,
    val description: String,
    val timeLabel: String,
    val imageUrl: String? = null,
    val alertType: String? = null,
    val location: GeoPoint? = null,

    // ARCHITECT'S UPGRADE: The Research Variables
    val zoneType: ZoneType = ZoneType.URBAN_CORE, // Default to safest
    val elevationMeters: Int = 0,                 // Verticality
    val isRentalHub: Boolean = false,             // Can I get a car here?
    val specialLogisticsNote: String? = null      // e.g., "Requires 20 Tetri coin"
)

enum class ZoneType {
    URBAN_CORE,
    NATURE,
    RURAL,
    ALPINE,
    COASTAL,// Tbilisi Center, Batumi (Walk/Taxi/Metro)
    URBAN_PERIPHERY,  // Tbilisi Suburbs, Kutaisi (Taxi valid)
    RURAL_HUB,        // Stepantsminda, Signagi (Taxi exists, but limited)
    REMOTE_WILDERNESS,// Martvili, Okatse, Tusheti (NO RETURN TAXI)
    ALPINE_PASS       // Goderdzi, Abano (4x4 ONLY)
}

enum class RouteCategory {
    GUIDE, RELIGIOUS, WINE_CELLAR, WINE_REGION, MOUNTAIN, HIKING, URBAN,
    URBAN_EXPLORER, COASTAL, HISTORICAL, CULTURE, NATURE, CAPITAL
}

enum class Difficulty { RELAXED, NORMAL, EXPLORER, WARRIOR }