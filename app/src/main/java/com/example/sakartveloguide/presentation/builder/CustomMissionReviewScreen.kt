package com.example.sakartveloguide.presentation.builder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.domain.model.ExtractionType
import com.example.sakartveloguide.domain.model.TransportStrategy
import com.example.sakartveloguide.presentation.theme.MatteCharcoal
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import com.example.sakartveloguide.presentation.theme.SnowWhite

@Composable
fun CustomMissionReviewScreen(
    viewModel: MissionBriefingViewModel,
    onSetFob: () -> Unit,
    onLaunch: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MatteCharcoal,
        bottomBar = {
            Surface(color = MatteCharcoal, tonalElevation = 8.dp) {
                val isReady = state.fobLocation != null
                Button(
                    onClick = { viewModel.finalizeMission(onLaunch) },
                    modifier = Modifier.fillMaxWidth().padding(24.dp).height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isReady) SakartveloRed else Color.DarkGray),
                    shape = RoundedCornerShape(16.dp),
                    enabled = isReady
                ) {
                    Text(if (isReady) "START ADVENTURE" else "SET BASE LOCATION", fontWeight = FontWeight.Black)
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(24.dp)) {
            item {
                Text("MISSION BRIEFING", color = SakartveloRed, style = MaterialTheme.typography.labelSmall)
                Text(state.tripTitle.uppercase(), color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(24.dp))
            }

            // 1. FLIGHT INTEL
            item {
                BriefingLogisticsCard(title = "INFILTRATION VECTOR", subtitle = "Tbilisi International (TBS)", icon = Icons.Default.Flight, color = Color(0xFF00D7E1))
                VerticalConnector()
            }

            // 2. FOB
            item {
                BriefingLogisticsCard(
                    title = "STARTING BASE (FOB)",
                    subtitle = if (state.fobLocation == null) "LOCATION PENDING" else "Accommodation Secured",
                    icon = Icons.Default.Home,
                    color = if (state.fobLocation == null) SakartveloRed else Color(0xFF4A90E2),
                    actionText = "SET",
                    onAction = onSetFob
                )
                VerticalConnector()
            }

            // 3. MOBILITY
            item {
                BriefingTransportCard(current = state.profile.transportStrategy, onSelected = { viewModel.updateTransport(it) })
                VerticalConnector()
            }

            // 4. STOPS
            itemsIndexed(state.stops) { index, stop ->
                BriefingLogisticsCard(title = "TARGET ${index + 1}", subtitle = stop.name, icon = Icons.Default.LocationOn, color = Color.White.copy(0.2f), isTarget = true)
                VerticalConnector()
            }

            // 5. SMART ARRANGE
            item {
                Button(
                    onClick = { viewModel.optimizeLoadout() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.05f)),
                    border = BorderStroke(1.dp, if(state.fobLocation != null) SakartveloRed else Color.Gray),
                    enabled = state.fobLocation != null
                ) {
                    Icon(Icons.Default.AutoFixHigh, null, tint = if(state.fobLocation != null) SakartveloRed else Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    Text("SMART ARRANGE ROUTE", color = Color.White, fontWeight = FontWeight.Bold)
                }
                VerticalConnector()
            }

            // 6. EXTRACTION
            item {
                BriefingLogisticsCard(
                    title = "EXTRACTION POINT",
                    subtitle = if (state.extractionType == ExtractionType.RETURN_TO_FOB) "Return to FOB" else "Airport Extraction (TBS)",
                    icon = if (state.extractionType == ExtractionType.RETURN_TO_FOB) Icons.Default.KeyboardReturn else Icons.Default.FlightTakeoff,
                    color = Color(0xFF4CAF50),
                    actionText = "TOGGLE",
                    onAction = { viewModel.toggleExtraction() }
                )
            }
        }
    }
}

// ... Keep BriefingLogisticsCard, BriefingTransportCard, TransportChip, VerticalConnector from previous paste ...
@Composable
fun BriefingLogisticsCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, isTarget: Boolean = false, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Surface(color = if (isTarget) Color.White.copy(0.05f) else color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (isTarget) Color.White.copy(0.1f) else color.copy(alpha = 0.4f))) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isTarget) Color.White else color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = if (isTarget) Color.White.copy(0.5f) else color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            if (actionText != null && onAction != null) {
                IconButton(onClick = onAction) { Icon(Icons.Default.Edit, null, tint = SakartveloRed) }
            }
        }
    }
}

@Composable
fun BriefingTransportCard(current: TransportStrategy, onSelected: (TransportStrategy) -> Unit) {
    Surface(color = Color.White.copy(0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
        Column(Modifier.padding(16.dp)) {
            Text("MOBILITY ASSETS", color = SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransportChip("TAXI", Icons.Default.LocalTaxi, current == TransportStrategy.PASSENGER_URBAN) { onSelected(TransportStrategy.PASSENGER_URBAN) }
                TransportChip("4X4", Icons.Default.DirectionsCar, current == TransportStrategy.DRIVER_RENTAL) { onSelected(TransportStrategy.DRIVER_RENTAL) }
            }
        }
    }
}

@Composable
fun TransportChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, color = if (isSelected) SakartveloRed.copy(0.2f) else Color.Transparent, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, if (isSelected) SakartveloRed else Color.White.copy(0.1f))) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isSelected) SakartveloRed else Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VerticalConnector() {
    Box(modifier = Modifier.fillMaxWidth().height(16.dp), contentAlignment = Alignment.CenterStart) {
        Box(modifier = Modifier.padding(start = 28.dp).width(2.dp).fillMaxHeight().background(Color.White.copy(0.05f)))
    }
}