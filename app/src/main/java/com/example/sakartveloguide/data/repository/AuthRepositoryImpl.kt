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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AuthRepository {

    private val _currentUser = MutableStateFlow<SakartveloUser?>(null)
    override val currentUser: StateFlow<SakartveloUser?> = _currentUser.asStateFlow()

    private val TAG = "SAKARTVELO_AUTH"
    private val WEB_CLIENT_ID = "500997288040-p778jvh39v3v64m8cr6cduaq18suqa0j.apps.googleusercontent.com"

    private val KEY_AUTH_ID = stringPreferencesKey("auth_user_id")
    private val KEY_AUTH_EMAIL = stringPreferencesKey("auth_user_email")
    private val KEY_AUTH_NAME = stringPreferencesKey("auth_user_name")
    private val KEY_AUTH_PHOTO = stringPreferencesKey("auth_user_photo")
    private val KEY_AUTH_IS_GUEST = booleanPreferencesKey("auth_is_guest")

    init {
        // ARCHITECT'S FIX: Blocking check for the very first read to prevent Race Conditions
        // This ensures the ID is known before the ViewModels start their work.
        val prefs = runBlocking { dataStore.data.first() }
        val savedId = prefs[KEY_AUTH_ID]
        if (!savedId.isNullOrEmpty()) {
            _currentUser.value = SakartveloUser(
                id = savedId,
                email = prefs[KEY_AUTH_EMAIL] ?: "",
                displayName = prefs[KEY_AUTH_NAME],
                photoUrl = prefs[KEY_AUTH_PHOTO],
                idToken = null,
                isGuest = prefs[KEY_AUTH_IS_GUEST] ?: false
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
                val user = SakartveloUser(
                    id = googleIdTokenCredential.id,
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName,
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    idToken = googleIdTokenCredential.idToken,
                    isGuest = false
                )
                persistSession(user)
                _currentUser.value = user
                Result.success(user)
            } else { Result.failure(Exception("Type Mismatch")) }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun continueAsGuest() {
        val guest = SakartveloUser("anonymous_guest", "guest@sakartvelo.local", "Guest Explorer", null, null, true)
        persistSession(guest)
        _currentUser.value = guest
    }

    override suspend fun signOut() {
        dataStore.edit { it.clear() }
        _currentUser.value = null
    }

    private suspend fun persistSession(user: SakartveloUser) {
        dataStore.edit { prefs ->
            prefs[KEY_AUTH_ID] = user.id
            prefs[KEY_AUTH_EMAIL] = user.email
            user.displayName?.let { prefs[KEY_AUTH_NAME] = it }
            user.photoUrl?.let { prefs[KEY_AUTH_PHOTO] = it }
            prefs[KEY_AUTH_IS_GUEST] = user.isGuest
        }
    }
}