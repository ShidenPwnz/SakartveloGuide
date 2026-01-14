package com.example.sakartveloguide.di

import android.content.Context
import com.example.sakartveloguide.data.location.LocationManagerImpl
import com.example.sakartveloguide.domain.location.LocationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context
    ): LocationManager {
        return LocationManagerImpl(context)
    }
}
