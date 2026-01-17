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
import androidx.compose.ui.graphics.Brush
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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(24.dp).height(64.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onStartTrip,
                        modifier = Modifier.weight(0.8f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("START MISSION", fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Button(
                        onClick = onReconfigure,
                        modifier = Modifier.weight(0.2f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
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
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))))
            }

            Column(Modifier.padding(24.dp)) {
                Text("MISSION PROTOCOL", color = SakartveloRed, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                Text(trip.title.get("en").uppercase(), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text(dateString, color = SakartveloRed, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(32.dp))
                Text("INFILTRATION & EXTRACTION", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(12.dp))
                ProtocolPointCard("ARRIVE VIA", profile.entryPoint, profile.isByAir, viewModel::updateEntryPoint)
                Spacer(Modifier.height(8.dp))
                ProtocolPointCard("DEPART VIA", profile.exitPoint, profile.isByAir, viewModel::updateExitPoint)

                Spacer(Modifier.height(32.dp))
                Text("ITINERARY SUMMARY", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
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
    val isLong = step.description.length > 60
    val hasLink = step.actionUrl != null || step is MissionStep.TacticalBridge
    val canExpand = isLong || hasLink

    Column(modifier = Modifier.fillMaxWidth().animateContentSize().clickable(enabled = canExpand) { isExpanded = !isExpanded }.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Surface(color = SakartveloRed.copy(0.1f), shape = RoundedCornerShape(4.dp), modifier = Modifier.size(28.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = index.toString(), color = SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(step.title, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(if (isExpanded || !isLong) step.description else (step.description.take(57) + "..."), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
            }
            if (canExpand) {
                Icon(Icons.Default.ChevronRight, null, tint = if (isExpanded) SakartveloRed else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), modifier = Modifier.rotate(if (isExpanded) 90f else 0f).padding(top = 4.dp).size(20.dp))
            }
        }
        AnimatedVisibility(visible = isExpanded && hasLink) {
            Column(Modifier.padding(top = 16.dp, start = 44.dp)) {
                val url = when(step) {
                    is MissionStep.TacticalBridge -> step.walkUrl ?: step.driveUrl ?: step.busUrl ?: step.boltUrl
                    else -> step.actionUrl
                }
                url?.let {
                    ReferralLinkBox("Deploy Protocol", "Launch external asset", "OPEN", Icons.Default.Launch, SakartveloRed) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                    }
                }
            }
        }
    }
}