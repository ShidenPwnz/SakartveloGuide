package com.example.sakartveloguide.presentation.passport.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.sakartveloguide.R
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch // FIXED: Added missing import

@Composable
fun PassportSlamOverlay(
    regionName: String,
    onAnimationFinished: () -> Unit
) {
    val scale = remember { Animatable(3f) }
    val animAlpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(-15f) }

    LaunchedEffect(Unit) {
        // Parallel Animation Launch
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.4f,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            animAlpha.animateTo(1f, tween(300))
        }

        // Hold time for visual impact
        delay(2500)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .zIndex(100f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(scale.value)
                .rotate(rotation.value)
                .alpha(animAlpha.value)
                .border(8.dp, SakartveloRed, RoundedCornerShape(24.dp))
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.approved_protocol),
                    color = SakartveloRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = regionName.uppercase(),
                    color = SakartveloRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    style = MaterialTheme.typography.displaySmall,
                    lineHeight = 40.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "SAKARTVELO",
                    color = SakartveloRed.copy(0.6f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}