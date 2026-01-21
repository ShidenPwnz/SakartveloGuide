package com.example.sakartveloguide.domain.util

import com.example.sakartveloguide.domain.model.GeoPoint
import kotlin.math.*

object TacticalMath {
    /**
     * Calculates distance in kilometers between two points.
     * Pure logic - zero dependencies on Android framework.
     */
    fun calculateDistanceKm(p1: GeoPoint, p2: GeoPoint): Double {
        val r = 6371.0 // Earth's Radius
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
