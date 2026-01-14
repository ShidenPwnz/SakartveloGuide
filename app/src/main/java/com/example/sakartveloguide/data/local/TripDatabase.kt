package com.example.sakartveloguide.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sakartveloguide.data.local.converter.RouteConverter // IMPORT THIS
import com.example.sakartveloguide.data.local.dao.TripDao
import com.example.sakartveloguide.data.local.dao.PassportDao
import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.data.local.entity.PassportEntity

@Database(
    entities = [TripEntity::class, PassportEntity::class], 
    version = 7, 
    exportSchema = false
)
@TypeConverters(RouteConverter::class) // ARCHITECT'S FIX: REGISTER THE CONVERTER HERE
abstract class TripDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun passportDao(): PassportDao
}
