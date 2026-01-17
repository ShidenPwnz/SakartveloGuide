package com.example.sakartveloguide.presentation.battle

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.R
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
    // Collect states explicitly
    val thread by viewModel.missionThread.collectAsState()
    val activeIndex by viewModel.activeStepIndex.collectAsState()
    val showOutOfRange by viewModel.showOutOfRangeDialog.collectAsState()
    val listState = rememberLazyListState()

    // --- ANTI-CHEAT ERROR DIALOG ---
    if (showOutOfRange) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOutOfRangeDialog() },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(stringResource(R.string.error_gps_title), color = SakartveloRed, fontWeight = FontWeight.Black) },
            text = { Text(stringResource(R.string.error_gps_desc), color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                Button(onClick = { viewModel.dismissOutOfRangeDialog() }, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) {
                    Text(stringResource(R.string.error_gps_btn), color = Color.White)
                }
            }
        )
    }

    var showAbortDialog by remember { mutableStateOf(false) }
    BackHandler { showAbortDialog = true }

    // --- ABORT CONFIRMATION DIALOG ---
    if (showAbortDialog) {
        AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("MISSION IN PROGRESS", color = SakartveloRed, fontWeight = FontWeight.Black) },
            text = { Text("Aborting will reset tactical logs. Proceed?", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                Button(onClick = { showAbortDialog = false; onAbort() }, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) {
                    Text(stringResource(R.string.btn_abort), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbortDialog = false }) {
                    Text(stringResource(R.string.btn_hold), color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                }
            }
        )
    }

    LaunchedEffect(activeIndex) {
        if (thread.isNotEmpty()) listState.animateScrollToItem(activeIndex)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (activeIndex >= thread.size - 1 && thread.isNotEmpty()) {
                Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 12.dp) {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.fillMaxWidth().padding(24.dp).height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.btn_finalize), fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { MissionHeaderTiny(path, onAbort = { showAbortDialog = true }) }

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
    val isDebriefing = step is MissionStep.PremiumExperience
    val borderColor = if (status == StepStatus.ACTIVE) (if (isDebriefing) Color(0xFFFFD700) else SakartveloRed) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .alpha(alpha)
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = if (status == StepStatus.ACTIVE) BorderStroke(1.dp, borderColor) else null
    ) {
        Column(Modifier.padding(20.dp)) {
            if (step is MissionStep.TacticalBridge) {
                // --- TACTICAL BRIDGE UI ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val distanceLabel = if (step.distanceKm < 1.0) "${(step.distanceKm * 1000).toInt()} M" else "${"%.1f".format(step.distanceKm)} KM"
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = SakartveloRed, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(text = "$distanceLabel • ${step.title}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                }
                step.warningTag?.let { Text("⚠ $it", color = SakartveloRed, style = MaterialTheme.typography.labelSmall) }
                Spacer(Modifier.height(4.dp))
                Text(step.description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)

                if (status == StepStatus.ACTIVE) {
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransitButton(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.btn_walk),
                            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        ) {
                            val url = step.walkUrl ?: step.driveUrl ?: step.busUrl
                            url?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
                        }

                        step.boltUrl?.let { url ->
                            TransitButton(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.btn_bolt),
                                icon = Icons.Default.LocalTaxi,
                                color = Color(0xFF32BB78)
                            ) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onSecure, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) {
                        Text(stringResource(R.string.btn_reached), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                // --- STANDARD ACTIVITY UI ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if(isDebriefing) Icons.Default.Star else if (status == StepStatus.SECURED) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if(isDebriefing) Color(0xFFFFD700) else if (status == StepStatus.SECURED) Color.Green else SakartveloRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(text = step.title.uppercase(), fontWeight = FontWeight.Black, color = if(isDebriefing) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(8.dp))
                Text(step.description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)

                if (status == StepStatus.ACTIVE) {
                    Spacer(Modifier.height(20.dp))
                    step.actionUrl?.let { url ->
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = if(isDebriefing) Color(0xFFFFD700).copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(0.1f)),
                            border = if(isDebriefing) BorderStroke(1.dp, Color(0xFFFFD700)) else null
                        ) {
                            Text(if(isDebriefing) stringResource(R.string.btn_rate) else "EXECUTE", color = if(isDebriefing) Color(0xFFFFD700) else SakartveloRed, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (!isDebriefing) {
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onSecure, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)) {
                            Text(stringResource(R.string.btn_secured), fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransitButton(
    modifier: Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
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
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun MissionHeaderTiny(path: TripPath, onAbort: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(WineDark)) {
        AsyncImage(
            model = path.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.4f)
        )
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MatteCharcoal))))
        Row(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Surface(color = SakartveloRed, shape = RoundedCornerShape(4.dp)) {
                    Text(text = "ACTIVE MISSION", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                // Extracting English title for the header consistency
                Text(text = path.title.get("en").uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            IconButton(onClick = onAbort, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape).align(Alignment.Top)) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}