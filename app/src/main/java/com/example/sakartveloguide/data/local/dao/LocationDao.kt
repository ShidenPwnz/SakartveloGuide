package com.example.sakartveloguide.data.local.dao

import androidx.room.*
import com.example.sakartveloguide.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT DISTINCT type FROM locations")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT region FROM locations") // NEW
    fun getAllRegions(): Flow<List<String>>

    @Query("SELECT * FROM locations WHERE id IN (:ids)")
    suspend fun getLocationsByIds(ids: List<Int>): List<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getCount(): Int

    @Query("DELETE FROM locations")
    suspend fun nukeTable()
}