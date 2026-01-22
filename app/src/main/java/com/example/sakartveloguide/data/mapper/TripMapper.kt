package com.example.sakartveloguide.data.mapper

import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.domain.model.*

fun TripEntity.toDomain(): TripPath {
    return TripPath(
        id = id,
        title = LocalizedString(
            en = titleEn, ka = titleKa, ru = titleRu,
            tr = titleTr, hy = titleHy, iw = titleIw, ar = titleAr
        ),
        description = LocalizedString(
            en = descEn, ka = descKa, ru = descRu,
            tr = descTr, hy = descHy, iw = descIw, ar = descAr
        ),
        imageUrl = imageUrl,
        category = try { RouteCategory.valueOf(category) } catch (e: Exception) { RouteCategory.CULTURE },
        difficulty = try { Difficulty.valueOf(difficulty) } catch (e: Exception) { Difficulty.NORMAL },
        totalRideTimeMinutes = durationDays * 60,
        durationDays = durationDays,
        hasSnowWarning = hasSnowWarning,
        isLocked = isLocked,
        isPremium = isPremium,
        route = emptyList(),
        itinerary = emptyList()
    )
}