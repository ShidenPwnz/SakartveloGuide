package com.example.sakartveloguide.presentation.battle

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.battle.components.*
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattlePlanScreen(
    viewModel: BattleViewModel,
    onAbort: () -> Unit
) {
    val missionState by viewModel.missionState.collectAsState()
    val trip by viewModel.currentTrip.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val session by viewModel.userSession.collectAsState(initial = UserSession())
    val context = LocalContext.current

    var showAbortDialog by remember { mutableStateOf(false) }

    BackHandler { showAbortDialog = true }

    if (showAbortDialog) {
        AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            title = { Text("ABORT MISSION?", fontWeight = FontWeight.Black, color = SakartveloRed) },
            text = { Text("Current tactical progress and FOB coordinates will be purged.") },
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
        FobSetupView(
            viewModel = viewModel,
            onSetBase = { geo -> viewModel.setFob(geo) }
        )
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.getExfilIntent()?.let { context.startActivity(it) } },
                    containerColor = SakartveloRed,
                    contentColor = Color.White
                ) { Icon(Icons.Default.Home, null) }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("TACTICAL DASHBOARD", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge) },
                    actions = {
                        IconButton(onClick = { showAbortDialog = true }) {
                            Icon(Icons.Default.Close, null, tint = SakartveloRed)
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                SortieHeader(
                    tripTitle = trip?.title?.get(session.language) ?: "MISSION",
                    activeTargetTitle = missionState.activeNodeIndex?.let {
                        trip?.itinerary?.get(it)?.title?.get(session.language)
                    }
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    trip?.let { activeTrip ->
                        itemsIndexed(activeTrip.itinerary) { index, node ->
                            val status = when {
                                missionState.completedNodeIndices.contains(index) -> TargetStatus.NEUTRALIZED
                                missionState.activeNodeIndex == index -> TargetStatus.ENGAGED
                                else -> TargetStatus.AVAILABLE
                            }

                            TargetCard(
                                node = node,
                                status = status,
                                language = session.language,
                                distanceKm = viewModel.calculateDistance(node.location),
                                onEngage = { viewModel.engageTarget(index) },
                                onSecure = { viewModel.neutralizeTarget(index) },
                                onNavigate = { mode ->
                                    context.startActivity(viewModel.getNavigationIntent(node.location!!, mode))
                                },
                                onBoltClick = {
                                    context.startActivity(viewModel.getBoltIntent(node.location!!))
                                },
                                onRentalClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.getRentalUrl()))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}