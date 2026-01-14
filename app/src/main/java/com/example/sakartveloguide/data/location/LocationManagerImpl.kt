// LocationManagerImpl.kt
package com.example.sakartveloguide.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.example.sakartveloguide.domain.location.LocationManager
import com.example.sakartveloguide.domain.model.GeoPoint
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LocationManagerImpl @Inject constructor(
    private val context: Context
) : LocationManager {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun lastKnownLocation(): GeoPoint? {
        // Simple synchronous-like check for immediate use
        return null // In MAD, we prefer the Flow
    }

    @SuppressLint("MissingPermission")
    override fun locationFlow(): Flow<GeoPoint> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(GeoPoint(it.latitude, it.longitude)) }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }

    override fun stopLocationUpdates() { /* Handled by Flow awaitClose */ }
}