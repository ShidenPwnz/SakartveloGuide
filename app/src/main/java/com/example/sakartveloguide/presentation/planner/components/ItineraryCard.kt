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
    isActive: Boolean, // Determines Red Border & Button Visibility
    isCompleted: Boolean,
    onNavigateDrive: () -> Unit,
    onNavigateWalk: () -> Unit,
    onCheckIn: () -> Unit,
    onRemove: () -> Unit,
    onCardClick: () -> Unit // Propagated click for selection
) {
    var isExpanded by remember { mutableStateOf(false) }

    // ARCHITECT'S LOGIC: Home nodes behave differently
    val isHomeNode = node.type == "HOME"

    val borderColor = if (mode == TripMode.LIVE && isActive) SakartveloRed else Color.Transparent
    val cardAlpha = if (isCompleted) 0.6f else 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- THE CONNECTOR ---
        if (distFromPrev != null) {
            TacticalConnector(dist = distFromPrev)
        } else {
            Spacer(Modifier.height(16.dp))
        }

        // --- THE CARD ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize() // Key for smooth expansion
                .clickable {
                    onCardClick() // 1. Always select on click
                    if (!isHomeNode) {
                        isExpanded = !isExpanded // 2. Only expand if NOT home
                    }
                },
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, borderColor),
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                // 1. VISUAL HEADER (Fixed Height)
                Box(Modifier.height(180.dp)) {
                    AsyncImage(
                        model = node.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = cardAlpha
                    )

                    // Gradient for text readability
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.9f)))))

                    // TOP BAR
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
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

                        // Delete / Completed Status
                        if (mode == TripMode.EDITING && !isCompleted && !isHomeNode) {
                            IconButton(
                                onClick = onRemove,
                                modifier = Modifier.size(28.dp).background(Color.Black.copy(0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        } else if (isCompleted) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                        }
                    }

                    // NAME OVERLAY
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

                // 2. EXPANDABLE BODY (Dynamic Height)
                // Only show for non-home nodes
                if (isExpanded && !isHomeNode) {
                    Text(
                        text = node.descEn,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                        // ARCHITECT'S FIX: Allow infinite lines for full description
                        maxLines = Int.MAX_VALUE
                    )
                }

                // 3. ACTION BAR (Only Visible if Active Target)
                if (mode == TripMode.LIVE && isActive) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        if (isHomeNode) {
                            // HOME LOGIC: Just Navigation
                            Button(
                                onClick = onNavigateDrive,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                            ) {
                                Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("NAVIGATE TO BASE", fontWeight = FontWeight.Black, color = Color.White)
                            }
                        } else {
                            // NORMAL LOGIC: Full Suite
                            Button(
                                onClick = onNavigateDrive,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.DirectionsCar, null, tint = MaterialTheme.colorScheme.onSurface)
                            }

                            Button(
                                onClick = onNavigateWalk,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.DirectionsWalk, null, tint = MaterialTheme.colorScheme.onSurface)
                            }

                            Button(
                                onClick = onCheckIn,
                                modifier = Modifier.weight(2f),
                                colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)
                            ) {
                                Text("CHECK IN", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TacticalConnector(dist: Double) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        DottedLine(height = 12.dp)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.1f))
        ) {
            Text(
                text = "${dist.toInt()} km",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        DottedLine(height = 12.dp)
    }
}

@Composable
fun DottedLine(height: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.height(height).width(2.dp)) {
        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 4f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}