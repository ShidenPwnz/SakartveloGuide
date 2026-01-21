package com.example.sakartveloguide.data.mapper

import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.domain.model.*

fun TripEntity.toDomain(): TripPath {
    return TripPath(
        id = id,
        // FIX 1: Convert simple Strings to LocalizedString (English default)
        title = LocalizedString(en = title),
        description = LocalizedString(en = description),
        imageUrl = imageUrl,
        category = try { RouteCategory.valueOf(category) } catch (e: Exception) { RouteCategory.CULTURE },
        difficulty = try { Difficulty.valueOf(difficulty) } catch (e: Exception) { Difficulty.NORMAL },

        // FIX 2: Estimate missing fields or set defaults
        totalRideTimeMinutes = durationDays * 60, // Rough estimate based on days
        durationDays = durationDays,
        hasSnowWarning = hasSnowWarning,
        isLocked = isLocked,
        isPremium = isPremium,

        // FIX 3: Return empty lists.
        // The Repository fills these in using the IDs when 'loadFullDetails' is true.
        route = emptyList(),
        itinerary = emptyList()
    )
}