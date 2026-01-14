package com.example.sakartveloguide.presentation.battle

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.home.HomeViewModel
import com.example.sakartveloguide.presentation.theme.*

@Composable
fun BattlePlanScreen(
    path: TripPath,
    viewModel: HomeViewModel,
    onFinish: () -> Unit,
    onAbort: () -> Unit
) {
    val thread by viewModel.missionThread.collectAsState()
    val activeIndex by viewModel.activeStepIndex.collectAsState()
    val profile by viewModel.logisticsProfile.collectAsState()
    val listState = rememberLazyListState()

    var showAbortDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = true) { showAbortDialog = true }

    if (showAbortDialog) {
        AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            containerColor = MatteCharcoal,
            title = { Text(text = "MISSION IN PROGRESS", color = SakartveloRed, fontWeight = FontWeight.Black) },
            text = { Text(text = "Aborting now will reset all objective logs. Proceed?", color = SnowWhite) },
            confirmButton = { Button(onClick = { showAbortDialog = false; onAbort() }, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) { Text("ABORT") } },
            dismissButton = { TextButton(onClick = { showAbortDialog = false }) { Text(text = "HOLD POSITION", color = SnowWhite.copy(alpha = 0.6f)) } }
        )
    }

    LaunchedEffect(activeIndex) { if (thread.isNotEmpty()) listState.animateScrollToItem(activeIndex) }

    Scaffold(
        containerColor = MatteCharcoal,
        bottomBar = {
            if (activeIndex >= thread.size - 1 && thread.isNotEmpty()) {
                Surface(color = MatteCharcoal, tonalElevation = 12.dp) {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.fillMaxWidth().padding(24.dp).height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("FINALIZE & STAMP PASSPORT", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(state = listState, modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
            item { MissionHeaderTiny(path, profile, onAbort = { showAbortDialog = true }) }

            itemsIndexed(thread) { index, step ->
                val status = when {
                    index < activeIndex -> StepStatus.SECURED
                    index == activeIndex -> StepStatus.ACTIVE
                    else -> StepStatus.PLANNED
                }
                ObjectiveCard(step, status) { viewModel.onObjectiveSecured() }
            }
        }
    }
}

@Composable
fun ObjectiveCard(step: MissionStep, status: StepStatus, onSecure: () -> Unit) {
    val context = LocalContext.current
    val alpha = if (status == StepStatus.SECURED) 0.35f else if (status == StepStatus.ACTIVE) 1f else 0.6f

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).alpha(alpha),
        colors = CardDefaults.cardColors(containerColor = SnowWhite.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            if (step is MissionStep.TacticalBridge) {
                // TACTICAL HEADER
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when {
                        step.primaryMode == "WALK" -> Icons.Default.DirectionsWalk
                        step.primaryMode.contains("HYBRID") -> Icons.Default.SwapCalls
                        step.primaryMode == "DRIVE" -> Icons.Default.DirectionsCar
                        else -> Icons.Default.LocalTaxi
                    }

                    val distanceLabel = if (step.distanceKm < 1.0)
                        "${(step.distanceKm * 1000).toInt()} M"
                    else
                        "${"%.1f".format(step.distanceKm)} KM"

                    Icon(icon, null, tint = SakartveloRed, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(text = "$distanceLabel • ${step.title}", color = SnowWhite, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                }

                Spacer(Modifier.height(8.dp))
                Text(text = step.description, color = SnowWhite.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)

                if (status == StepStatus.ACTIVE) {
                    Spacer(Modifier.height(20.dp))

                    // --- DYNAMIC BUTTON LOGIC ---

                    // SCENARIO 1: HYBRID CAR (Show WALK + DRIVE)
                    if (step.primaryMode == "HYBRID_CAR") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            step.walkUrl?.let { url ->
                                TransitButton(Modifier.weight(1f), "WALK", Icons.Default.DirectionsWalk, Color.Gray) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }
                            step.driveUrl?.let { url ->
                                TransitButton(Modifier.weight(1f), "DRIVE", Icons.Default.DirectionsCar, SakartveloRed) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }
                        }
                    }
                    // SCENARIO 2: HYBRID FOOT (Show WALK + TAXI)
                    else if (step.primaryMode == "HYBRID_FOOT") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            step.walkUrl?.let { url ->
                                TransitButton(Modifier.weight(1f), "WALK", Icons.Default.DirectionsWalk, Color.Gray) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }
                            step.boltUrl?.let { url ->
                                TransitButton(Modifier.weight(1f), "BOLT", Icons.Default.LocalTaxi, Color(0xFF32BB78)) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }
                        }
                    }
                    // SCENARIO 3: PURE WALK
                    else if (step.primaryMode == "WALK") {
                        step.walkUrl?.let { url ->
                            TransitButton(Modifier.fillMaxWidth(), "WALK (MAPS)", Icons.Default.DirectionsWalk, Color.Gray) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        }
                    }
                    // SCENARIO 4: PURE DRIVE
                    else if (step.primaryMode == "DRIVE") {
                        step.driveUrl?.let { url ->
                            TransitButton(Modifier.fillMaxWidth(), "DRIVE (MAPS)", Icons.Default.DirectionsCar, SakartveloRed) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        }
                    }
                    // SCENARIO 5: TAXI / TRANSIT
                    else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            step.busUrl?.let { url ->
                                TransitButton(Modifier.weight(1f), "BUS", Icons.Default.DirectionsBus, Color(0xFF5D3FD3)) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }
                            step.boltUrl?.let { url ->
                                TransitButton(Modifier.weight(1f), "BOLT", Icons.Default.LocalTaxi, Color(0xFF32BB78)) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onSecure, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed), shape = RoundedCornerShape(16.dp)) {
                        Text("ARRIVED AT DESTINATION", fontWeight = FontWeight.Black)
                    }
                }
            } else {
                // STANDARD UI (No Changes Here)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = if (status == StepStatus.SECURED) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = if (status == StepStatus.SECURED) Color.Green else SakartveloRed, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(text = step.title, fontWeight = FontWeight.Black, color = SnowWhite)
                }
                Spacer(Modifier.height(8.dp))
                Text(text = step.description, color = SnowWhite.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                if (status == StepStatus.ACTIVE) {
                    Spacer(Modifier.height(20.dp))
                    step.actionUrl?.let { url ->
                        Button(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = SnowWhite.copy(0.1f))) {
                            Text(text = "EXECUTE LINK", color = SnowWhite)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Button(onClick = onSecure, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed), shape = RoundedCornerShape(12.dp)) {
                        Text("OBJECTIVE SECURED", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun TransitButton(modifier: Modifier, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = color)
        Spacer(Modifier.width(8.dp))
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun MissionHeaderTiny(path: TripPath, profile: LogisticsProfile?, onAbort: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(WineDark)) {
        AsyncImage(
            model = path.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.4f)
        )
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, MatteCharcoal)))
        )
        Row(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Surface(color = SakartveloRed, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = "ACTIVE MISSION",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = SnowWhite,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = path.title.uppercase(),
                    color = SnowWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
            IconButton(
                onClick = onAbort,
                modifier = Modifier.background(SnowWhite.copy(alpha = 0.1f), CircleShape)
                    .align(Alignment.Top)
            ) {
                Icon(Icons.Default.Close, null, tint = SnowWhite)
            }
        }
    }
}