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
    val alertType: String? = null
)

// ARCHITECT'S FIX: Expanded Enum to match the Phase 12 Data Injection
enum class RouteCategory {
    RELIGIOUS,
    WINE_CELLAR,
    WINE_REGION, // Added
    MOUNTAIN,
    HIKING,      // Added
    URBAN,
    URBAN_EXPLORER, // Added
    COASTAL,
    HISTORICAL,
    CULTURE,     // Added
    NATURE,
    CAPITAL
}

enum class Difficulty {
    RELAXED,
    NORMAL,
    EXPLORER,
    WARRIOR
}