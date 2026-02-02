package com.example.sakartveloguide.domain.usecase

import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.domain.util.TacticalMath
import javax.inject.Inject

class GetSmartRecommendationsUseCase @Inject constructor() {

    // TACTICAL WEIGHTS
    private val WEIGHT_PRIORITY = 25.0    // Quality Signal
    private val WEIGHT_POPULARITY = 0.5   // Social Signal (0-100)
    private val BONUS_LANDMARK = 50.0     // "Must See" Status
    private val BONUS_REGION = 30.0       // Relevance to Base
    private val PENALTY_DISTANCE = 4.0    // Cost per KM

    operator fun invoke(
        referencePoint: GeoPoint,
        baseRegion: String?,
        allLocations: List<LocationEntity>,
        currentRouteIds: Set<Int>,
        limit: Int = 20
    ): List<LocationEntity> {

        return allLocations
            .filter { it.id !in currentRouteIds } // Exclusion Protocol
            .filter { it.priority > 0 }           // Noise Filter
            .map { location ->
                val distance = TacticalMath.calculateDistanceKm(
                    referencePoint,
                    GeoPoint(location.latitude, location.longitude)
                )

                val score = calculateScore(location, distance, baseRegion)
                location to score
            }
            .sortedByDescending { it.second } // Rank by Score
            .take(limit)
            .map { it.first }
    }

    private fun calculateScore(
        loc: LocationEntity,
        distance: Double,
        targetRegion: String?
    ): Double {
        var score = 0.0

        // 1. Base Value
        score += loc.priority * WEIGHT_PRIORITY
        score += loc.popularity * WEIGHT_POPULARITY

        // 2. Tactical Bonuses
        if (loc.isLandmark) score += BONUS_LANDMARK
        if (targetRegion != null && loc.region.equals(targetRegion, ignoreCase = true)) {
            score += BONUS_REGION
        }

        // 3. Distance Decay (Logistics Cost)
        // We ignore the first 1km (local vicinity is free)
        val penaltyDist = if (distance < 1.0) 0.0 else distance
        score -= (penaltyDist * PENALTY_DISTANCE)

        return score
    }
}