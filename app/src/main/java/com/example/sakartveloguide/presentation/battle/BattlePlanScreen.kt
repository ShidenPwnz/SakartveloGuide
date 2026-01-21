package com.example.sakartveloguide.presentation.battle

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.battle.components.*
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BattlePlanScreen(
    viewModel: BattleViewModel,
    onAbort: () -> Unit
) {
    // 1. Collect Data
    val missionState by viewModel.missionState.collectAsState()
    val trip by viewModel.currentTrip.collectAsState()
    val session by viewModel.userSession.collectAsState(initial = UserSession())
    val profile by viewModel.logisticsProfile.collectAsState()

    val context = LocalContext.current
    val listState = rememberLazyListState()

    // 2. Abort/Cancel Dialog
    var showAbortDialog by remember { mutableStateOf(false) }
    BackHandler { showAbortDialog = true }

    if (showAbortDialog) {
        AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            title = { Text("END TRIP?", fontWeight = FontWeight.Black, color = SakartveloRed) },
            text = { Text("You will lose your current progress on this route. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        showAbortDialog = false
                        viewModel.abortMission()
                        onAbort()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)
                ) { Text("END TRIP") }
            },
            dismissButton = {
                TextButton(onClick = { showAbortDialog = false }) { Text("CONTINUE") }
            }
        )
    }

    // 3. Main Interface Logic
    if (missionState.fobLocation == null) {
        // Step A: "Where are you staying?"
        FobSetupView(
            viewModel = viewModel,
            onSetBase = { geo ->
                // ARCHITECT'S FIX: Provide the required onSuccess lambda
                viewModel.setFob(geo) {
                    // No navigation needed here; the state change (fobLocation != null)
                    // will automatically trigger the UI recomposition to the 'else' block below.
                }
            }
        )
    } else {
        // Step B: The Active Itinerary
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.getExfilIntent()?.let { context.startActivity(it) } },
                    containerColor = SakartveloRed,
                    contentColor = Color.White
                ) { Icon(Icons.Default.Home, contentDescription = "Go Home") }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "YOUR ITINERARY",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    actions = {
                        IconButton(onClick = { showAbortDialog = true }) {
                            Icon(Icons.Default.Close, null, tint = SakartveloRed)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {

                // Current Activity Header
                SortieHeader(
                    tripTitle = trip?.title?.get(session.language) ?: "JOURNEY",
                    activeTargetTitle = missionState.activeNodeIndex?.let { idx ->
                        trip?.itinerary?.getOrNull(idx)?.title?.get(session.language)
                    }
                )

                // The Day-by-Day List
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    trip?.let { activeTrip ->
                        // Group items by "D1", "D2", etc.
                        val groupedNodes = activeTrip.itinerary.groupBy { node ->
                            node.timeLabel.take(2) // Assumes format "D1 10:00"
                        }

                        groupedNodes.forEach { (dayCode, nodes) ->

                            // Sticky Day Header
                            stickyHeader {
                                DayHeader(dayCode = dayCode)
                            }

                            // Items for that day
                            itemsIndexed(nodes) { _, node ->
                                // Find the REAL index in the full list for logic
                                val realIndex = activeTrip.itinerary.indexOf(node)

                                val status = when {
                                    missionState.completedNodeIndices.contains(realIndex) -> TargetStatus.NEUTRALIZED
                                    missionState.activeNodeIndex == realIndex -> TargetStatus.ENGAGED
                                    else -> TargetStatus.AVAILABLE
                                }

                                val tacticalAction = viewModel.determineAction(
                                    node = node,
                                    status = status,
                                    distanceKm = viewModel.calculateDistance(node.location),
                                    profile = profile
                                )

                                TargetCard(
                                    node = node,
                                    status = status,
                                    action = tacticalAction,
                                    language = session.language,
                                    distanceKm = viewModel.calculateDistance(node.location),
                                    onEngage = {
                                        viewModel.engageTarget(realIndex)
                                    },
                                    onExecuteAction = { action ->
                                        if (action is TacticalAction.Execute) {
                                            if (action.intent != null) {
                                                context.startActivity(action.intent)
                                            } else {
                                                // Local action (e.g. Check In)
                                                viewModel.neutralizeTarget(realIndex)
                                            }
                                        }
                                    }
                                )

                                // Auto-scroll to active item
                                LaunchedEffect(status) {
                                    if (status == TargetStatus.ENGAGED) {
                                        // Small delay ensures layout is ready
                                        listState.animateScrollToItem(realIndex)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER COMPONENT ---

@Composable
fun DayHeader(dayCode: String) {
    // Friendly Day Names
    val dayTitle = when(dayCode) {
        "D1" -> "DAY 1: ARRIVAL & CITY"
        "D2" -> "DAY 2: EXPLORING DEEPER"
        "D3" -> "DAY 3: THE FINAL STRETCH"
        "D4" -> "DAY 4: BONUS LOCATIONS"
        "ST" -> "LOGISTICS" // For START node
        "EN" -> "EXTRACTION" // For END node
        else -> "ITINERARY"
    }

    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Visual accent line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(SakartveloRed, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = dayTitle,
                style = MaterialTheme.typography.titleMedium,
                color = SakartveloRed,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}