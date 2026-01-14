package com.example.sakartveloguide.data.mapper

import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.domain.model.*

fun TripEntity.toDomain(): TripPath {
    return TripPath(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        // ARCHITECT'S FIX: Safe Enum Mapping
        category = try { 
            RouteCategory.valueOf(category) 
        } catch (e: Exception) { 
            RouteCategory.CULTURE // Fallback to a default if DB is out of sync
        },
        difficulty = try { 
            Difficulty.valueOf(difficulty) 
        } catch (e: Exception) { 
            Difficulty.NORMAL 
        },
        totalRideTimeMinutes = totalRideTimeMinutes,
        durationDays = durationDays,
        hasSnowWarning = hasSnowWarning,
        isLocked = isLocked,
        isPremium = isPremium,
        route = route,
        itinerary = itinerary
    )
}