package com.example.sakartveloguide.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.sakartveloguide.presentation.home.*
import com.example.sakartveloguide.presentation.passport.*
import com.example.sakartveloguide.presentation.settings.*
import com.example.sakartveloguide.presentation.builder.*
import com.example.sakartveloguide.presentation.planner.*
import com.example.sakartveloguide.domain.model.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SakartveloNavGraph(
    homeViewModel: HomeViewModel,
    onCompleteTrip: (TripPath) -> Unit
) {
    val navController = rememberNavController()
    val startDestination by homeViewModel.initialDestination.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.navigationEvent.collectLatest { route ->
            navController.navigate(route) {
                if (route == "home") popUpTo(0)
                launchSingleTop = true
            }
        }
    }

    if (startDestination != null) {
        NavHost(navController = navController, startDestination = startDestination!!) {

            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onPathClick = { id ->
                        if (id == "meta_sandbox") {
                            homeViewModel.prepareForNewMission()
                            navController.navigate("briefing/custom_cargo?ids=")
                        } else {
                            navController.navigate("briefing/$id")
                        }
                    },
                    onPassportClick = { navController.navigate("passport") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            composable("custom_builder") {
                // Dead route, but kept for safety
                val vm: MissionBuilderViewModel = hiltViewModel()
                MissionBuilderScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onProceed = { ids -> navController.navigate("briefing/custom_cargo?ids=${ids.joinToString(",")}") }
                )
            }

            // --- TRIP PLANNER ---
            composable(
                route = "briefing/{tripId}?ids={ids}",
                arguments = listOf(
                    navArgument("tripId") { type = NavType.StringType },
                    navArgument("ids") { defaultValue = ""; type = NavType.StringType }
                )
            ) { backStackEntry ->
                val vm: AdventureViewModel = hiltViewModel(backStackEntry)

                TripPlannerScreen(
                    viewModel = vm,
                    // ARCHITECT'S FIX: Safe Exit Strategy
                    // If app was killed, backstack is empty. popBackStack() would close app.
                    // We explicitly navigate Home to ensure safety.
                    onBack = {
                        navController.navigate("home") {
                            popUpTo(0) // Clear stack
                            launchSingleTop = true
                        }
                    },
                    onNavigateToFobMap = {
                        navController.navigate("fob_recon/${backStackEntry.arguments?.getString("tripId")}")
                    }
                )
            }

            composable(
                route = "fob_recon/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("briefing/$tripId?ids=")
                }

                val vm: AdventureViewModel = hiltViewModel(parentEntry)

                FobSetupView(
                    initialCenter = vm.uiState.value.route.firstOrNull()?.let { GeoPoint(it.latitude, it.longitude) } ?: GeoPoint(41.7, 44.8),
                    onSetBase = { loc ->
                        vm.setBaseCamp(loc)
                        navController.popBackStack()
                    }
                )
            }

            composable("settings") {
                val sessionState = homeViewModel.userSession.collectAsState(initial = UserSession())
                SettingsScreen(
                    session = sessionState.value,
                    onBack = { navController.popBackStack() },
                    onWipeData = { homeViewModel.wipeAllUserData() },
                    onLanguageChange = { code -> homeViewModel.onLanguageChange(code) }
                )
            }

            composable("passport") {
                val passportViewModel: PassportViewModel = hiltViewModel()
                val stampsState = passportViewModel.stamps.collectAsState()
                PassportScreen(stampsState.value, { navController.popBackStack() })
            }
        }
    }
}