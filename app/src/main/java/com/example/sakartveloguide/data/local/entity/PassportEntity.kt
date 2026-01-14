package com.example.sakartveloguide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passport_stamps")
data class PassportEntity(
    @PrimaryKey val regionId: String, // e.g., "MTSKHETA_MTIANETI"
    val regionName: String,
    val dateUnlocked: Long,
    val tripTitle: String
)
