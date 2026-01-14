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
import androidx.compose.material.icons.automirrored.filled.AirplaneTicket
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.home.HomeViewModel
import com.example.sakartveloguide.presentation.mission.components.MapViewContainer
import com.example.sakartveloguide.presentation.theme.*
import org.maplibre.android.annotations.PolylineOptions
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
    val dateString = profile.startDate?.let { "${sdf.format(it)} — ${sdf.format(profile.endDate!!)}" } ?: "DATES PENDING"

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

            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                MapViewContainer(modifier = Modifier.fillMaxSize()) { map ->
                    val routePoints = trip.route.map { LatLng(it.latitude, it.longitude) }
                    if (routePoints.isNotEmpty()) {
                        map.addPolyline(PolylineOptions().addAll(routePoints).color(android.graphics.Color.parseColor("#D72638")).width(8f))
                        val bounds = LatLngBounds.Builder().includes(routePoints).build()
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    }
                }
                Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, MatteCharcoal))))
            }

            Column(Modifier.padding(24.dp)) {
                Text("MISSION PROTOCOL", color = SakartveloRed, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                Text(trip.title.uppercase(), color = SnowWhite, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                Text(dateString, color = SakartveloRed, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(32.dp))

                // 1. TACTICAL NODES (BORDERS/AIRPORTS)
                Text("TACTICAL NODES", color = SnowWhite.copy(0.5f), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(12.dp))
                ProtocolPointCard("INFILTRATION", profile.entryPoint, profile.isByAir, viewModel::updateEntryPoint)
                Spacer(Modifier.height(8.dp))
                ProtocolPointCard("EXTRACTION", profile.exitPoint, profile.isByAir, viewModel::updateExitPoint)

                Spacer(Modifier.height(32.dp))

                // 2. LIVE INTERACTIVE THREAD
                Text("TACTICAL THREAD (EDITABLE)", color = SnowWhite.copy(0.5f), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(12.dp))

                previewSteps.forEachIndexed { index, step ->
                    InteractiveStepRow(index + 1, step, context)
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun InteractiveStepRow(index: Int, step: MissionStep, context: android.content.Context) {
    var isExpanded by remember { mutableStateOf(false) }
    val isLogistics = step !is MissionStep.Activity

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isLogistics) { isExpanded = !isExpanded }
            .padding(vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = if (step is MissionStep.PremiumExperience) Color(0xFFFFD700).copy(0.2f) else SakartveloRed.copy(0.1f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(index.toString(), color = if (step is MissionStep.PremiumExperience) Color(0xFFFFD700) else SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(step.title, color = if (step is MissionStep.PremiumExperience) Color(0xFFFFD700) else SnowWhite, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(step.description.take(45) + "...", color = SnowWhite.copy(0.4f), style = MaterialTheme.typography.labelSmall)
            }
            if (isLogistics) {
                Icon(
                    Icons.Default.ChevronRight, null,
                    tint = SnowWhite.copy(0.2f),
                    modifier = Modifier.size(16.dp).rotate(if(isExpanded) 90f else 0f)
                )
            }
        }

        // Expanded Action Box inside the list
        AnimatedVisibility(visible = isExpanded && step.actionUrl != null) {
            Column(Modifier.padding(top = 12.dp, start = 44.dp)) {
                ReferralLinkBox(
                    title = "Secure Asset",
                    description = "Take action on this logistical step.",
                    buttonText = "EXECUTE",
                    icon = Icons.Default.Launch,
                    color = SakartveloRed,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(step.actionUrl))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}