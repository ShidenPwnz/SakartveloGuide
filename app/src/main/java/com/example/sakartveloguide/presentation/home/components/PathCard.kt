package com.example.sakartveloguide.presentation.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@Composable
fun PathCard(
    trip: TripPath,
    languageCode: String,
    onCardClick: (String) -> Unit,
    onHideTutorial: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Breathe")
    val imageScale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.10f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Zoom"
    )

    Card(
        // ARCHITECT'S FIX: meta_tutorial now triggers navigation (onCardClick)
        // meta_tutorial_dismiss is a hypothetical ID if we wanted to hide it later
        onClick = { onCardClick(trip.id) },
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(trip.imageUrl)
                    .crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    scaleX = imageScale
                    scaleY = imageScale
                },
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(
                    text = trip.title.get(languageCode).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = trip.description.get(languageCode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 3,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "TAP TO INITIALIZE",
                    color = SakartveloRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}