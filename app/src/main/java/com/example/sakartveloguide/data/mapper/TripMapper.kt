package com.example.sakartveloguide.data.mapper

import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.domain.model.*

fun TripEntity.toDomain(): TripPath {
    return TripPath(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        category = try { RouteCategory.valueOf(category) } catch (e: Exception) { RouteCategory.CULTURE },
        difficulty = try { Difficulty.valueOf(difficulty) } catch (e: Exception) { Difficulty.NORMAL },
        totalRideTimeMinutes = totalRideTimeMinutes,
        durationDays = durationDays,
        hasSnowWarning = hasSnowWarning,
        isLocked = isLocked,
        isPremium = isPremium,
        route = route,
        itinerary = itinerary
    )
}