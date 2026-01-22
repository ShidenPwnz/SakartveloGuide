package com.example.sakartveloguide.presentation.planner.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.presentation.planner.TripMode
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@Composable
fun ItineraryCard(
    node: LocationEntity,
    distFromPrev: Double?,
    mode: TripMode,
    isActive: Boolean, // Red Border & Buttons
    isExpanded: Boolean, // Text Visibility (Controlled by Parent)
    isCompleted: Boolean,
    onMapClick: () -> Unit,
    onTaxiClick: () -> Unit,
    onCheckIn: () -> Unit,
    onRemove: () -> Unit,
    onCardClick: () -> Unit
) {
    val isHomeNode = node.type == "HOME"
    val showActiveVisuals = mode == TripMode.LIVE && isActive

    val borderColor = if (showActiveVisuals) SakartveloRed else Color.Transparent
    val cardAlpha = if (isCompleted) 0.6f else 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- TACTICAL CLUSTER ---
        if (distFromPrev != null) {
            TacticalConnector(
                dist = distFromPrev,
                showControls = showActiveVisuals,
                onMap = onMapClick,
                onTaxi = onTaxiClick
            )
        } else {
            Spacer(Modifier.height(16.dp))
        }

        // --- THE CARD SURFACE ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize() // Smooth expand/collapse
                .clickable { onCardClick() },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, borderColor),
            shadowElevation = if (showActiveVisuals) 12.dp else 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                // 1. IMAGE HEADER
                Box(Modifier.height(180.dp)) {
                    AsyncImage(
                        model = node.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = cardAlpha
                    )
                    // Gradient
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.9f)))))

                    // Top Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(color = SakartveloRed, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                node.region.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (mode == TripMode.EDITING && !isCompleted && !isHomeNode) {
                            IconButton(
                                onClick = onRemove,
                                modifier = Modifier.size(24.dp).background(Color.Black.copy(0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        } else if (isCompleted) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                        }
                    }

                    Text(
                        node.nameEn,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        maxLines = 2,
                        lineHeight = 24.sp,
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    )
                }

                // 2. DESCRIPTION (Controlled by Parent, BLOCKED for Home)
                if (isExpanded && !isHomeNode) {
                    Text(
                        text = node.descEn,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                    )
                }

                // 3. ACTION BAR (Live Mode Only)
                if (showActiveVisuals && node.id != -1) {
                    Surface(
                        onClick = onCheckIn,
                        color = SakartveloRed,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                if (isHomeNode) Icons.Default.Home else Icons.Default.CheckCircle,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isHomeNode) "ARRIVE HOME & FINISH" else "CHECK IN",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TacticalConnector(
    dist: Double,
    showControls: Boolean,
    onMap: () -> Unit,
    onTaxi: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        DottedLine(height = 12.dp)

        Row(verticalAlignment = Alignment.CenterVertically) {

            // MAP PILL (Left)
            if (showControls) {
                DiscreetPill(Icons.Default.Map, "MAP", onMap)
                DottedHorizontal(8.dp)
            }

            // DISTANCE PILL (Center)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.1f))
            ) {
                Text(
                    text = formatDistance(dist),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // TAXI PILL (Right)
            if (showControls) {
                DottedHorizontal(8.dp)
                DiscreetPill(Icons.Default.LocalTaxi, "TAXI", onTaxi)
            }
        }

        DottedLine(height = 12.dp)
    }
}

@Composable
fun DiscreetPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurface.copy(0.8f))
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
        }
    }
}

@Composable
fun DottedLine(height: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.height(height).width(2.dp)) {
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 4f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

@Composable
fun DottedHorizontal(width: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.width(width).height(2.dp)) {
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 4f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
        )
    }
}

fun formatDistance(km: Double): String {
    return if (km < 1.0) {
        "${(km * 1000).toInt()} m"
    } else {
        "${String.format("%.1f", km)} km"
    }
}