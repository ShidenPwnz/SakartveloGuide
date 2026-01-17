package com.example.sakartveloguide.domain.model

import com.example.sakartveloguide.R

data class TripPath(
    val id: String,
    val title: LocalizedString,
    val description: LocalizedString,
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
    val title: LocalizedString,
    val description: LocalizedString,
    val timeLabel: String,
    val imageUrl: String? = null,
    val alertType: String? = null,
    val location: GeoPoint? = null,
    val zoneType: ZoneType = ZoneType.URBAN_CORE,
    val elevationMeters: Int = 0,
    val isRentalHub: Boolean = false,
    val specialLogisticsNote: String? = null
)

data class LocalizedString(
    val en: String, val ka: String = "", val ru: String = "",
    val tr: String = "", val hy: String = "", val iw: String = "", val ar: String = ""
) {
    fun get(code: String): String = when(code.lowercase()) {
        "ka" -> ka.ifEmpty { en }; "ru" -> ru.ifEmpty { en }
        "tr" -> tr.ifEmpty { en }; "hy" -> hy.ifEmpty { en }
        "iw" -> iw.ifEmpty { en }; "ar" -> ar.ifEmpty { en }
        else -> en
    }
}

fun String.toLocalized() = LocalizedString(en = this)

enum class ZoneType { URBAN_CORE, URBAN_PERIPHERY, RURAL_HUB, REMOTE_WILDERNESS, ALPINE_PASS }
enum class RouteCategory { GUIDE, RELIGIOUS, WINE_CELLAR, WINE_REGION, MOUNTAIN, HIKING, URBAN, URBAN_EXPLORER, COASTAL, HISTORICAL, CULTURE, NATURE, CAPITAL }
enum class Difficulty { RELAXED, NORMAL, EXPLORER, WARRIOR, EASY, MODERATE, HARD }

// Inside TripPath.kt or a standalone file
fun RouteCategory.getLabelRes(): Int {
    return when(this) {
        RouteCategory.GUIDE -> R.string.cat_guide
        RouteCategory.CAPITAL -> R.string.cat_capital
        RouteCategory.MOUNTAIN -> R.string.cat_mountain
        RouteCategory.WINE_REGION, RouteCategory.WINE_CELLAR -> R.string.cat_wine
        RouteCategory.CULTURE, RouteCategory.HISTORICAL -> R.string.cat_culture
        RouteCategory.URBAN, RouteCategory.URBAN_EXPLORER -> R.string.cat_capital
        RouteCategory.NATURE -> R.string.cat_mountain
        RouteCategory.COASTAL -> R.string.cat_culture
        else -> R.string.cat_culture
    }
}
