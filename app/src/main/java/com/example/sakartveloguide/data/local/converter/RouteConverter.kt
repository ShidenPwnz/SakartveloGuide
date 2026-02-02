package com.example.sakartveloguide.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RouteConverter {
    private val gson = Gson()

    // --- Integer Lists (Trip IDs) ---
    @TypeConverter
    fun fromIdList(value: List<Int>?): String = gson.toJson(value ?: emptyList<Int>())

    @TypeConverter
    fun toIdList(value: String?): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value ?: "[]", type)
    }

    // --- String Lists (Location Tags) ---
    @TypeConverter
    fun fromTagList(value: List<String>?): String = gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toTagList(value: String?): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value ?: "[]", type)
    }
}