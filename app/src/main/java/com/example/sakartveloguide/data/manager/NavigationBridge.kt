package com.example.sakartveloguide.data.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.domain.util.TacticalMath
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationBridge @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun calculateDistanceKm(p1: GeoPoint?, p2: GeoPoint?): Double {
        if (p1 == null || p2 == null) return 0.0
        return TacticalMath.calculateDistanceKm(p1, p2)
    }

    fun getMapsIntent(start: GeoPoint?, end: GeoPoint, mode: String): Intent {
        // ARCHITECT'S FIX: Use the universal web-dir URI.
        // Leaving 'origin' empty or using 'Current+Location' forces GPS start.
        val transportMode = if (mode == "walking") "w" else "d"
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${end.latitude},${end.longitude}&travelmode=$mode")

        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getExfilIntent(destination: GeoPoint): Intent {
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${destination.latitude},${destination.longitude}")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getBoltIntent(dest: GeoPoint): Intent {
        val uri = Uri.parse("bolt://ride?destination_lat=${dest.latitude}&destination_lng=${dest.longitude}")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}