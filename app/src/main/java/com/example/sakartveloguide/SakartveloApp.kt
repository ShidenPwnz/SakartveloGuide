package com.example.sakartveloguide

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

@HiltAndroidApp
class SakartveloApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Centralized MapLibre Initialization with API Key
        MapLibre.getInstance(
            this,
            "qkMaulJ2NlsVPfbF8xwp",
            WellKnownTileServer.MapTiler
        )

        // ARCHITECT'S MOVE: Explicitly manage connection state for offline capabilities
        MapLibre.setConnected(true)
    }
}
