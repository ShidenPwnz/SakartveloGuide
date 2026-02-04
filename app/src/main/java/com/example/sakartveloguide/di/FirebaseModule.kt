package com.example.sakartveloguide.di

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(@ApplicationContext context: Context): FirebaseAuth {
        // ARCHITECT'S FIX: Defensive initialization
        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.d("SAKARTVELO_DI", "Forcing Firebase Init in Module")
            FirebaseApp.initializeApp(context)
        }
        return Firebase.auth
    }

    @Provides
    @Singleton
    fun provideFirestore(@ApplicationContext context: Context): FirebaseFirestore {
        // Double check for Firestore as well
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        val firestore = Firebase.firestore
        // ARCHITECT'S NOTE: Enable offline persistence for mountain reliability
        return firestore
    }
}