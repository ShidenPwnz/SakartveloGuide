package com.example.sakartveloguide.data.repository

import android.util.Log
import com.example.sakartveloguide.data.local.dao.PassportDao
import com.example.sakartveloguide.data.local.entity.PassportEntity
import com.example.sakartveloguide.domain.repository.PassportRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PassportRepositoryImpl @Inject constructor(
    private val passportDao: PassportDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : PassportRepository {

    override fun getAllStamps(): Flow<List<PassportEntity>> = passportDao.getAllStamps()

    override suspend fun addStamp(stamp: PassportEntity) {
        // 1. Save Locally (Primary)
        passportDao.insertStamp(stamp)

        // 2. Mirror to Cloud (Secondary)
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .collection("stamps")
                .document(stamp.regionId) // Use regionId to prevent duplicates
                .set(stamp)
                .addOnSuccessListener { Log.d("CLOUD_SYNC", "Stamp mirrored: ${stamp.regionName}") }
                .addOnFailureListener { e -> Log.e("CLOUD_SYNC", "Mirror failed: ${e.message}") }
        }
    }
}