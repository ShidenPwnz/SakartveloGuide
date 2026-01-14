package com.example.sakartveloguide.di

import com.example.sakartveloguide.data.repository.PassportRepositoryImpl
import com.example.sakartveloguide.data.repository.TripRepositoryImpl
import com.example.sakartveloguide.domain.repository.PassportRepository
import com.example.sakartveloguide.domain.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTripRepository(impl: TripRepositoryImpl): TripRepository

    @Binds
    @Singleton
    abstract fun bindPassportRepository(impl: PassportRepositoryImpl): PassportRepository
}
