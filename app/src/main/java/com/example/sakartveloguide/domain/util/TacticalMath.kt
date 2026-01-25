package com.example.sakartveloguide.domain.util

import com.example.sakartveloguide.domain.model.GeoPoint
import kotlin.math.*

object TacticalMath {

    /**
     * Standard Haversine (Crow flies)
     */
    fun calculateDirectDistanceKm(p1: GeoPoint, p2: GeoPoint): Double {
        val r = 6371.0
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    /**
     * Legacy Alias for UseCases
     */
    fun calculateDistanceKm(p1: GeoPoint, p2: GeoPoint): Double = calculateDirectDistanceKm(p1, p2)

    /**
     * Applied Winding Factor (1.27x) for Road Distance Estimation
     */
    fun calculateRoadEstimateKm(p1: GeoPoint, p2: GeoPoint): Double {
        val direct = calculateDirectDistanceKm(p1, p2)
        return if (direct < 0.1) direct else direct * 1.27
    }

    fun isUserInOperationalArea(loc: GeoPoint?): Boolean {
        if (loc == null) return false
        return loc.latitude in 41.0..44.0 && loc.longitude in 40.0..47.0
    }
}