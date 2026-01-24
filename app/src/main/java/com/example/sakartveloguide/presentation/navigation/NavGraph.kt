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
                    onBack = {
                        navController.navigate("home") {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    },
                    onNavigateToFobMap = {
                        navController.navigate("fob_recon/${backStackEntry.arguments?.getString("tripId")}")
                    },
                    onNavigateToPassport = {
                        navController.navigate("passport")
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
                val plannerState by vm.uiState.collectAsState()

                val firstPoint = plannerState.route.firstOrNull()?.let {
                    GeoPoint(it.latitude, it.longitude)
                } ?: GeoPoint(41.7125, 44.7930)

                FobSetupView(
                    initialCenter = firstPoint,
                    onSetBase = { loc ->
                        vm.setBaseCamp(loc)
                        navController.popBackStack()
                    }
                )
            }

            composable("settings") {
                val sessionState by homeViewModel.userSession.collectAsState(initial = UserSession())
                val currentUser by homeViewModel.currentUser.collectAsState()
                SettingsScreen(
                    user = currentUser,
                    session = sessionState,
                    onBack = { navController.popBackStack() },
                    onWipeData = { homeViewModel.wipeAllUserData() },
                    onLogout = {
                        homeViewModel.signOut()
                        navController.popBackStack()
                    },
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