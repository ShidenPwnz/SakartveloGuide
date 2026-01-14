package com.example.sakartveloguide.presentation.mission

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.home.HomeViewModel
import com.example.sakartveloguide.presentation.mission.components.MapViewContainer
import com.example.sakartveloguide.presentation.theme.*
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MissionControlScreen(
    trip: TripPath,
    viewModel: HomeViewModel,
    onStartTrip: () -> Unit,
    onReconfigure: () -> Unit
) {
    val profile by viewModel.logisticsProfile.collectAsState()
    val previewSteps by viewModel.previewThread.collectAsState()
    val context = LocalContext.current

    val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
    val dateString = profile.startDate?.let {
        "${sdf.format(it)} — ${sdf.format(profile.endDate!!)}"
    } ?: "DATES PENDING"

    Scaffold(
        containerColor = MatteCharcoal,
        bottomBar = {
            Surface(color = MatteCharcoal, tonalElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(24.dp).height(64.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onStartTrip, modifier = Modifier.weight(0.8f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed), shape = RoundedCornerShape(16.dp)) {
                        Text("START MISSION", fontWeight = FontWeight.Black)
                    }
                    Button(onClick = onReconfigure, modifier = Modifier.weight(0.2f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = SnowWhite.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) {
                        Icon(Icons.Default.Tune, null, tint = SnowWhite)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {

            // 1. VISUAL INTEL (Locked Map)
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                MapViewContainer(modifier = Modifier.fillMaxSize(), isInteractable = false) { map ->
                    val points = trip.itinerary.filter { it.location != null }.map {
                        LatLng(it.location!!.latitude, it.location!!.longitude)
                    }
                    if (points.isNotEmpty()) {
                        points.forEach { map.addMarker(MarkerOptions().position(it)) }
                        val bounds = LatLngBounds.Builder().includes(points).build()
                        try {
                            map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150), 1000)
                        } catch (e: Exception) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 13.0))
                        }
                    }
                }
                Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, MatteCharcoal))))
            }

            Column(Modifier.padding(24.dp)) {
                Text("MISSION PROTOCOL", color = SakartveloRed, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                Text(trip.title.uppercase(), color = SnowWhite, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text(dateString, color = SakartveloRed, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(32.dp))

                // 2. LOGISTICS PARAMETERS
                Text("INFILTRATION & EXTRACTION", color = SnowWhite.copy(0.5f), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(12.dp))
                ProtocolPointCard("ARRIVE VIA", profile.entryPoint, profile.isByAir, viewModel::updateEntryPoint)
                Spacer(Modifier.height(8.dp))
                ProtocolPointCard("DEPART VIA", profile.exitPoint, profile.isByAir, viewModel::updateExitPoint)

                Spacer(Modifier.height(32.dp))

                // 3. THE THREAD (The "Disconnected" Section)
                Text("TACTICAL THREAD", color = SnowWhite.copy(0.5f), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(12.dp))

                if (previewSteps.isEmpty()) {
                    // Fallback in case thread is generating
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SakartveloRed)
                } else {
                    previewSteps.forEachIndexed { index, step ->
                        InteractivePreviewRow(index + 1, step, context)
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun InteractivePreviewRow(index: Int, step: MissionStep, context: android.content.Context) {
    var isExpanded by remember { mutableStateOf(false) }

    // 1. Logic Check: Does it have external links?
    val hasTool = when(step) {
        is MissionStep.TacticalBridge -> true
        else -> step.actionUrl != null
    }

    // 2. Logic Check: Is the text long enough to require expansion?
    val isTextLong = step.description.length > 60

    // 3. Final Decision: Should this row be interactable?
    val canExpand = hasTool || isTextLong

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize() // Smooth transition for height changes
            .clickable(enabled = canExpand) { isExpanded = !isExpanded }
            .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // STEP INDICATOR
            Surface(
                color = SakartveloRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = index.toString(),
                        color = SakartveloRed,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // CONTENT BLOCK
            Column(Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    color = SnowWhite,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                // ARCHITECT'S DYNAMIC TEXT:
                // We only truncate if it's long AND not expanded.
                val displayText = if (isExpanded || !isTextLong) {
                    step.description
                } else {
                    step.description.take(57) + "..."
                }

                Text(
                    text = displayText,
                    color = SnowWhite.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )

                // Bridge Specific Intelligence (Always show when expanded)
                if (isExpanded && step is MissionStep.TacticalBridge) {
                    step.warningTag?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("⚠ $it", color = SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    step.specialNote?.let {
                        Spacer(Modifier.height(4.dp))
                        Text("ℹ $it", color = Color(0xFFFFD700), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // DYNAMIC CHEVRON: Only show if the user can actually expand the card
            if (canExpand) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (isExpanded) SakartveloRed else SnowWhite.copy(alpha = 0.2f),
                    modifier = Modifier
                        .rotate(if (isExpanded) 90f else 0f)
                        .padding(top = 4.dp)
                        .size(20.dp)
                )
            }
        }

        // EXECUTABLE ASSETS (Buttons)
        AnimatedVisibility(
            visible = isExpanded && hasTool,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(Modifier.padding(top = 16.dp, start = 44.dp)) {
                val targetUrl = when(step) {
                    is MissionStep.TacticalBridge -> step.walkUrl ?: step.driveUrl ?: step.busUrl ?: step.boltUrl
                    else -> step.actionUrl
                }

                targetUrl?.let { url ->
                    ReferralLinkBox(
                        title = "Deploy Protocol",
                        description = "Launch external asset",
                        buttonText = "EXECUTE",
                        icon = Icons.Default.Launch,
                        color = SakartveloRed
                    ) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
            }
        }
    }
}
