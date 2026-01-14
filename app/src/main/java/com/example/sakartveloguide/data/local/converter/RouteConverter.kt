package com.example.sakartveloguide.data.local.converter

import androidx.room.TypeConverter
import com.example.sakartveloguide.domain.model.BattleNode
import com.example.sakartveloguide.domain.model.GeoPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RouteConverter {
    private val gson = Gson()

    // 1. Convert List<GeoPoint> to String and back
    @TypeConverter
    fun fromGeoList(value: List<GeoPoint>?): String = gson.toJson(value ?: emptyList<GeoPoint>())

    @TypeConverter
    fun toGeoList(value: String?): List<GeoPoint> {
        val type = object : TypeToken<List<GeoPoint>>() {}.type
        return gson.fromJson(value ?: "[]", type)
    }

    // 2. Convert List<BattleNode> to String and back
    @TypeConverter
    fun fromItinerary(value: List<BattleNode>?): String = gson.toJson(value ?: emptyList<BattleNode>())

    @TypeConverter
    fun toItinerary(value: String?): List<BattleNode> {
        val type = object : TypeToken<List<BattleNode>>() {}.type
        return gson.fromJson(value ?: "[]", type)
    }
}
