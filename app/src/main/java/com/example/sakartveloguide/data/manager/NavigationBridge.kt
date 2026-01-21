package com.example.sakartveloguide.data.manager

import android.content.Intent
import android.net.Uri
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.domain.util.TacticalMath
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationBridge @Inject constructor() {

    fun calculateDistanceKm(p1: GeoPoint?, p2: GeoPoint?): Double {
        if (p1 == null || p2 == null) return 0.0
        if (p1.latitude == 0.0 && p1.longitude == 0.0) return 0.0
        return TacticalMath.calculateDistanceKm(p1, p2)
    }

    /**
     * Standard Google Maps directions protocol.
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
     * Direct Navigation to FOB protocol.
     */
    fun getExfilIntent(destination: GeoPoint): Intent {
        val uri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
    }

    /**
     * Bolt Taxi deep-link protocol.
     */
    fun getBoltIntent(dest: GeoPoint): Intent {
        val uri = Uri.parse("bolt://ride?destination_lat=${dest.latitude}&destination_lng=${dest.longitude}")
        return Intent(Intent.ACTION_VIEW, uri)
    }
}