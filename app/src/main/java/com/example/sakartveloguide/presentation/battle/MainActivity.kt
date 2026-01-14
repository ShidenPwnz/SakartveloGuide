package com.example.sakartveloguide.presentation.battle

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
import com.example.sakartveloguide.presentation.mission.LogisticsWizard
import com.example.sakartveloguide.presentation.navigation.SakartveloNavGraph
import com.example.sakartveloguide.presentation.passport.components.PassportSlamOverlay
import com.example.sakartveloguide.presentation.theme.SakartveloTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !viewModel.isSplashReady.value }

        setContent {
            // Observe the states from the ViewModel
            val pendingLogisticsId by viewModel.pendingLogisticsTripId.collectAsState()
            val stampingTrip by viewModel.stampingTrip.collectAsState()
            val uiState by viewModel.uiState.collectAsState()

            SakartveloTheme {
                Box(modifier = Modifier.fillMaxSize()) {

                    // LAYER 1: NAVIGATION HUB
                    SakartveloNavGraph(
                        homeViewModel = viewModel,
                        onCompleteTrip = { trip -> viewModel.onCompleteTrip(trip) },
                        onAbortTrip = { viewModel.onAbortTrip() },
                        onCallFleet = { title -> viewModel.onCallFleet(title) },
                        onOpenBolt = { viewModel.onOpenBolt() },
                        onBookAccommodation = { city -> viewModel.onBookAccommodation(city) }
                    )

                    // LAYER 2: LOGISTICS WIZARD OVERLAY
                    // ARCHITECT'S FIX: Find the trip in the UI state to pass it to the wizard
                    pendingLogisticsId?.let { tripId ->
                        val selectedTrip = uiState.groupedPaths.values
                            .flatten()
                            .find { it.id == tripId }

                        selectedTrip?.let { trip ->
                            LogisticsWizard(
                                trip = trip, // PASSING THE REQUIRED PARAMETER
                                onDismiss = { viewModel.dismissWizard() },
                                onConfirm = { profile ->
                                    viewModel.onConfirmLogistics(profile)
                                }
                            )
                        }
                    }

                    // LAYER 3: PASSPORT SLAM (ACHIEVEMENT)
                    stampingTrip?.let { trip ->
                        PassportSlamOverlay(
                            trip = trip,
                            onAnimationFinished = {
                                viewModel.onSlamAnimationFinished()
                            }
                        )
                    }
                }
            }
        }
    }
}