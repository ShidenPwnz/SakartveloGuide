package com.example.sakartveloguide.data.local

import androidx.room.AutoMigration
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
    version = 13, // ARCHITECT'S FIX: Migration baseline set to 13
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 12, to = 13)
    ]
)
@TypeConverters(RouteConverter::class)
abstract class TripDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun passportDao(): PassportDao
    abstract fun locationDao(): LocationDao
}