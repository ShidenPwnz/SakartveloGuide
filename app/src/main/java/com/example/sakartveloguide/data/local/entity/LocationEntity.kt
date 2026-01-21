package com.example.sakartveloguide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val region: String,
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String = ""
)