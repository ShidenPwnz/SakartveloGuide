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
            .build()

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase for Cloud Mirroring
        FirebaseApp.initializeApp(this)

        // ARCHITECT'S FIX: Use secure BuildConfig field for MapTiler API
        MapLibre.getInstance(
            this,
            BuildConfig.MAPTILER_API_KEY,
            WellKnownTileServer.MapTiler
        )
    }
}