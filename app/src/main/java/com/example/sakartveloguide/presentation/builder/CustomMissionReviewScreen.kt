package com.example.sakartveloguide.presentation.builder

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.util.TacticalMath
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
    val listState = rememberLazyListState()
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }

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
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                Text("MISSION BRIEFING", color = SakartveloRed, style = MaterialTheme.typography.labelSmall)
                Text(state.tripTitle.uppercase(), color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(24.dp))
            }

            // 1. ARRIVAL (Airport)
            item {
                BriefingLogisticsCard(title = "INFILTRATION VECTOR", subtitle = state.profile.entryPoint.name.replace("_", " "), icon = Icons.Default.Flight, color = Color(0xFF00D7E1))
            }

            // 2. MOBILITY STRATEGY (Corrected alignment)
            item {
                val distArrivalToBase = state.fobLocation?.let { TacticalMath.calculateDistanceKm(state.airportLocation, it) }
                TacticalConnector(distArrivalToBase)
                BriefingTransportCard(current = state.profile.transportStrategy, onSelected = { viewModel.updateTransport(it) })
            }

            // 3. STARTING BASE (FOB)
            item {
                TacticalConnector(null)
                BriefingLogisticsCard(
                    title = "STARTING BASE (FOB)",
                    subtitle = if (state.fobLocation == null) "LOCATION PENDING" else "Accommodation Secured",
                    icon = Icons.Default.Home,
                    color = if (state.fobLocation == null) SakartveloRed else Color(0xFF4A90E2),
                    actionText = "SET",
                    onAction = onSetFob
                )
            }

            // 4. THE SORTIES (Targets with Drag & Distance)
            itemsIndexed(state.stops) { index, stop ->
                // Telemetry: Distance from previous point in the chain
                val prevLoc = if (index == 0) state.fobLocation else GeoPoint(state.stops[index-1].latitude, state.stops[index-1].longitude)
                val dist = if (prevLoc != null) TacticalMath.calculateDistanceKm(prevLoc, GeoPoint(stop.latitude, stop.longitude)) else null

                val isDragging = draggedItemIndex == index
                val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "lift")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(if (isDragging) 1f else 0f)
                        .scale(scale)
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { draggedItemIndex = index },
                                onDragEnd = { draggedItemIndex = null },
                                onDragCancel = { draggedItemIndex = null },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val threshold = 60f
                                    if (dragAmount.y > threshold && index < state.stops.size - 1) {
                                        viewModel.moveStop(index, index + 1)
                                        draggedItemIndex = index + 1
                                    } else if (dragAmount.y < -threshold && index > 0) {
                                        viewModel.moveStop(index, index - 1)
                                        draggedItemIndex = index - 1
                                    }
                                }
                            )
                        }
                ) {
                    TacticalConnector(dist)
                    BriefingLogisticsCard(
                        title = "TARGET ${index + 1}",
                        subtitle = stop.name,
                        icon = Icons.Default.LocationOn,
                        color = Color.White.copy(0.2f),
                        isTarget = true,
                        showDragHandle = true
                    )
                }
            }

            // 5. SMART ARRANGE
            item {
                Spacer(Modifier.height(24.dp))
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
            }

            // 6. EXTRACTION
            item {
                val lastStop = state.stops.lastOrNull()
                val lastLoc = if (lastStop != null) GeoPoint(lastStop.latitude, lastStop.longitude) else state.fobLocation
                val extractLoc = if (state.extractionType == ExtractionType.AIRPORT_EXTRACTION) state.airportLocation else state.fobLocation
                val distExfil = if (lastLoc != null && extractLoc != null) TacticalMath.calculateDistanceKm(lastLoc, extractLoc) else null

                TacticalConnector(distExfil)

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

// --- LOGISTICS COMPONENTS ---

@Composable
fun TacticalConnector(distanceKm: Double?) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 28.dp), horizontalAlignment = Alignment.Start) {
        Box(modifier = Modifier.width(2.dp).height(12.dp).background(Color.White.copy(0.1f)))
        distanceKm?.let {
            Surface(color = Color.Black, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
                Text(text = "${String.format("%.1f", it)} KM", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = SakartveloRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        } ?: Box(modifier = Modifier.size(8.dp))
        Box(modifier = Modifier.width(2.dp).height(12.dp).background(Color.White.copy(0.1f)))
    }
}

@Composable
fun BriefingLogisticsCard(
    title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color,
    isTarget: Boolean = false, actionText: String? = null, showDragHandle: Boolean = false, onAction: (() -> Unit)? = null
) {
    Surface(
        color = if (isTarget) Color.White.copy(0.05f) else color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isTarget) Color.White.copy(0.1f) else color.copy(alpha = 0.4f))
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isTarget) Color.White else color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = if (isTarget) Color.White.copy(0.5f) else color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
            }
            if (showDragHandle) {
                Icon(Icons.Default.DragHandle, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(24.dp))
            } else if (actionText != null && onAction != null) {
                TextButton(onClick = onAction) { Text(actionText, color = SakartveloRed, fontWeight = FontWeight.Black) }
            }
        }
    }
}

@Composable
fun BriefingTransportCard(current: TransportStrategy, onSelected: (TransportStrategy) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
        Column(Modifier.padding(16.dp)) {
            Text("MOBILITY ASSETS", color = SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransportChip(Modifier.weight(1f), "TAXI", Icons.Default.LocalTaxi, current == TransportStrategy.PASSENGER_URBAN) { onSelected(TransportStrategy.PASSENGER_URBAN) }
                TransportChip(Modifier.weight(1f), "4X4", Icons.Default.DirectionsCar, current == TransportStrategy.DRIVER_RENTAL) { onSelected(TransportStrategy.DRIVER_RENTAL) }
            }
        }
    }
}

@Composable
fun TransportChip(modifier: Modifier, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, color = if (isSelected) SakartveloRed.copy(0.2f) else Color.Transparent, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, if (isSelected) SakartveloRed else Color.White.copy(0.1f))) {
        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
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