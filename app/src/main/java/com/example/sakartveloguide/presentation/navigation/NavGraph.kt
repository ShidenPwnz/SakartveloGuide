package com.example.sakartveloguide.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakartveloguide.presentation.home.HomeScreen
import com.example.sakartveloguide.presentation.home.HomeViewModel
import com.example.sakartveloguide.presentation.home.HomeUiState
import com.example.sakartveloguide.presentation.detail.PathDetailsScreen
import com.example.sakartveloguide.presentation.detail.PathDetailsViewModel
import com.example.sakartveloguide.presentation.mission.MissionControlScreen
import com.example.sakartveloguide.presentation.battle.BattlePlanScreen
import com.example.sakartveloguide.presentation.passport.PassportScreen
import com.example.sakartveloguide.presentation.passport.PassportViewModel
import com.example.sakartveloguide.domain.model.TripPath
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SakartveloNavGraph(
    homeViewModel: HomeViewModel,
    onCompleteTrip: (TripPath) -> Unit,
    onAbortTrip: () -> Unit,
    onCallFleet: (String) -> Unit,
    onOpenBolt: () -> Unit,
    onBookAccommodation: (String) -> Unit,
) {
    val navController = rememberNavController()
    val startDestination by homeViewModel.initialDestination.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.navigationEvent.collectLatest { route ->
            navController.navigate(route) {
                if (route == "home") popUpTo(0)
                else popUpTo("home") { saveState = true }
                launchSingleTop = true
            }
        }
    }

    if (startDestination != null) {
        NavHost(
            navController = navController,
            startDestination = startDestination!!
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onPathClick = { id -> navController.navigate("pitch/$id") },
                    onPaywallClick = { /* TODO */ },
                    onPassportClick = { navController.navigate("passport") }
                )
            }

            composable(
                route = "pitch/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) {
                val detailsViewModel: PathDetailsViewModel = hiltViewModel()
                val state by detailsViewModel.uiState.collectAsState()

                PathDetailsScreen(
                    state = state,
                    onLockPath = { tripId ->
                        homeViewModel.initiateLogistics(tripId)
                        navController.navigate("logistics/$tripId")
                    }
                )
            }

            composable(
                route = "logistics/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val state: HomeUiState by homeViewModel.uiState.collectAsState()
                val trip = state.groupedPaths.values.flatten().find { it.id == tripId }
                val profile by homeViewModel.logisticsProfile.collectAsState()

                trip?.let { activeTrip ->
                    MissionControlScreen(
                        trip = activeTrip,
                        profile = profile,
                        onStartTrip = {
                            homeViewModel.startMission(activeTrip)
                            navController.navigate("battle/${activeTrip.id}")
                        },
                        onReconfigure = { homeViewModel.initiateLogistics(tripId) }
                    )
                }
            }

            composable("battle/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val state: HomeUiState by homeViewModel.uiState.collectAsState()
                val trip = state.groupedPaths.values.flatten().find { it.id == tripId }

                trip?.let { activePath ->
                    // ARCHITECT'S FIX: Clean call with only 4 parameters
                    BattlePlanScreen(
                        path = activePath,
                        viewModel = homeViewModel,
                        onFinish = { onCompleteTrip(activePath) },
                        onAbort = { onAbortTrip() }
                    )
                }
            }

            composable("passport") {
                val passportViewModel: PassportViewModel = hiltViewModel()
                val stamps by passportViewModel.stamps.collectAsState()
                PassportScreen(stamps = stamps, onBack = { navController.popBackStack() })
            }
        }
    }
}