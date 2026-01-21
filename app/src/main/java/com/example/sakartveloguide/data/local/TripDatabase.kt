package com.example.sakartveloguide.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sakartveloguide.data.local.converter.RouteConverter
import com.example.sakartveloguide.data.local.dao.TripDao
import com.example.sakartveloguide.data.local.dao.PassportDao
import com.example.sakartveloguide.data.local.dao.LocationDao
import com.example.sakartveloguide.data.local.entity.TripEntity
import com.example.sakartveloguide.data.local.entity.PassportEntity
import com.example.sakartveloguide.data.local.entity.LocationEntity

@Database(
    entities = [
        TripEntity::class,
        PassportEntity::class,
        LocationEntity::class
    ],
    version = 10, // BUMP TO 10
    exportSchema = false
)
@TypeConverters(RouteConverter::class)
abstract class TripDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun passportDao(): PassportDao
    abstract fun locationDao(): LocationDao
}