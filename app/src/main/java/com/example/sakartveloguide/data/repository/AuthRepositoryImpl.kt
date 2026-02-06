package com.example.sakartveloguide.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.sakartveloguide.BuildConfig
import com.example.sakartveloguide.data.local.dao.PassportDao
import com.example.sakartveloguide.domain.model.SakartveloUser
import com.example.sakartveloguide.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val passportDao: PassportDao // Injected for Guest Migration
) : AuthRepository {

    private val _currentUser = MutableStateFlow<SakartveloUser?>(null)
    override val currentUser: StateFlow<SakartveloUser?> = _currentUser.asStateFlow()

    private val WEB_CLIENT_ID = BuildConfig.GOOGLE_WEB_CLIENT_ID

    init {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            _currentUser.value = SakartveloUser(firebaseUser.uid, firebaseUser.email ?: "", firebaseUser.displayName, firebaseUser.photoUrl?.toString(), null, false)
        }
    }

    override suspend fun signIn(context: Context): Result<SakartveloUser> {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false).setServerClientId(WEB_CLIENT_ID).setAutoSelectEnabled(true).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                val fUser = authResult.user!!

                // ARCHITECT'S FIX: Guest to User Stamp Migration
                migrateLocalStampsToCloud(fUser.uid)

                val user = SakartveloUser(fUser.uid, fUser.email ?: "", fUser.displayName, fUser.photoUrl?.toString(), googleIdTokenCredential.idToken, false)
                _currentUser.value = user
                Result.success(user)
            } else { Result.failure(Exception("Invalid Credential Type")) }
        } catch (e: Exception) {
            Log.e("AUTH_PROTOCOL", "Authentication failed: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun migrateLocalStampsToCloud(userId: String) {
        try {
            val localStamps = passportDao.getAllStamps().first()
            if (localStamps.isNotEmpty()) {
                val batch = firestore.batch()
                localStamps.forEach { stamp ->
                    val docRef = firestore.collection("users").document(userId).collection("stamps").document(stamp.regionId)
                    batch.set(docRef, stamp)
                }
                batch.commit().await()
                Log.d("AUTH_MIRROR", "Successfully migrated ${localStamps.size} local stamps to Cloud.")
            }
        } catch (e: Exception) {
            Log.e("AUTH_MIRROR", "Mirroring failed: ${e.message}")
        }
    }

    override suspend fun continueAsGuest() {
        _currentUser.value = SakartveloUser("anonymous_guest", "guest@sakartvelo.local", "Traveler", null, null, true)
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        dataStore.edit { it.clear() }
        _currentUser.value = null
    }
}