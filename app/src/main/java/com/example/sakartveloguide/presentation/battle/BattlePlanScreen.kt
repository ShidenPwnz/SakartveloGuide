package com.example.sakartveloguide.presentation.battle

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.battle.components.*
import com.example.sakartveloguide.presentation.mission.components.MapViewContainer
import com.example.sakartveloguide.presentation.theme.MatteCharcoal
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BattlePlanScreen(
    viewModel: BattleViewModel,
    onAbort: () -> Unit
) {
    val missionState by viewModel.missionState.collectAsState()
    val trip by viewModel.currentTrip.collectAsState()
    val session by viewModel.userSession.collectAsState(initial = UserSession())
    val profile by viewModel.logisticsProfile.collectAsState()

    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Request high-accuracy GPS exactly once upon entering battle mode
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    var showAbortDialog by remember { mutableStateOf(false) }
    BackHandler { showAbortDialog = true }

    if (showAbortDialog) {
        AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            title = { Text("ABORT MISSION?", fontWeight = FontWeight.Black, color = SakartveloRed) },
            text = { Text("Current tactical progress and FOB coordinates will be purged from the dashboard.") },
            confirmButton = {
                Button(onClick = {
                    showAbortDialog = false
                    viewModel.abortMission()
                    onAbort()
                }, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) {
                    Text("CONFIRM ABORT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbortDialog = false }) { Text("CANCEL") }
            }
        )
    }

    if (missionState.fobLocation == null) {
        FobSetupView(viewModel = viewModel, onSetBase = { geo -> viewModel.setFob(geo) {} })
    } else {
        Scaffold(
            containerColor = MatteCharcoal,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.getExfilIntent()?.let { context.startActivity(it) } },
                    containerColor = SakartveloRed,
                    contentColor = Color.White
                ) { Icon(Icons.Default.Home, null) }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {

                // 1. HEADER: TACTICAL MAP
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    MapViewContainer(modifier = Modifier.fillMaxSize(), isInteractable = true) { map ->
                        trip?.let { activeTrip ->
                            val points = activeTrip.itinerary.mapNotNull { it.location }
                                .map { LatLng(it.latitude, it.longitude) }

                            if (points.isNotEmpty()) {
                                @Suppress("DEPRECATION")
                                points.forEach { map.addMarker(MarkerOptions().position(it)) }

                                val bounds = LatLngBounds.Builder().includes(points).build()
                                map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1500)
                            }
                        }
                    }

                    // Gradient overlay to blend map into list
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, MatteCharcoal)
                                )
                            )
                    )

                    // Close/Abort Mission Action
                    IconButton(
                        onClick = { showAbortDialog = true },
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(0.4f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                // 2. HEADER: SORTIE STATUS
                SortieHeader(
                    tripTitle = trip?.title?.get(session.language) ?: "MISSION",
                    activeTargetTitle = missionState.activeNodeIndex?.let {
                        trip?.itinerary?.getOrNull(it)?.title?.get(session.language)
                    }
                )

                // 3. ITINERARY LIST
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    trip?.let { activeTrip ->
                        // Grouping Logic: START/END nodes go to Logistics, D1/D2 go to Days
                        val groupedNodes = activeTrip.itinerary.groupBy { node ->
                            when (node.timeLabel) {
                                "START", "END" -> "LOG"
                                else -> node.timeLabel.take(2)
                            }
                        }

                        groupedNodes.forEach { (dayCode, nodes) ->
                            if (dayCode != "LOG") {
                                stickyHeader { BattleDayHeader(dayCode) }
                            }

                            itemsIndexed(nodes) { _, node ->
                                val realIndex = activeTrip.itinerary.indexOf(node)
                                val status = when {
                                    missionState.completedNodeIndices.contains(realIndex) -> TargetStatus.NEUTRALIZED
                                    missionState.activeNodeIndex == realIndex -> TargetStatus.ENGAGED
                                    else -> TargetStatus.AVAILABLE
                                }

                                TargetCard(
                                    node = node,
                                    status = status,
                                    action = viewModel.determineAction(node, status, viewModel.calculateDistance(node.location), profile),
                                    language = session.language,
                                    distanceKm = viewModel.calculateDistance(node.location),
                                    onEngage = { viewModel.engageTarget(realIndex) },
                                    onExecuteAction = { action ->
                                        if (action is TacticalAction.Execute) {
                                            if (action.intent != null) context.startActivity(action.intent)
                                            else viewModel.neutralizeTarget(realIndex)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ARCHITECT'S FIX: Restored missing component for sticky headers
@Composable
fun BattleDayHeader(dayCode: String) {
    val title = when (dayCode) {
        "D1" -> "DAY 1: INITIAL STRIKE"
        "D2" -> "DAY 2: CORE OPERATIONS"
        "D3" -> "DAY 3: FINAL OBJECTIVES"
        else -> "OPERATIONAL THEATRE"
    }

    Surface(
        color = MatteCharcoal.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .background(SakartveloRed, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                color = SakartveloRed,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}