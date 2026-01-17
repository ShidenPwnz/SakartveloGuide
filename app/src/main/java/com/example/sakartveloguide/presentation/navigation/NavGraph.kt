package com.example.sakartveloguide.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakartveloguide.presentation.home.*
import com.example.sakartveloguide.presentation.detail.PathDetailsScreen
import com.example.sakartveloguide.presentation.detail.PathDetailsViewModel
import com.example.sakartveloguide.presentation.mission.MissionControlScreen
import com.example.sakartveloguide.presentation.mission.LogisticsWizard
import com.example.sakartveloguide.presentation.passport.PassportScreen
import com.example.sakartveloguide.presentation.passport.PassportViewModel
import com.example.sakartveloguide.presentation.settings.SettingsScreen
import com.example.sakartveloguide.presentation.battle.BattlePlanScreen
import com.example.sakartveloguide.domain.model.TripPath
import com.example.sakartveloguide.domain.model.UserSession
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SakartveloNavGraph(
    homeViewModel: HomeViewModel,
    onCompleteTrip: (TripPath) -> Unit
) {
    val navController = rememberNavController()
    // Explicit state collection to avoid delegate ambiguity errors
    val startDestinationState = homeViewModel.initialDestination.collectAsState()
    val startDestination = startDestinationState.value

    LaunchedEffect(Unit) {
        homeViewModel.navigationEvent.collectLatest { route ->
            navController.navigate(route) {
                if (route == "home") popUpTo(0)
                launchSingleTop = true
            }
        }
    }

    if (startDestination != null) {
        NavHost(navController = navController, startDestination = startDestination) {

            // 1. HOME SECTOR
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onPathClick = { id -> navController.navigate("pitch/$id") },
                    // ARCHITECT'S FIX: Removed onPaywallClick to match HomeScreen signature
                    onPassportClick = { navController.navigate("passport") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            // 2. SETTINGS
            composable("settings") {
                val sessionState = homeViewModel.userSession.collectAsState(initial = UserSession())
                SettingsScreen(
                    session = sessionState.value,
                    onBack = { navController.popBackStack() },
                    onWipeData = { homeViewModel.wipeAllUserData() },
                    onLanguageChange = { code -> homeViewModel.onLanguageChange(code) }
                )
            }

            // 3. INTEL REPORT (Pitch)
            composable("pitch/{tripId}") {
                val detailsViewModel: PathDetailsViewModel = hiltViewModel()
                val detailsState = detailsViewModel.uiState.collectAsState()

                PathDetailsScreen(
                    state = detailsState.value,
                    onLockPath = { id ->
                        homeViewModel.initiateLogistics(id)
                        navController.navigate("logistics_setup/$id")
                    }
                )
            }

            // 4. LOGISTICS WIZARD
            composable("logistics_setup/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val profileState = homeViewModel.logisticsProfile.collectAsState()
                val homeState = homeViewModel.uiState.collectAsState()

                // Explicit type-safe finding
                val allTrips = homeState.value.groupedPaths.values.flatten()
                val trip = allTrips.find { it.id == tripId }

                trip?.let {
                    LogisticsWizard(
                        trip = it,
                        currentProfile = profileState.value,
                        onDismiss = { navController.popBackStack() },
                        onConfirm = { newProfile -> homeViewModel.onConfirmLogistics(newProfile) }
                    )
                }
            }

            // 5. MISSION CONTROL
            composable("mission_protocol/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val homeState = homeViewModel.uiState.collectAsState()
                val allTrips = homeState.value.groupedPaths.values.flatten()
                val trip = allTrips.find { it.id == tripId }

                trip?.let {
                    MissionControlScreen(
                        trip = it,
                        viewModel = homeViewModel,
                        onStartTrip = {
                            homeViewModel.startMission(it)
                            navController.navigate("battle/${it.id}") { popUpTo("home") }
                        },
                        onReconfigure = { navController.popBackStack() }
                    )
                }
            }

            // 6. BATTLE PLAN
            composable("battle/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val homeState = homeViewModel.uiState.collectAsState()
                val allTrips = homeState.value.groupedPaths.values.flatten()
                val trip = allTrips.find { it.id == tripId }

                trip?.let {
                    BattlePlanScreen(
                        path = it,
                        viewModel = homeViewModel,
                        onFinish = { onCompleteTrip(it) },
                        onAbort = { homeViewModel.onAbortTrip() }
                    )
                }
            }

            // 7. PASSPORT
            composable("passport") {
                val passportViewModel: PassportViewModel = hiltViewModel()
                val stampsState = passportViewModel.stamps.collectAsState()
                PassportScreen(stampsState.value, { navController.popBackStack() })
            }
        }
    }
}