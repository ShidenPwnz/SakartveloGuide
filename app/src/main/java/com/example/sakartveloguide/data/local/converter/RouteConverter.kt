package com.example.sakartveloguide.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RouteConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromIdList(value: List<Int>?): String = gson.toJson(value ?: emptyList<Int>())

    @TypeConverter
    fun toIdList(value: String?): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value ?: "[]", type)
    }
}