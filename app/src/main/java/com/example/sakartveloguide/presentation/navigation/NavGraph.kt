package com.example.sakartveloguide.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.sakartveloguide.presentation.home.*
import com.example.sakartveloguide.presentation.detail.*
import com.example.sakartveloguide.presentation.mission.*
import com.example.sakartveloguide.presentation.passport.*
import com.example.sakartveloguide.presentation.settings.*
import com.example.sakartveloguide.presentation.battle.*
import com.example.sakartveloguide.presentation.builder.*
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

            // 1. HOME MATRIX
            // ... inside Home composable ...

            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onPathClick = { id ->
                        if (id == "meta_sandbox") {
                            // ARCHITECT'S FIX: Reset GPS & Loadout before entering builder
                            homeViewModel.prepareForNewMission()
                            navController.navigate("custom_builder")
                        } else {
                            navController.navigate("pitch/$id")
                        }
                    },
                    onPassportClick = { navController.navigate("passport") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            // 2. MISSION BUILDER
            composable("custom_builder") {
                val vm: MissionBuilderViewModel = hiltViewModel()
                MissionBuilderScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onProceed = { ids ->
                        val idsStr = ids.joinToString(",")
                        navController.navigate("briefing/custom_cargo?ids=$idsStr")
                    }
                )
            }

            // 3. BRIEFING HUB
            composable(
                route = "briefing/{tripId}?ids={ids}",
                arguments = listOf(
                    navArgument("tripId") { type = NavType.StringType },
                    navArgument("ids") { defaultValue = ""; type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: "custom_cargo"
                val vm: MissionBriefingViewModel = hiltViewModel()

                CustomMissionReviewScreen(
                    viewModel = vm,
                    onSetFob = { navController.navigate("fob_recon/$tripId") }, // PASS TRIP ID
                    onLaunch = { navController.navigate("battle/$tripId") }
                )
            }

            // 4. INTEL REPORT
            composable("pitch/{tripId}") {
                val vm: PathDetailsViewModel = hiltViewModel()
                val state by vm.uiState.collectAsState()
                PathDetailsScreen(state = state) { id ->
                    navController.navigate("briefing/$id")
                }
            }

            // 5. FOB RECON (Accommodation Setup)
            // ARCHITECT'S FIX: Now accepts tripId to stay in sync with the Briefing Hub
            composable("fob_recon/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: "custom_cargo"
                val battleViewModel: BattleViewModel = hiltViewModel()
                
                com.example.sakartveloguide.presentation.battle.components.FobSetupView(
                    viewModel = battleViewModel,
                    onSetBase = { geoPoint ->
                        battleViewModel.setFob(geoPoint) {
                            navController.popBackStack()
                        }
                    }
                )
            }

            // 6. BATTLE PLAN
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

            // 7. SETTINGS
            composable("settings") {
                val sessionState = homeViewModel.userSession.collectAsState(initial = UserSession())
                SettingsScreen(
                    session = sessionState.value,
                    onBack = { navController.popBackStack() },
                    onWipeData = { homeViewModel.wipeAllUserData() },
                    onLanguageChange = { code -> homeViewModel.onLanguageChange(code) }
                )
            }

            // 8. PASSPORT
            composable("passport") {
                val passportViewModel: PassportViewModel = hiltViewModel()
                val stampsState = passportViewModel.stamps.collectAsState()
                PassportScreen(stampsState.value, { navController.popBackStack() })
            }
        }
    }
}
