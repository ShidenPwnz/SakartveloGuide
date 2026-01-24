package com.example.sakartveloguide.domain.repository

import android.content.Context
import com.example.sakartveloguide.domain.model.SakartveloUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<SakartveloUser?>
    suspend fun signIn(context: Context): Result<SakartveloUser>
    suspend fun continueAsGuest() // ADD THIS
    suspend fun signOut()
}