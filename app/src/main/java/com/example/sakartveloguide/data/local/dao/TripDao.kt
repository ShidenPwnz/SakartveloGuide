package com.example.sakartveloguide.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sakartveloguide.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getTripById(tripId: String): TripEntity?

    @Query("SELECT COUNT(*) FROM trips")
    suspend fun getTripCount(): Int

    // ARCHITECT'S FIX: The Nuclear Option
    @Query("DELETE FROM trips")
    suspend fun nukeTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)

    @Query("UPDATE trips SET isLocked = :locked WHERE id = :tripId")
    suspend fun updateLockStatus(tripId: String, locked: Boolean)
}