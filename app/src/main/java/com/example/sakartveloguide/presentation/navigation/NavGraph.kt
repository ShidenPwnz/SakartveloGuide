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
import com.example.sakartveloguide.presentation.battle.BattleViewModel
import com.example.sakartveloguide.presentation.battle.components.FobSetupView // IMPORT ADDED
import com.example.sakartveloguide.domain.model.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SakartveloNavGraph(
    homeViewModel: HomeViewModel,
    onCompleteTrip: (TripPath) -> Unit
) {
    val navController = rememberNavController()
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

            composable("home") {
                HomeScreen(viewModel = homeViewModel, onPathClick = { id -> navController.navigate("pitch/$id") }, onPassportClick = { navController.navigate("passport") }, onSettingsClick = { navController.navigate("settings") })
            }

            composable("settings") {
                val sessionState = homeViewModel.userSession.collectAsState(initial = UserSession())
                SettingsScreen(session = sessionState.value, onBack = { navController.popBackStack() }, onWipeData = { homeViewModel.wipeAllUserData() }, onLanguageChange = { code -> homeViewModel.onLanguageChange(code) })
            }

            composable("pitch/{tripId}") {
                val detailsViewModel: PathDetailsViewModel = hiltViewModel()
                val detailsState = detailsViewModel.uiState.collectAsState()
                PathDetailsScreen(state = detailsState.value, onLockPath = { id -> homeViewModel.initiateLogistics(id); navController.navigate("logistics_setup/$id") })
            }

            composable("logistics_setup/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val profileState = homeViewModel.logisticsProfile.collectAsState()
                val homeState = homeViewModel.uiState.collectAsState()
                val allTrips = homeState.value.groupedPaths.values.flatten()
                val trip = allTrips.find { it.id == tripId }

                trip?.let {
                    LogisticsWizard(
                        trip = it,
                        currentProfile = profileState.value,
                        onDismiss = { navController.popBackStack() },
                        onConfirm = { newProfile ->
                            homeViewModel.onConfirmLogistics(newProfile)
                            // PROCEED TO RECON
                            navController.navigate("fob_recon/$tripId")
                        }
                    )
                }
            }

            // --- RECONNAISSANCE STEP ---
            composable("fob_recon/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val battleViewModel: BattleViewModel = hiltViewModel()

                FobSetupView(
                    viewModel = battleViewModel,
                    onSetBase = { geoPoint: GeoPoint ->
                        battleViewModel.setFob(geoPoint)
                        navController.navigate("mission_protocol/$tripId")
                    }
                )
            }

            composable("mission_protocol/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val homeState = homeViewModel.uiState.collectAsState()
                val trip = homeState.value.groupedPaths.values.flatten().find { it.id == tripId }

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

            composable("battle/{tripId}") {
                val battleViewModel: BattleViewModel = hiltViewModel()
                BattlePlanScreen(
                    viewModel = battleViewModel,
                    onAbort = {
                        battleViewModel.abortMission()
                        navController.navigate("home") { popUpTo(0) }
                    }
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