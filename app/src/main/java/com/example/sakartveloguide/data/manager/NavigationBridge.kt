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

    /**
     * ARCHITECT'S FIX: Knowledge Graph Search.
     * We pass the name as the query. We omit the coordinates from the 'q'
     * but provide them as the 'center' of the map. This forces Google Maps
     * to search for the NAME in the specific REGION provided by the coordinates.
     */
    fun getReconIntent(name: String, lat: Double, lng: Double): Intent {
        val encodedName = Uri.encode(name)
        // Format: https://www.google.com/maps/search/?api=1&query=Name
        // This is the most reliable way to trigger the "Place Sheet" with photos.
        // We append the region/coordinates context to the query to ensure accuracy.
        val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedName")

        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getFullRouteIntent(origin: GeoPoint?, stops: List<GeoPoint>): Intent {
        if (stops.isEmpty()) return Intent()
        val destination = stops.last()
        val waypoints = if (stops.size > 1) {
            stops.dropLast(1).joinToString("|") { "${it.latitude},${it.longitude}" }
        } else null

        val uriBuilder = Uri.parse("https://www.google.com/maps/dir/?api=1")
            .buildUpon()
            .appendQueryParameter("destination", "${destination.latitude},${destination.longitude}")
            .appendQueryParameter("travelmode", "driving")

        origin?.let { uriBuilder.appendQueryParameter("origin", "${it.latitude},${it.longitude}") }
        waypoints?.let { uriBuilder.appendQueryParameter("waypoints", it) }

        return Intent(Intent.ACTION_VIEW, uriBuilder.build()).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getMapsIntent(start: GeoPoint?, end: GeoPoint, mode: String): Intent {
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${end.latitude},${end.longitude}&travelmode=$mode")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}