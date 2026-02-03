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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.R
import com.example.sakartveloguide.data.local.entity.LocationEntity
import com.example.sakartveloguide.presentation.planner.TripMode
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import com.example.sakartveloguide.domain.util.getDisplayName
import com.example.sakartveloguide.domain.util.getDisplayDesc

@Composable
fun ItineraryCard(
    node: LocationEntity,
    lang: String,
    distFromPrev: Double?,
    mode: TripMode,
    isActive: Boolean,
    isExpanded: Boolean,
    isCompleted: Boolean,
    isSmall: Boolean = false,
    onMapClick: () -> Unit,
    onTaxiClick: () -> Unit,
    onRentClick: () -> Unit,
    onMoreInfo: () -> Unit,
    onCheckIn: () -> Unit,
    onRemove: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    navModifier: Modifier = Modifier,
    actionButtonModifier: Modifier = Modifier
) {
    val isHomeNode = node.type == "HOME"
    val showActiveVisuals = mode == TripMode.LIVE && isActive

    val borderColor = if (showActiveVisuals) SakartveloRed else if (isHomeNode && !isCompleted) SakartveloRed.copy(alpha = 0.5f) else Color.Transparent
    val cardAlpha = if (isCompleted && !isHomeNode) 0.6f else 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (distFromPrev != null) {
            Box(navModifier) {
                TacticalConnector(distFromPrev, showActiveVisuals, onMapClick, onTaxiClick, onRentClick)
            }
        } else {
            Spacer(Modifier.height(16.dp))
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable { onCardClick() },
            shape = if (showActiveVisuals && node.id != -1) RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) else RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, borderColor),
            shadowElevation = if (showActiveVisuals) 12.dp else 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                Box(Modifier.height(if (isSmall && !isExpanded) 120.dp else 180.dp)) {
                    AsyncImage(
                        model = node.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = cardAlpha
                    )
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.9f)))))

                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Surface(color = SakartveloRed, shape = RoundedCornerShape(4.dp)) {
                            Text(node.region.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        if (mode == TripMode.EDITING && !isCompleted && !isHomeNode) {
                            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp).background(Color.Black.copy(0.5f), CircleShape)) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        } else if (isCompleted) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                        }
                    }
                    Text(node.getDisplayName(lang), color = Color.White, fontWeight = FontWeight.Black, fontSize = if(isSmall && !isExpanded) 16.sp else 20.sp, maxLines = 2, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
                }

                if (isExpanded && !isHomeNode) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = node.getDisplayDesc(lang), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onMoreInfo, modifier = Modifier.fillMaxWidth().height(40.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("MORE INFO", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showActiveVisuals && node.id != -1) {
            Surface(
                onClick = { onCheckIn() },
                color = SakartveloRed,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = actionButtonModifier.fillMaxWidth().height(48.dp)
            ) {
                // ARCHITECT'S FIX: Explicit Arrangement.Center
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(if (isHomeNode) Icons.Default.Home else Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(text = if (isHomeNode) "FINISH MISSION" else "CHECK IN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun TacticalConnector(dist: Double, showControls: Boolean, onMap: () -> Unit, onTaxi: () -> Unit, onRent: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
        DottedLine(height = 12.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showControls) { DiscreetPill(Icons.Default.Map, "MAP", onMap); DottedHorizontal(8.dp) }
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(50), border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.1f))) {
                Text(text = formatDistance(dist), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(0.7f), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
            if (showControls) {
                DottedHorizontal(8.dp)
                if (dist > 35.0) { DiscreetPill(Icons.Default.Key, "RENT", onRent) }
                else { DiscreetPill(Icons.Default.LocalTaxi, "TAXI", onTaxi) }
            }
        }
        DottedLine(height = 12.dp)
    }
}

@Composable
fun DiscreetPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.2f))) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurface.copy(0.8f))
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
        }
    }
}

@Composable
fun DottedLine(height: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.height(height).width(2.dp)) {
        val strokeWidth = 4f
        val dashPath = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawLine(color = Color.Gray.copy(alpha = 0.4f), start = Offset(size.width / 2f, 0f), end = Offset(size.width / 2f, size.height), strokeWidth = strokeWidth, pathEffect = dashPath)
    }
}

@Composable
fun DottedHorizontal(width: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.width(width).height(2.dp)) {
        val strokeWidth = 4f
        val dashPath = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
        drawLine(color = Color.Gray.copy(alpha = 0.4f), start = Offset(0f, size.height / 2f), end = Offset(size.width, size.height / 2f), strokeWidth = strokeWidth, pathEffect = dashPath)
    }
}

fun formatDistance(km: Double): String = if (km < 1.0) "${(km * 1000).toInt()} m" else "~${String.format("%.1f", km)} km"