package com.example.sakartveloguide.domain.repository

import com.example.sakartveloguide.data.local.entity.PassportEntity
import kotlinx.coroutines.flow.Flow

interface PassportRepository {
    fun getAllStamps(): Flow<List<PassportEntity>>
    // ARCHITECT'S FIX: Renamed from saveStamp to addStamp
    suspend fun addStamp(stamp: PassportEntity) 
}
