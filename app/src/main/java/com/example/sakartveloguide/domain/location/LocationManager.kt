package com.example.sakartveloguide.domain.location

import com.example.sakartveloguide.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationManager {
    suspend fun getCurrentLocation(): GeoPoint?
    fun lastKnownLocation(): GeoPoint?
    fun locationFlow(): Flow<GeoPoint>
    fun stopLocationUpdates()
}