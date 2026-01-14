package com.example.sakartveloguide.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.sakartveloguide.domain.location.LocationManager
import com.example.sakartveloguide.domain.model.GeoPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationManagerImpl @Inject constructor(
    private val context: Context
) : LocationManager {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): GeoPoint? = suspendCancellableCoroutine { continuation ->
        val cts = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    continuation.resume(GeoPoint(location.latitude, location.longitude))
                } else {
                    continuation.resume(null)
                }
            }
            .addOnFailureListener {
                continuation.resume(null)
            }

        continuation.invokeOnCancellation { cts.cancel() }
    }

    override fun lastKnownLocation(): GeoPoint? = null
    override fun locationFlow(): Flow<GeoPoint> = MutableStateFlow(GeoPoint(0.0, 0.0))
    override fun stopLocationUpdates() {}
}