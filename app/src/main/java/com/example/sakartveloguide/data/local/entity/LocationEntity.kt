package com.example.sakartveloguide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "locations")
data class LocationEntity(
    // ARCHITECT'S FIX: Manual ID control for synchronization with JSON
    @PrimaryKey(autoGenerate = false) 
    @SerializedName("id") val id: Int,
    
    @SerializedName("name") val name: String,
    @SerializedName("region") val region: String,
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lng") val longitude: Double,
    @SerializedName("image") val imageUrl: String
)