package com.example.sakartveloguide.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakartveloguide.presentation.home.*
import com.example.sakartveloguide.presentation.detail.PathDetailsScreen
import com.example.sakartveloguide.presentation.detail.PathDetailsViewModel
import com.example.sakartveloguide.presentation.mission.MissionControlScreen
import com.example.sakartveloguide.presentation.battle.BattlePlanScreen
import com.example.sakartveloguide.presentation.mission.LogisticsWizard
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
        homeViewModel.navigationEvent.collectLatest { route: String ->
            navController.navigate(route) {
                if (route.contains("mission_protocol")) {
                    popUpTo("logistics_setup/{tripId}") { inclusive = true }
                } else if (route == "home") {
                    popUpTo(0)
                }
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
                    onPaywallClick = {},
                    onPassportClick = { navController.navigate("passport") }
                )
            }

            composable("pitch/{tripId}") { backStackEntry ->
                val detailsViewModel: PathDetailsViewModel = hiltViewModel()
                val state by detailsViewModel.uiState.collectAsState()
                PathDetailsScreen(
                    state = state,
                    onLockPath = { id ->
                        homeViewModel.initiateLogistics(id)
                        navController.navigate("logistics_setup/$id")
                    }
                )
            }

            composable(
                route = "logistics_setup/{tripId}",
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val state: HomeUiState by homeViewModel.uiState.collectAsState()
                val trip = state.groupedPaths.values.flatten().find { it.id == tripId }
                val profile by homeViewModel.logisticsProfile.collectAsState()

                trip?.let {
                    LogisticsWizard(
                        trip = it,
                        currentProfile = profile,
                        onDismiss = { navController.popBackStack() },
                        onConfirm = { newProfile ->
                            homeViewModel.onConfirmLogistics(newProfile)
                        }
                    )
                }
            }

            composable(
                route = "mission_protocol/{tripId}",
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val state: HomeUiState by homeViewModel.uiState.collectAsState()
                val trip = state.groupedPaths.values.flatten().find { it.id == tripId }

                trip?.let { activeTrip ->
                    MissionControlScreen(
                        trip = activeTrip,
                        viewModel = homeViewModel,
                        onStartTrip = {
                            homeViewModel.startMission(activeTrip)
                            navController.navigate("battle/${activeTrip.id}") {
                                popUpTo("home")
                            }
                        },
                        onReconfigure = {
                            navController.navigate("logistics_setup/$tripId") {
                                popUpTo("mission_protocol/$tripId") { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable("battle/{tripId}") { backStackEntry ->
                val state by homeViewModel.uiState.collectAsState()
                val trip = state.groupedPaths.values.flatten().find { it.id == backStackEntry.arguments?.getString("tripId") }
                trip?.let {
                    BattlePlanScreen(
                        path = it,
                        viewModel = homeViewModel,
                        onFinish = { onCompleteTrip(it) },
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