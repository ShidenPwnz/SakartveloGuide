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


            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                MapViewContainer(
                    modifier = Modifier.fillMaxSize(),
                    isInteractable = false // ARCHITECT'S FIX: Laptops and tactical maps don't move
                ) { map ->
                    val points = trip.itinerary.filter { it.location != null }.map {
                        LatLng(it.location!!.latitude, it.location!!.longitude)
                    }
                    // ... rest of map logic ...
                }
            }

            Column(Modifier.padding(24.dp)) {
                Text("MISSION PROTOCOL", color = SakartveloRed, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                Text(trip.title.uppercase(), color = SnowWhite, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text(dateString, color = SakartveloRed, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(32.dp))
                Text("INFILTRATION & EXTRACTION", color = SnowWhite.copy(0.5f), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(12.dp))
                ProtocolPointCard("ARRIVE VIA", profile.entryPoint, profile.isByAir, viewModel::updateEntryPoint)
                Spacer(Modifier.height(8.dp))
                ProtocolPointCard("DEPART VIA", profile.exitPoint, profile.isByAir, viewModel::updateExitPoint)

                Spacer(Modifier.height(32.dp))
                Text("ITINERARY SUMMARY", color = SnowWhite.copy(0.5f), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(12.dp))

                previewSteps.forEachIndexed { index, step ->
                    InteractivePreviewRow(index + 1, step, context)
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun InteractivePreviewRow(index: Int, step: MissionStep, context: android.content.Context) {
    var isExpanded by remember { mutableStateOf(false) }
    val hasTool = step.actionUrl != null

    Column(modifier = Modifier.fillMaxWidth().clickable(enabled = hasTool) { isExpanded = !isExpanded }.padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = if (step is MissionStep.PremiumExperience) Color(0xFFFFD700).copy(alpha = 0.2f) else SakartveloRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = index.toString(), color = if (step is MissionStep.PremiumExperience) Color(0xFFFFD700) else SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(text = step.title, color = if (step is MissionStep.PremiumExperience) Color(0xFFFFD700) else SnowWhite, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = step.description.take(50) + "...", color = SnowWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall)
            }
            if (hasTool) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SnowWhite.copy(alpha = 0.2f), modifier = Modifier.size(16.dp).rotate(if (isExpanded) 90f else 0f))
            }
        }
        AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(Modifier.padding(top = 12.dp, start = 44.dp)) {
                step.actionUrl?.let { url ->
                    ReferralLinkBox(
                        title = "Deploy Asset",
                        description = "Secure this step immediately.",
                        buttonText = "EXECUTE",
                        icon = Icons.Default.Launch,
                        color = if (step is MissionStep.PremiumExperience) Color(0xFFFFD700) else SakartveloRed,
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                    )
                }
            }
        }
    }
}