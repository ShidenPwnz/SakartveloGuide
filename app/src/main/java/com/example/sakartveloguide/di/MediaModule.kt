package com.example.sakartveloguide.di

import android.content.Context
import com.example.sakartveloguide.ui.manager.HapticManager
import com.example.sakartveloguide.ui.manager.SoundManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideSoundManager(@ApplicationContext context: Context): SoundManager = SoundManager(context)

    @Provides
    @Singleton
    fun provideHapticManager(@ApplicationContext context: Context): HapticManager = HapticManager(context)
}