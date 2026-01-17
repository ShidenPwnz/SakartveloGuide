package com.example.sakartveloguide.presentation.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sakartveloguide.domain.model.TripPath
import com.example.sakartveloguide.presentation.theme.*

@Composable
fun PathCard(
    trip: TripPath,
    languageCode: String,
    onCardClick: (String) -> Unit,
    onHideTutorial: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Breathe")

    val imageScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Zoom"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Pulse"
    )

    Card(
        onClick = { if (trip.id == "meta_tutorial") onHideTutorial() else onCardClick(trip.id) },
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                brush = Brush.radialGradient(
                    colors = listOf(SakartveloRed.copy(alpha = pulseAlpha), Color.Transparent)
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(0.5f).fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(trip.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                        },
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(0.5f).padding(24.dp)) {
                Text(
                    text = trip.title.get(languageCode).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = trip.description.get(languageCode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 4
                )

                Spacer(Modifier.weight(1f))

                if (trip.id != "meta_tutorial") {
                    PathIntelligenceRow(path = trip)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "TAP TO INITIALIZE",
                        color = SakartveloRed,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer { alpha = pulseAlpha }
                    )
                } else {
                    Text("TAP TO DISMISS TUTORIAL", color = SakartveloRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}