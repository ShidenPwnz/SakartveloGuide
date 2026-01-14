package com.example.sakartveloguide.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight // FIXED
import androidx.compose.ui.unit.dp // FIXED
import androidx.compose.ui.unit.sp // FIXED
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
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
import com.example.sakartveloguide.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*

@OptIn(ExperimentalMaterial3Api::class)
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
        NavHost(navController = navController, startDestination = startDestination!!) {
            composable(route = "home") {
                HomeScreen(homeViewModel, { id -> navController.navigate("pitch/$id") }, {}, { navController.navigate("passport") }, { navController.navigate("settings") })
            }

            composable(route = "settings") {
                SettingsScreen({ navController.popBackStack() }, { homeViewModel.wipeAllUserData() })
            }

            composable("pitch/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                when (tripId) {
                    "meta_tutorial" -> TutorialContent { navController.popBackStack() }
                    "meta_about" -> AboutSakartveloContent { navController.popBackStack() }
                    else -> {
                        val detailsViewModel: PathDetailsViewModel = hiltViewModel()
                        val state by detailsViewModel.uiState.collectAsState()
                        PathDetailsScreen(state, { id -> homeViewModel.initiateLogistics(id); navController.navigate("logistics_setup/$id") })
                    }
                }
            }

            composable(route = "logistics_setup/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val profile by homeViewModel.logisticsProfile.collectAsState()
                val state by homeViewModel.uiState.collectAsState()
                val trip = state.groupedPaths.values.flatten().find { it.id == tripId }
                trip?.let { LogisticsWizard(it, profile, { navController.popBackStack() }, { p -> homeViewModel.onConfirmLogistics(p) }) }
            }

            composable(route = "mission_protocol/{tripId}") { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                val state by homeViewModel.uiState.collectAsState()
                val trip = state.groupedPaths.values.flatten().find { it.id == tripId }

                trip?.let {
                    // ARCHITECT'S FIX: Removed redundant callbacks.
                    // Point selection is handled internally via viewModel parameter.
                    MissionControlScreen(
                        it,
                        homeViewModel,
                        {
                            homeViewModel.startMission(it)
                            navController.navigate("battle/${it.id}") { popUpTo("home") }
                        },
                        { navController.popBackStack() }
                    )
                }
            }

            composable("battle/{tripId}") { backStackEntry ->
                val state by homeViewModel.uiState.collectAsState()
                val trip = state.groupedPaths.values.flatten().find { it.id == backStackEntry.arguments?.getString("tripId") }
                trip?.let { BattlePlanScreen(it, homeViewModel, { onCompleteTrip(it) }, { homeViewModel.onAbortTrip() }) }
            }

            composable("passport") {
                val passportViewModel: PassportViewModel = hiltViewModel()
                val stamps by passportViewModel.stamps.collectAsState()
                PassportScreen(stamps, { navController.popBackStack() })
            }
        }
    }
}

// ... InfoSection, TutorialContent, AboutSakartveloContent stay identical but now have fixed imports ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialContent(onBack: () -> Unit) {
    Scaffold(containerColor = MatteCharcoal, topBar = { TopAppBar(title = { Text("SYSTEM MANUAL", color = SnowWhite, fontWeight = FontWeight.Black) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = SnowWhite) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MatteCharcoal)) }) { padding ->
        Column(Modifier.padding(padding).padding(24.dp).verticalScroll(rememberScrollState())) {
            InfoSection("THE MATRIX", "Swipe sectors horizontally (Capital, Wine, Mountain). Swipe vertically to choose your mission card.")
            InfoSection("LOGISTICS", "Secure your Flight, Comms (eSIM), Lodging, and Transport before initiating. Red boxes require manual action.")
            InfoSection("EXECUTION", "Follow the tactical thread. Once an objective is secured, tap the red button to advance. Extraction is required for stamp approval.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSakartveloContent(onBack: () -> Unit) {
    Scaffold(containerColor = MatteCharcoal, topBar = { TopAppBar(title = { Text("SECTOR REPORT", color = SnowWhite, fontWeight = FontWeight.Black) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = SnowWhite) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MatteCharcoal)) }) { padding ->
        Column(Modifier.padding(padding).padding(24.dp).verticalScroll(rememberScrollState())) {
            InfoSection("CURRENCY & CASH", "The Georgian Lari (GEL). While cards work in Tbilisi, cash is mandatory for mountain villages and local taxis.")
            InfoSection("COMMUNICATIONS", "Magti offers the best mountain coverage. Use the 'Connectivity' tool in setup to secure a digital SIM.")
            InfoSection("EMERGENCY", "Dial 112 for all emergency services. English operators are available.")
        }
    }
}

@Composable
fun InfoSection(title: String, body: String) {
    Column(Modifier.padding(bottom = 24.dp)) {
        Text(title, color = SakartveloRed, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        Text(body, color = SnowWhite.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
    }
}