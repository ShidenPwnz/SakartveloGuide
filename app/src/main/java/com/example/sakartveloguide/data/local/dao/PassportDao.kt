package com.example.sakartveloguide.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sakartveloguide.data.local.entity.PassportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PassportDao {
    @Query("SELECT * FROM passport_stamps ORDER BY dateUnlocked DESC")
    fun getAllStamps(): Flow<List<PassportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStamp(stamp: PassportEntity)

    // ARCHITECT'S FIX: Bulk insertion for Cloud Sync
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStamps(stamps: List<PassportEntity>)
}