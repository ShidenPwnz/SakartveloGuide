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
    val location: GeoPoint? = null // ARCHITECT'S FIX: Enables Smart Transit
)

enum class RouteCategory {
    GUIDE, RELIGIOUS, WINE_CELLAR, WINE_REGION, MOUNTAIN, HIKING, URBAN,
    URBAN_EXPLORER, COASTAL, HISTORICAL, CULTURE, NATURE, CAPITAL
}

enum class Difficulty { RELAXED, NORMAL, EXPLORER, WARRIOR }