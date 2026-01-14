package com.example.sakartveloguide.domain.usecase

import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.domain.model.TripPath
import javax.inject.Inject
import kotlin.math.*

class GetAssistantPromptUseCase @Inject constructor() {

    operator fun invoke(userLoc: GeoPoint, trip: TripPath): String {
        // Destination: Kazbegi center
        val destination = GeoPoint(42.66, 44.64)

        val distanceKm = calculateDistance(userLoc, destination)

        return when {
            distanceKm < 1.5 -> "Welcome to Kazbegi! The air is thin, the peaks are high. Time to hike to Gergeti."
            distanceKm < 15.0 -> "Entering the high pass. Watch for mountain sheep and keep your lights on."
            else -> "On track for ${trip.title}. Enjoy the views of the Aragvi valley."
        }
    }

    /**
     * Standard Haversine formula to calculate distance between two points on Earth.
     * Pure Kotlin - no dependencies on Android or MapLibre.
     */
    private fun calculateDistance(start: GeoPoint, end: GeoPoint): Double {
        val earthRadius = 6371.0 // Kilometers
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
