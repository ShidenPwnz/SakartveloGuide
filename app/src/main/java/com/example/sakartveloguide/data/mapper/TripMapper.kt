package com.example.sakartveloguide.data.mapper

import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.domain.model.*

fun TripEntity.toDomain(): TripPath {
    return TripPath(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        category = RouteCategory.valueOf(category),
        difficulty = Difficulty.valueOf(difficulty),
        totalRideTimeMinutes = totalRideTimeMinutes,
        durationDays = durationDays,
        hasSnowWarning = hasSnowWarning,
        isLocked = isLocked,
        isPremium = isPremium,
        route = route,
        itinerary = itinerary
    )
}
