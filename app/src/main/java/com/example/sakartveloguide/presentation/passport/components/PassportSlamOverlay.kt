package com.example.sakartveloguide.presentation.passport.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.sakartveloguide.domain.model.TripPath
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PassportSlamOverlay(
    trip: TripPath,
    onAnimationFinished: () -> Unit
) {
    // ARCHITECT'S NOTE: Starting at 5x scale creates the "falling from sky" effect
    val scale = remember { Animatable(5f) } 
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(-10f) } // Tactical tilt

    LaunchedEffect(Unit) {
        launch {
            // The "Thud": Low stiffness creates the heavy impact
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow 
                )
            )
        }
        launch {
            alpha.animateTo(1f, tween(100))
        }
        launch {
            rotation.animateTo(0f, spring(stiffness = Spring.StiffnessLow))
        }

        // Hold for the "Collector's Gaze"
        delay(2500)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .zIndex(100f), // Force top layer
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    alpha = alpha.value,
                    rotationZ = rotation.value
                )
                .drawBehind {
                    // Visual: Tactical dashed circle (Ink-stamp aesthetic)
                    drawCircle(
                        color = SakartveloRed,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), 0f)
                        )
                    )
                }
                .padding(60.dp)
        ) {
            Text(
                text = trip.category.name,
                color = SakartveloRed,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "APPROVED PROTOCOL",
                color = SakartveloRed.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "SAKARTVELO",
                color = SakartveloRed,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
