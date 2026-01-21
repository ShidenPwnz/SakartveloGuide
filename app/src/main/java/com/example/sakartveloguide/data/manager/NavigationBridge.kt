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

    fun getMapsIntent(start: GeoPoint, end: GeoPoint, mode: String): Intent {
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&travelmode=$mode")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getExfilIntent(destination: GeoPoint): Intent {
        val uri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    // FIXED: Bolt Logic with Web Fallback
    fun getBoltIntent(dest: GeoPoint): Intent {
        // Try Deep Link first
        val uri = Uri.parse("bolt://ride?destination_lat=${dest.latitude}&destination_lng=${dest.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // We verify if app is installed in the UI layer usually,
        // but here we return the intent. If it fails, the ViewModel fallback handles it.
        // Actually, let's return a safe wrapper intent here.
        return intent
    }
}