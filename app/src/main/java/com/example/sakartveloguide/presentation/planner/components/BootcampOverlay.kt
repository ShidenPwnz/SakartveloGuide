package com.example.sakartveloguide.presentation.planner.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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

@Composable
fun BootcampSpotlight(
    step: BootcampStep,
    targetCoords: LayoutCoordinates?,
    onNext: () -> Unit,
    onDismiss: () -> Unit
) {
    if (step == BootcampStep.NONE || step == BootcampStep.COMPLETE || targetCoords == null) return

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // ARCHITECT'S FIX: Use screen height in pixels for safe math
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val rect = targetCoords.let {
        val pos = it.positionInRoot()
        val size = it.size
        Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
    }

    Box(modifier = Modifier.fillMaxSize().zIndex(1000f)) {
        // 1. THE TACTICAL MASK
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
                .pointerInput(rect) {
                    detectTapGestures { offset ->
                        // Hybrid logic: Advance tutorial if tap is outside the highlighted area
                        if (!rect.contains(offset)) {
                            onNext()
                        }
                    }
                }
        ) {
            drawRect(color = Color.Black.copy(alpha = 0.85f))

            // Punch the hole
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(rect.left - 8, rect.top - 8),
                size = Size(rect.width + 16, rect.height + 16),
                cornerRadius = CornerRadius(24f, 24f),
                blendMode = BlendMode.Clear
            )
        }

        // 2. TEXT POSITIONING
        // Logic: If the hole is in the top half of the screen, show instructions in the bottom half.
        val isTargetInTopHalf = rect.center.y < (screenHeightPx / 2)

        Column(
            modifier = Modifier
                .align(if (isTargetInTopHalf) Alignment.BottomCenter else Alignment.TopCenter)
                .padding(horizontal = 32.dp)
                .padding(
                    top = if (!isTargetInTopHalf) 120.dp else 0.dp,
                    bottom = if (isTargetInTopHalf) 120.dp else 0.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = getBootcampTitle(step),
                color = SakartveloRed,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = getBootcampDesc(step),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "TAP HIGHLIGHT TO ACT • TAP SCREEN FOR NEXT",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 3. EXIT PROTOCOL
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding()
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.7f))
        }
    }
}

private fun getBootcampTitle(step: BootcampStep) = when(step) {
    BootcampStep.ESSENTIALS -> "01 // INTEL & LOGISTICS"
    BootcampStep.SET_HOME -> "02 // BASE OF OPERATIONS"
    BootcampStep.ADD_LOCATION -> "03 // TARGET ACQUISITION"
    BootcampStep.OPTIMIZE -> "04 // TACTICAL ROUTING"
    BootcampStep.START -> "05 // MISSION GO"
    else -> ""
}

private fun getBootcampDesc(step: BootcampStep) = when(step) {
    BootcampStep.ESSENTIALS -> "Check visa requirements, book 4x4s, and secure flights here."
    BootcampStep.SET_HOME -> "Set your HQ location. This unlocks smart recommendations nearby."
    BootcampStep.ADD_LOCATION -> "Tap (+) to add verified locations from the database."
    BootcampStep.OPTIMIZE -> "The engine auto-sorts stops to minimize driving time."
    BootcampStep.START -> "Lock parameters and switch to Live Navigation Mode."
    else -> ""
}