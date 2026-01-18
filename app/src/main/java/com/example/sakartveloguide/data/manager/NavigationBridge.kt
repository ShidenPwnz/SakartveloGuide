package com.example.sakartveloguide.data.manager

import android.content.Intent
import android.net.Uri
import com.example.sakartveloguide.domain.model.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class NavigationBridge @Inject constructor() {

    /**
     * Standard Haversine distance math.
     */
    fun calculateDistanceKm(p1: GeoPoint?, p2: GeoPoint?): Double {
        if (p1 == null || p2 == null) return 0.0
        if (p1.latitude == 0.0 && p1.longitude == 0.0) return 0.0

        val r = 6371.0
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    /**
     * Google Maps Multi-Mode Intent.
     */
    fun getMapsIntent(start: GeoPoint, end: GeoPoint, mode: String): Intent {
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1" +
                "&origin=${start.latitude},${start.longitude}" +
                "&destination=${end.latitude},${end.longitude}" +
                "&travelmode=$mode")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
    }

    /**
     * Direct Navigation to FOB.
     */
    fun getExfilIntent(destination: GeoPoint): Intent {
        val uri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
    }

    /**
     * ARCHITECT'S FIX: Bolt Deep Link Protocol.
     * This allows the app to open Bolt with coordinates pre-set.
     */
    fun getBoltIntent(dest: GeoPoint): Intent {
        val uri = Uri.parse("bolt://ride?destination_lat=${dest.latitude}&destination_lng=${dest.longitude}")
        return Intent(Intent.ACTION_VIEW, uri)
    }
}