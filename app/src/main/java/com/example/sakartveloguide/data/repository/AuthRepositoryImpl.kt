package com.example.sakartveloguide.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.sakartveloguide.domain.model.SakartveloUser
import com.example.sakartveloguide.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    private val _currentUser = MutableStateFlow<SakartveloUser?>(null)
    override val currentUser: StateFlow<SakartveloUser?> = _currentUser.asStateFlow()

    private val WEB_CLIENT_ID = "618444346122-2ed25fqdmhe2d5hvc7teg2p0t8rjo1g9.apps.googleusercontent.com"

    init {
        // Sync local state with Firebase Auth state on startup
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            _currentUser.value = SakartveloUser(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString(),
                idToken = null,
                isGuest = false
            )
        }
    }

    override suspend fun signIn(context: Context): Result<SakartveloUser> {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                // 1. Authenticate with Firebase
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                val fUser = authResult.user!!

                // 2. Create Domain User
                val user = SakartveloUser(
                    id = fUser.uid, // Use Firebase UID as the primary key
                    email = fUser.email ?: "",
                    displayName = fUser.displayName,
                    photoUrl = fUser.photoUrl?.toString(),
                    idToken = googleIdTokenCredential.idToken,
                    isGuest = false
                )

                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid Credential Type"))
            }
        } catch (e: Exception) {
            Log.e("AUTH_PROTOCOL", "Authentication failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun continueAsGuest() {
        // For guest mode, we don't sign into Firebase, but keep local state
        val guest = SakartveloUser("anonymous_guest", "guest@sakartvelo.local", "Traveler", null, null, true)
        _currentUser.value = guest
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        dataStore.edit { it.clear() }
        _currentUser.value = null
    }
}