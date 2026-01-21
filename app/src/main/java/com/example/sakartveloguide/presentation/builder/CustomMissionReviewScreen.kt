package com.example.sakartveloguide.presentation.builder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // CRITICAL IMPORT FIXED
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.domain.util.TacticalMath
import com.example.sakartveloguide.presentation.theme.MatteCharcoal
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@Composable
fun CustomMissionReviewScreen(
    viewModel: MissionBriefingViewModel,
    onSetFob: () -> Unit,
    onLaunch: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Improved Drag State Management
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

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

            // 1. INFILTRATION VECTOR
            item {
                ExpandableLogisticsCard(
                    title = "INFILTRATION VECTOR",
                    subtitle = state.profile.entryPoint.name.replace("_", " "),
                    icon = Icons.Default.Flight,
                    color = Color(0xFF00D7E1)
                ) {
                    Button(
                        onClick = { viewModel.openExternalLink("skyscanner") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D7E1))
                    ) {
                        Text("OPEN SKYSCANNER", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. MOBILITY STRATEGY
            item {
                val distArrivalToBase = state.fobLocation?.let { TacticalMath.calculateDistanceKm(state.airportLocation, it) }
                TacticalConnector(distArrivalToBase)
                BriefingTransportCard(
                    current = state.profile.transportStrategy,
                    onSelected = { viewModel.updateTransport(it) },
                    onLinkRequest = { type -> viewModel.openExternalLink(type) }
                )
            }

            // 3. STARTING BASE (FOB)
            item {
                TacticalConnector(null)
                ExpandableLogisticsCard(
                    title = "STARTING BASE (FOB)",
                    subtitle = if (state.fobLocation == null) "LOCATION PENDING" else "Accommodation Secured",
                    icon = Icons.Default.Home,
                    color = if (state.fobLocation == null) SakartveloRed else Color(0xFF4A90E2),
                    actionText = "SET",
                    onAction = onSetFob
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.openExternalLink("booking") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003580))) {
                            Text("BOOKING", fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = { viewModel.openExternalLink("airbnb") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F))) {
                            Text("AIRBNB", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. SORTIE TARGETS (Fixed Drag & Drop Logic)
            itemsIndexed(state.stops) { index, stop ->
                val prevLoc = if (index == 0) state.fobLocation else GeoPoint(state.stops[index-1].latitude, state.stops[index-1].longitude)
                val dist = if (prevLoc != null) TacticalMath.calculateDistanceKm(prevLoc, GeoPoint(stop.latitude, stop.longitude)) else null

                val isDragging = draggedItemIndex == index
                val scale by animateFloatAsState(if (isDragging) 1.04f else 1f, label = "scale")
                val elevation by animateFloatAsState(if (isDragging) 12f else 0f, label = "elevation")
                val containerAlpha by animateFloatAsState(if (draggedItemIndex != null && !isDragging) 0.4f else 1f, label = "ghost")

                // ARCHITECT'S FIX: Use Box with graphicsLayer for high-performance transformations
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(if (isDragging) 10f else 1f)
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.translationY = if (isDragging) dragOffset else 0f
                            this.alpha = containerAlpha
                        }
                ) {
                    Column {
                        TacticalConnector(dist)
                        ExpandableLogisticsCard(
                            title = "TARGET ${index + 1}",
                            subtitle = stop.name,
                            icon = Icons.Default.LocationOn,
                            color = Color.White.copy(0.2f),
                            isTarget = true,
                            elevation = elevation,
                            borderColor = if (isDragging) SakartveloRed else Color.Transparent,

                            // Pointer logic moved to Drag Handle to fix scroll interference
                            dragHandleModifier = Modifier.pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { draggedItemIndex = index },
                                    onDragEnd = {
                                        draggedItemIndex = null
                                        dragOffset = 0f
                                        viewModel.onDragComplete()
                                    },
                                    onDragCancel = {
                                        draggedItemIndex = null
                                        dragOffset = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y

                                        val threshold = 180f
                                        if (dragOffset > threshold && index < state.stops.size - 1) {
                                            viewModel.moveStop(index, index + 1)
                                            draggedItemIndex = index + 1
                                            dragOffset = 0f
                                        } else if (dragOffset < -threshold && index > 0) {
                                            viewModel.moveStop(index, index - 1)
                                            draggedItemIndex = index - 1
                                            dragOffset = 0f
                                        }
                                    }
                                )
                            }
                        ) {
                            Column {
                                Text(stop.description, color = Color.White.copy(0.7f), fontSize = 13.sp, lineHeight = 18.sp)
                                Spacer(Modifier.height(12.dp))
                                if (stop.imageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = stop.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. SMART OPTIMIZATION
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
                ExpandableLogisticsCard(
                    title = "EXTRACTION POINT",
                    subtitle = if (state.extractionType == ExtractionType.RETURN_TO_FOB) "Return to FOB" else "Airport Extraction (TBS)",
                    icon = if (state.extractionType == ExtractionType.RETURN_TO_FOB) Icons.Default.KeyboardReturn else Icons.Default.FlightTakeoff,
                    color = Color(0xFF4CAF50),
                    actionText = "TOGGLE",
                    onAction = { viewModel.toggleExtraction() }
                )
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ExpandableLogisticsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    isTarget: Boolean = false,
    actionText: String? = null,
    dragHandleModifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    elevation: Float = 0f,
    onAction: (() -> Unit)? = null,
    expandedContent: @Composable (() -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isTarget) Color.White.copy(0.05f) else color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (borderColor != Color.Transparent) borderColor else if (isTarget) Color.White.copy(0.1f) else color.copy(alpha = 0.4f)),
        shadowElevation = elevation.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
            ) {
                Icon(icon, null, tint = if (isTarget) Color.White else color, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, color = if (isTarget) Color.White.copy(0.5f) else color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                }

                if (isTarget) {
                    Box(
                        modifier = dragHandleModifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DragHandle, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(28.dp))
                    }
                } else if (actionText != null && onAction != null) {
                    TextButton(onClick = onAction) { Text(actionText, color = SakartveloRed, fontWeight = FontWeight.Black) }
                }
            }

            AnimatedVisibility(visible = isExpanded && expandedContent != null) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.1f)))
                    Spacer(Modifier.height(16.dp))
                    expandedContent?.invoke()
                }
            }
        }
    }
}

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
fun BriefingTransportCard(current: TransportStrategy, onSelected: (TransportStrategy) -> Unit, onLinkRequest: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
        Column(Modifier.padding(16.dp)) {
            Text("MOBILITY ASSETS", color = SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransportChip(Modifier.weight(1f), "TAXI", Icons.Default.LocalTaxi, current == TransportStrategy.PASSENGER_URBAN) { onSelected(TransportStrategy.PASSENGER_URBAN) }
                TransportChip(Modifier.weight(1f), "4X4", Icons.Default.DirectionsCar, current == TransportStrategy.DRIVER_RENTAL) { onSelected(TransportStrategy.DRIVER_RENTAL) }
            }

            Spacer(Modifier.height(12.dp))
            if (current == TransportStrategy.PASSENGER_URBAN) {
                Button(onClick = { onLinkRequest("bolt") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32BB78))) {
                    Text("GET BOLT APP", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else if (current == TransportStrategy.DRIVER_RENTAL) {
                Button(onClick = { onLinkRequest("localrent") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))) {
                    Text("RENT 4x4 (LOCALRENT)", fontWeight = FontWeight.Bold, color = Color.White)
                }
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