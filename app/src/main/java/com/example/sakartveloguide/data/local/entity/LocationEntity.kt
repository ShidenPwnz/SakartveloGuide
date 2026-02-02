package com.example.sakartveloguide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.sakartveloguide.data.local.converter.RouteConverter

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val type: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,

    // --- SMART RECOMMENDATION ENGINE ---
    val priority: Int = 1,
    val popularity: Int = 0,
    val isLandmark: Boolean = false,

    @TypeConverters(RouteConverter::class)
    val tags: List<String> = emptyList(),

    // --- MULTILINGUAL DATA ---
    val nameEn: String,
    val nameKa: String = "",
    val nameRu: String = "",
    val nameTr: String = "",
    val nameHy: String = "",
    val nameIw: String = "",
    val nameAr: String = "",

    val descEn: String,
    val descKa: String = "",
    val descRu: String = "",
    val descTr: String = "",
    val descHy: String = "",
    val descIw: String = "",
    val descAr: String = ""
)