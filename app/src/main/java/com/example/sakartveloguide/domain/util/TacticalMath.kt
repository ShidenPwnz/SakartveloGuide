package com.example.sakartveloguide.domain.util

import com.example.sakartveloguide.domain.model.GeoPoint
import kotlin.math.*

object TacticalMath {
    /**
     * Calculates distance in kilometers between two points.
     */
    fun calculateDistanceKm(p1: GeoPoint, p2: GeoPoint): Double {
        val r = 6371.0
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    /**
     * PHASE 27: Country-Level Validation.
     * Roughly covers Georgia's bounding box.
     * Lat: 41.0 to 44.0
     * Lon: 40.0 to 47.0
     */
    fun isUserInOperationalArea(loc: GeoPoint?): Boolean {
        if (loc == null) return false // No GPS = No Stamp
        return loc.latitude in 41.0..44.0 && loc.longitude in 40.0..47.0
    }
}