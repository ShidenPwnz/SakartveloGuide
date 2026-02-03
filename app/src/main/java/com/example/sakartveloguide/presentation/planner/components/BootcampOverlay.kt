package com.example.sakartveloguide.presentation.planner.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.sakartveloguide.presentation.planner.BootcampStep
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BootcampSpotlight(
    step: BootcampStep,
    targetCoords: LayoutCoordinates?,
    isInteractive: Boolean,
    onNext: () -> Unit,
    onDismiss: () -> Unit
) {
    if (step == BootcampStep.NONE || step == BootcampStep.COMPLETE || targetCoords == null || !targetCoords.isAttached) return

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val maxWidthPx = configuration.screenWidthDp.toFloat() * density.density
    val maxHeightPx = configuration.screenHeightDp.toFloat() * density.density

    val pos = targetCoords.positionInRoot()
    val size = targetCoords.size
    val targetRect = Rect(
        left = pos.x - 12f,
        top = pos.y - 12f,
        right = pos.x + size.width + 12f,
        bottom = pos.y + size.height + 12f
    )

    if (targetRect.width <= 0f) return

    val animRect = remember { Animatable(targetRect, Rect.VectorConverter) }
    val animAlpha = remember { Animatable(0f) }

    LaunchedEffect(targetRect) {
        launch { animAlpha.animateTo(0f, tween(100)) }
        animRect.animateTo(targetValue = targetRect, animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
        delay(100)
        animAlpha.animateTo(1f, tween(300))
    }

    val currentRect = animRect.value
    val scrimColor = Color.Black.copy(alpha = 0.88f)
    val noRipple = remember { MutableInteractionSource() }

    // Always allow skip by tapping screen EXCEPT on the Final Start Journey button
    val canTapToNext = step != BootcampStep.START

    BoxWithConstraints(modifier = Modifier.fillMaxSize().zIndex(1000f)) {
        Box(Modifier.fillMaxWidth().height(with(density) { currentRect.top.coerceAtLeast(0f).toDp() }).background(scrimColor).align(Alignment.TopCenter).clickable(noRipple, null) { if(canTapToNext) onNext() })
        Box(Modifier.fillMaxWidth().height(with(density) { (maxHeightPx - currentRect.bottom).coerceAtLeast(0f).toDp() }).background(scrimColor).align(Alignment.BottomCenter).clickable(noRipple, null) { if(canTapToNext) onNext() })
        Box(Modifier.width(with(density) { currentRect.left.coerceAtLeast(0f).toDp() }).height(with(density) { currentRect.height.toDp() }).offset(y = with(density) { currentRect.top.toDp() }).background(scrimColor).align(Alignment.TopStart).clickable(noRipple, null) { if(canTapToNext) onNext() })
        Box(Modifier.width(with(density) { (maxWidthPx - currentRect.right).coerceAtLeast(0f).toDp() }).height(with(density) { currentRect.height.toDp() }).offset(y = with(density) { currentRect.top.toDp() }).background(scrimColor).align(Alignment.TopEnd).clickable(noRipple, null) { if(canTapToNext) onNext() })

        val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
        val pulseAlpha by infiniteTransition.animateFloat(0.8f, 0.2f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), "a")
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(color = SakartveloRed.copy(alpha = pulseAlpha), topLeft = currentRect.topLeft, size = currentRect.size, cornerRadius = CornerRadius(24f, 24f), style = Stroke(width = 3.dp.toPx()))
        }

        val isTargetInTopHalf = currentRect.center.y < (maxHeightPx / 2)
        Column(
            modifier = Modifier
                .align(if (isTargetInTopHalf) Alignment.BottomCenter else Alignment.TopCenter)
                .padding(horizontal = 32.dp, vertical = 100.dp)
                .graphicsLayer { alpha = animAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(getJourneyTitle(step), color = SakartveloRed, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Surface(color = Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, SakartveloRed.copy(0.4f))) {
                Text(getJourneyDesc(step), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
            }
            if (canTapToNext) {
                Spacer(Modifier.height(16.dp))
                Text("TAP SCREEN TO CONTINUE", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding()) {
            Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.7f))
        }
    }
}

private fun getJourneyTitle(step: BootcampStep) = when(step) {
    BootcampStep.ESSENTIALS -> "YOUR ESSENTIALS"
    BootcampStep.SET_HOME -> "YOUR HOME BASE"
    BootcampStep.ADD_LOCATION -> "DISCOVER PLACES"
    BootcampStep.START -> "START THE JOURNEY"
    else -> ""
}

private fun getJourneyDesc(step: BootcampStep) = when(step) {
    BootcampStep.ESSENTIALS -> "Quickly access flights, stays, and transport here."
    BootcampStep.SET_HOME -> "Set your hotel or apartment location to get smart recommendations nearby."
    BootcampStep.ADD_LOCATION -> "We will simulate adding Metekhi Palace now."
    BootcampStep.START -> "When you're ready to go, tap Start Journey."
    else -> ""
}