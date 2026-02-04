package com.example.sakartveloguide

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import javax.inject.Inject

@HiltAndroidApp
class SakartveloApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()

        // ARCHITECT'S FIX: Initialize Firebase explicitly to prevent Hilt injection race conditions
        FirebaseApp.initializeApp(this)

        // Centralized MapLibre Initialization
        MapLibre.getInstance(
            this,
            "qkMaulJ2NlsVPfbF8xwp",
            WellKnownTileServer.MapTiler
        )
        MapLibre.setConnected(true)
    }
}