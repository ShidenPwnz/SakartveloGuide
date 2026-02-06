package com.example.sakartveloguide.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.navigation.compose.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.sakartveloguide.presentation.home.*
import com.example.sakartveloguide.presentation.passport.*
import com.example.sakartveloguide.presentation.settings.*
import com.example.sakartveloguide.presentation.planner.*
import com.example.sakartveloguide.presentation.builder.FobSetupView
import com.example.sakartveloguide.domain.model.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SakartveloNavGraph(homeViewModel: HomeViewModel) {
    val navController = rememberNavController()
    val startDestState by homeViewModel.initialDestination.collectAsState()

    if (startDestState != null) {
        LaunchedEffect(Unit) {
            homeViewModel.navigationEvent.collectLatest { route ->
                navController.navigate(route) {
                    if (route == "home") popUpTo(0)
                    launchSingleTop = true
                }
            }
        }

        NavHost(navController = navController, startDestination = startDestState!!) {
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onPathClick = { id ->
                        if (id == "meta_sandbox" || id == "meta_tutorial") {
                            homeViewModel.prepareForNewMission()
                        }
                        navController.navigate("briefing/$id?ids=")
                    },
                    onPassportClick = { navController.navigate("passport") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

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
                    onBack = { navController.navigate("home") { popUpTo(0); launchSingleTop = true } },
                    onNavigateToFobMap = {
                        val tid = backStackEntry.arguments?.getString("tripId") ?: ""
                        navController.navigate("fob_recon/$tid")
                    },
                    onNavigateToPassport = { navController.navigate("passport") }
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

                // ARCHITECT'S FIX: Explicitly collect state for Recon center point
                val plannerState by vm.uiState.collectAsState()

                val center = if (plannerState.route.isNotEmpty()) {
                    GeoPoint(plannerState.route[0].latitude, plannerState.route[0].longitude)
                } else {
                    GeoPoint(41.7125, 44.7930)
                }

                FobSetupView(initialCenter = center, onSetBase = { loc ->
                    vm.setBaseCamp(loc)
                    navController.popBackStack()
                })
            }

            composable("settings") {
                val sessionState by homeViewModel.userSession.collectAsState(initial = UserSession())
                val currentUser by homeViewModel.currentUser.collectAsState()
                SettingsScreen(
                    user = currentUser,
                    session = sessionState,
                    onBack = { navController.popBackStack() },
                    onWipeData = { homeViewModel.wipeAllUserData() },
                    onLogout = { homeViewModel.signOut() },
                    onLanguageChange = { homeViewModel.onLanguageChange(it) }
                )
            }

            composable("passport") {
                val passportViewModel: PassportViewModel = hiltViewModel()
                val stampsState by passportViewModel.stamps.collectAsState()
                PassportScreen(stampsState, { navController.popBackStack() })
            }
        }
    }
}