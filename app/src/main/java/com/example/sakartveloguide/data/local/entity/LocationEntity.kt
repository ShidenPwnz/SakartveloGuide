package com.example.sakartveloguide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: Int,
    val type: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,

    // NAMES (If your JSON only has 'name', we store it in nameEn and use as fallback)
    val nameEn: String,
    val nameKa: String = "",
    val nameRu: String = "",
    val nameTr: String = "",
    val nameHy: String = "",
    val nameIw: String = "",
    val nameAr: String = "",

    // DESCRIPTIONS (Mapped from your desc_xx keys)
    val descEn: String,
    val descKa: String = "",
    val descRu: String = "",
    val descTr: String = "",
    val descHy: String = "",
    val descIw: String = "",
    val descAr: String = ""
)