package com.example.sakartveloguide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val type: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,

    // Trip Titles (From your JSON 'name')
    val nameEn: String,
    val nameKa: String = "",
    val nameRu: String = "",
    val nameTr: String = "",
    val nameHy: String = "",
    val nameIw: String = "",
    val nameAr: String = "",

    // Descriptions (From your JSON 'desc_xx')
    val descEn: String,
    val descKa: String = "",
    val descRu: String = "",
    val descTr: String = "",
    val descHy: String = "",
    val descIw: String = "",
    val descAr: String = ""
)