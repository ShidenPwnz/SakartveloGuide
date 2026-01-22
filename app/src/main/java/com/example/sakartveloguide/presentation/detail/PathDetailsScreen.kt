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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sakartveloguide.R
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@Composable
fun PathDetailsScreen(
    state: PathDetailsUiState,
    onLockPath: (String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                Button(
                    onClick = { onLockPath(state.tripId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        stringResource(R.string.configure_trip),
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
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
                    Text(
                        text = stringResource(R.string.trip_details_title),
                        color = SakartveloRed,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = state.title.uppercase(),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(Modifier.height(16.dp))

                    if (state.stats.hasSnowWarning) {
                        Surface(
                            color = Color(0xFFFF9800).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AcUnit,
                                    null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        stringResource(R.string.snow_warning_title),
                                        color = Color(0xFFFF9800),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        stringResource(R.string.snow_warning_desc),
                                        color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TacticalChip(
                            stringResource(
                                R.string.trip_duration_days,
                                state.stats.durationDays
                            ), Icons.Default.CalendarMonth
                        )
                        TacticalChip(
                            stringResource(R.string.trip_drive_time, state.stats.driveTime),
                            Icons.Default.Schedule
                        )
                        TacticalChip(
                            stringResource(R.string.trip_intensity, state.stats.intensity.name),
                            Icons.Default.Landscape
                        )
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
            Box(
                Modifier
                    .size(12.dp)
                    .background(SakartveloRed, CircleShape)
            )
            if (!isLast) {
                Box(
                    Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.onBackground.copy(0.15f))
                )
            }
        }
        Spacer(Modifier.width(20.dp))
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .animateContentSize()
        ) {
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isExpanded) item.fullDescription else item.shortSummary,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
            )
            if (isExpanded) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl)
                        .crossfade(true).build(),
                    contentDescription = null,
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
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = SakartveloRed, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black
            )
        }
    }
}