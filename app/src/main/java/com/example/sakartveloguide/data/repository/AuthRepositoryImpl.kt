package com.example.sakartveloguide.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.sakartveloguide.domain.model.SakartveloUser
import com.example.sakartveloguide.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.credentials.CustomCredential

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    private val _currentUser = MutableStateFlow<SakartveloUser?>(null)
    override val currentUser: StateFlow<SakartveloUser?> = _currentUser.asStateFlow()

    private val TAG = "SAKARTVELO_AUTH"

    // ARCHITECT'S WARNING: Ensure this is the "Web Client ID", NOT the Android one.
    private val WEB_CLIENT_ID = "500997288040-p778jvh39v3v64m8cr6cduaq18suqa0j.apps.googleusercontent.com"

    override suspend fun signIn(context: Context): Result<SakartveloUser> {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            Log.d(TAG, "Launching Credential Manager...")
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            // ARCHITECT'S FIX:
            // We must check if it's a CustomCredential and then use the
            // GoogleIdTokenCredential factory to parse the data bundle.
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                Log.d(TAG, "Auth successful for: ${googleIdTokenCredential.displayName}")

                val user = SakartveloUser(
                    id = googleIdTokenCredential.id,
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName,
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    idToken = googleIdTokenCredential.idToken,
                    isGuest = false
                )
                _currentUser.value = user
                Result.success(user)
            } else {
                Log.e(TAG, "Unexpected credential type: ${credential.type}")
                Result.failure(Exception("Unexpected credential type"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Auth failure", e)
            Result.failure(e)
        }
    }

    override suspend fun continueAsGuest() {
        Log.d(TAG, "Initializing Guest Session")
        _currentUser.value = SakartveloUser(
            id = "anonymous_guest",
            email = "guest@sakartvelo.local",
            displayName = "Guest Explorer",
            photoUrl = null,
            idToken = null,
            isGuest = true
        )
    }

    override suspend fun signOut() {
        Log.d(TAG, "Clearing Session")
        _currentUser.value = null
    }
}