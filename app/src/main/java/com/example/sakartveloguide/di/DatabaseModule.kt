package com.example.sakartveloguide.di

import android.content.Context
import androidx.room.Room
import com.example.sakartveloguide.data.local.TripDatabase
import com.example.sakartveloguide.data.local.dao.PassportDao
import com.example.sakartveloguide.data.local.dao.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTripDatabase(@ApplicationContext context: Context): TripDatabase {
        return Room.databaseBuilder(
            context,
            TripDatabase::class.java,
            "sakartvelo_guide.db"
        ).build()
    }

    @Provides
    fun provideTripDao(db: TripDatabase): TripDao = db.tripDao()

    @Provides
    fun providePassportDao(db: TripDatabase): PassportDao = db.passportDao() 
}
