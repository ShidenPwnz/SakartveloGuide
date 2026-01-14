package com.example.sakartveloguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.sakartveloguide.presentation.home.HomeViewModel
import com.example.sakartveloguide.presentation.navigation.SakartveloNavGraph
import com.example.sakartveloguide.presentation.theme.SakartveloTheme
import com.example.sakartveloguide.presentation.passport.components.PassportSlamOverlay
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !viewModel.isSplashReady.value }

        setContent {
            val stampingTrip by viewModel.stampingTrip.collectAsState()

            SakartveloTheme {
                Box(modifier = Modifier.fillMaxSize()) {

                    // Main Content Layer
                    SakartveloNavGraph(
                        homeViewModel = viewModel,
                        onCompleteTrip = { trip -> viewModel.onCompleteTrip(trip) },
                        onAbortTrip = { viewModel.onAbortTrip() },
                        onCallFleet = { title -> viewModel.onCallFleet(title) },
                        onOpenBolt = { viewModel.onOpenBolt() },
                        onBookAccommodation = { city -> viewModel.onBookAccommodation(city) }
                    )

                    // Achievement Overlay
                    stampingTrip?.let { trip ->
                        PassportSlamOverlay(
                            trip = trip,
                            onAnimationFinished = { viewModel.onSlamAnimationFinished() }
                        )
                    }
                }
            }
        }
    }
}