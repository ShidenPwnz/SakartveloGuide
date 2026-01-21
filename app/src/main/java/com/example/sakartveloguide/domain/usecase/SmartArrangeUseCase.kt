package com.example.sakartveloguide.domain.usecase

import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.domain.util.TacticalMath
import javax.inject.Inject

class SmartArrangeUseCase @Inject constructor() {

    operator fun invoke(
        startPoint: GeoPoint,
        selectedLocations: List<LocationEntity>
    ): List<LocationEntity> {
        if (selectedLocations.isEmpty()) return emptyList()

        val optimizedList = mutableListOf<LocationEntity>()
        val remainingLocations = selectedLocations.toMutableList()

        var currentPosition = startPoint

        while (remainingLocations.isNotEmpty()) {
            // Use the TacticalMath utility to find the nearest point
            val nearest = remainingLocations.minByOrNull { location ->
                TacticalMath.calculateDistanceKm(
                    currentPosition,
                    GeoPoint(location.latitude, location.longitude)
                )
            }

            nearest?.let {
                optimizedList.add(it)
                currentPosition = GeoPoint(it.latitude, it.longitude)
                remainingLocations.remove(it)
            }
        }

        return optimizedList
    }
}