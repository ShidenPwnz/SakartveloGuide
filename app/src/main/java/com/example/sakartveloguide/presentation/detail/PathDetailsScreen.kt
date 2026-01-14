package com.example.sakartveloguide.presentation.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sakartveloguide.presentation.theme.MatteCharcoal
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import com.example.sakartveloguide.presentation.theme.SnowWhite

@Composable
fun PathDetailsScreen(
    state: PathDetailsUiState,
    onLockPath: (String) -> Unit
) {
    Scaffold(
        containerColor = MatteCharcoal,
        bottomBar = {
            Surface(tonalElevation = 8.dp, color = MatteCharcoal) {
                Button(
                    onClick = { onLockPath(state.tripId) },
                    modifier = Modifier.fillMaxWidth().padding(24.dp).height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("LOCK MISSION PROTOCOL", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = SakartveloRed)
                }
            } else {
                // TACTICAL HEADER
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("INTEL REPORT", color = SakartveloRed, style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
                    Text(state.title.uppercase(), color = SnowWhite, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)

                    Spacer(Modifier.height(16.dp))

                    // ARCHITECT'S FIX: Snow Warning Logic
                    if (state.stats.hasSnowWarning) {
                        Surface(
                            color = Color(0xFFFF9800).copy(alpha = 0.15f), // Orange Tint
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AcUnit, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("SNOW PROTOCOL ACTIVE", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    Text("4x4 & Chains required Nov-April", color = SnowWhite.copy(0.8f), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TacticalChip("${state.stats.durationDays} DAYS", Icons.Default.CalendarMonth)
                        TacticalChip(state.stats.driveTime, Icons.Default.Schedule)
                        TacticalChip(state.stats.intensity.name, Icons.Default.Landscape)
                    }
                }

                // VERTICAL TIMELINE
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    state.timelineItems.forEachIndexed { index, item ->
                        TimelineNode(
                            item = item,
                            isLast = index == state.timelineItems.lastIndex
                        )
                    }
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun TimelineNode(item: TimelineUiModel, isLast: Boolean) {
    var isExpanded by remember { mutableStateOf(true) }

    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(12.dp).background(SakartveloRed, CircleShape))
            if (!isLast) {
                Box(Modifier.width(1.dp).weight(1f).background(SnowWhite.copy(0.2f)))
            }
        }
        Spacer(Modifier.width(20.dp))
        Column(modifier = Modifier.padding(bottom = 32.dp).animateContentSize()) {
            Text(
                text = item.title,
                color = SnowWhite,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = if (isExpanded) item.fullDescription else item.shortSummary,
                color = SnowWhite.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
            )

            if (isExpanded) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    placeholder = rememberVectorPainter(Icons.Default.Image),
                    error = rememberVectorPainter(Icons.Default.BrokenImage),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun TacticalChip(text: String, icon: ImageVector) {
    Surface(color = SnowWhite.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = SakartveloRed, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = SnowWhite, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
        }
    }
}