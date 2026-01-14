package com.example.sakartveloguide.data.repository

import com.example.sakartveloguide.data.local.dao.PassportDao
import com.example.sakartveloguide.data.local.entity.PassportEntity
import com.example.sakartveloguide.domain.repository.PassportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PassportRepositoryImpl @Inject constructor(
    private val passportDao: PassportDao
) : PassportRepository {
    
    override fun getAllStamps(): Flow<List<PassportEntity>> = passportDao.getAllStamps()

    // ARCHITECT'S FIX: Match the interface name
    override suspend fun addStamp(stamp: PassportEntity) {
        passportDao.insertStamp(stamp)
    }
}
