package com.example.sakartveloguide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.sakartveloguide.data.local.converter.RouteConverter
import com.google.gson.annotations.SerializedName

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val imageUrl: String,
    val category: String,
    val difficulty: String,
    val durationDays: Int,

    // --- MULTILINGUAL TITLES ---
    @SerializedName("title_en") val titleEn: String,
    @SerializedName("title_ka") val titleKa: String = "",
    @SerializedName("title_ru") val titleRu: String = "",
    @SerializedName("title_tr") val titleTr: String = "",
    @SerializedName("title_hy") val titleHy: String = "",
    @SerializedName("title_iw") val titleIw: String = "",
    @SerializedName("title_ar") val titleAr: String = "",

    // --- MULTILINGUAL DESCRIPTIONS ---
    @SerializedName("desc_en") val descEn: String,
    @SerializedName("desc_ka") val descKa: String = "",
    @SerializedName("desc_ru") val descRu: String = "",
    @SerializedName("desc_tr") val descTr: String = "",
    @SerializedName("desc_hy") val descHy: String = "",
    @SerializedName("desc_iw") val descIw: String = "",
    @SerializedName("desc_ar") val descAr: String = "",

    // Logic Fields
    val isLocked: Boolean = false,
    val isPremium: Boolean = false,
    val hasSnowWarning: Boolean = false,

    @TypeConverters(RouteConverter::class)
    val targetIds: List<Int>
)