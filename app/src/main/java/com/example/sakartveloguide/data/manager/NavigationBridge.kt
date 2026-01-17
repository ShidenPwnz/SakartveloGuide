package com.example.sakartveloguide.data.manager

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.domain.model.TransportType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationBridge @Inject constructor() {

    /**
     * Calculates distance in meters between two domain points.
     */
    // Replace the calculateDistanceKm function inside HomeViewModel with this verified syntax:
    private fun calculateDistanceKm(p1: GeoPoint, p2: GeoPoint): Double {
        val r = 6371.0
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(p1.latitude)) * Math.cos(Math.toRadians(p2.latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }


    /**
     * Generates a Google Maps Directions URL (Free/Universal)
     * travelmode: 'driving', 'walking', 'transit', or 'bicycling'
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
     * Generates a Bolt Deep Link (Specific to Georgia/Global)
     * Note: Bolt uses lat/lng in their deep links.
     */
    fun getBoltIntent(dest: GeoPoint): Intent {
        // Bolt deep link structure for setting destination
        val uri = Uri.parse("bolt://ride?destination_lat=${dest.latitude}&destination_lng=${dest.longitude}")
        return Intent(Intent.ACTION_VIEW, uri)
    }
}