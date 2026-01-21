package com.example.sakartveloguide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.sakartveloguide.data.local.converter.RouteConverter

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val category: String,
    val difficulty: String,
    val durationDays: Int,

    // --- RESTORED FIELDS (Required by TripDao) ---
    val isLocked: Boolean = false,
    val isPremium: Boolean = false,
    val hasSnowWarning: Boolean = false,

    // The new ID sequence from JSON
    @TypeConverters(RouteConverter::class)
    val targetIds: List<Int>
)