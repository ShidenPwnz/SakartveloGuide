package com.example.sakartveloguide.domain.repository

import com.example.sakartveloguide.domain.model.TripPath
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun getAvailableTrips(): Flow<List<TripPath>>
    suspend fun refreshTrips()
    suspend fun lockTrip(tripId: String)
    suspend fun getTripById(id: String): TripPath?
    suspend fun nukeAllData()
    // ARCHITECT'S FIX: Added missing contract method
    suspend fun getTripCount(): Int
}