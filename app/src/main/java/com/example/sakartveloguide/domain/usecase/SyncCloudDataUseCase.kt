package com.example.sakartveloguide.domain.usecase

import android.util.Log
import com.example.sakartveloguide.data.local.dao.PassportDao
import com.example.sakartveloguide.data.local.entity.PassportEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SyncCloudDataUseCase @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val passportDao: PassportDao,
    private val auth: FirebaseAuth
) {
    /**
     * ARCHITECT'S REFINEMENT:
     * We attempt to get the most recent stamps. We use the DEFAULT source
     * to allow Firebase to decide between Server and Cache.
     */
    suspend operator fun invoke() {
        val userId = auth.currentUser?.uid ?: return

        try {
            Log.d("CLOUD_SYNC", "Initiating Cloud Mirror for User: $userId")

            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("stamps")
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val cloudStamps = snapshot.toObjects(PassportEntity::class.java)
                Log.d("CLOUD_SYNC", "Mirror Success: ${cloudStamps.size} stamps recovered.")

                // Bulk Update Local Room Storage
                passportDao.insertStamps(cloudStamps)
            } else {
                Log.d("CLOUD_SYNC", "Mirror Check: Cloud vault is empty.")
            }
        } catch (e: Exception) {
            Log.e("CLOUD_SYNC", "Mirror Protocol Failed: ${e.message}")
        }
    }
}