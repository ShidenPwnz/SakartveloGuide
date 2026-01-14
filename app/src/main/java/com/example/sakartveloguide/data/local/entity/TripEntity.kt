package com.example.sakartveloguide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sakartveloguide.domain.model.BattleNode
import com.example.sakartveloguide.domain.model.GeoPoint

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val category: String,
    val difficulty: String,
    val totalRideTimeMinutes: Int,
    val durationDays: Int, 
    val hasSnowWarning: Boolean = false,
    val isLocked: Boolean = false,      
    val isPremium: Boolean = false,
    val route: List<GeoPoint> = emptyList(),
    val itinerary: List<BattleNode> = emptyList()
)
