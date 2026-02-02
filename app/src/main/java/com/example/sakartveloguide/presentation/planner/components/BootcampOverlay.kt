package com.example.sakartveloguide.presentation.planner.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.sakartveloguide.R
import com.example.sakartveloguide.presentation.planner.BootcampStep // FIXED: Added Import
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@Composable
fun BootcampOverlay(step: BootcampStep) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp)
            .background(Color.Black.copy(alpha = 0.5f)) // Dim background
            .zIndex(100f) // Ensure on top
            .clickable(enabled = false) { } // Eat clicks
    ) {
        when(step) {
            BootcampStep.ESSENTIALS -> {
                TacticalNote(
                    text = "STEP 1: LOGISTICS\nSecure flight & sleep first!",
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 180.dp),
                    arrowDirection = ArrowDir.UP
                )
            }
            BootcampStep.SET_HOME -> {
                TacticalNote(
                    text = "STEP 2: BASE CAMP\nTap here to set your HQ.",
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 260.dp),
                    arrowDirection = ArrowDir.UP
                )
            }
            BootcampStep.ADD_LOCATION -> {
                TacticalNote(
                    text = "STEP 3: ENRICHMENT\nTap + to add the 3rd target.",
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp),
                    arrowDirection = ArrowDir.DOWN
                )
            }
            // ... (Other steps)
            else -> {}
        }
    }
}

enum class ArrowDir { UP, DOWN, NONE }

@Composable
fun TacticalNote(
    text: String,
    modifier: Modifier = Modifier,
    arrowDirection: ArrowDir
) {
    Column(
        modifier = modifier.rotate(-2f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (arrowDirection == ArrowDir.UP) ScribbleArrow(true)

        Box(
            modifier = Modifier
                .background(Color(0xFFFEF9C3), RoundedCornerShape(2.dp))
                .padding(16.dp)
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        }

        if (arrowDirection == ArrowDir.DOWN) ScribbleArrow(false)
    }
}

@Composable
fun ScribbleArrow(pointingUp: Boolean) {
    Canvas(modifier = Modifier.height(40.dp).width(20.dp)) {
        val path = Path()
        if (pointingUp) {
            path.moveTo(size.width / 2, size.height)
            path.lineTo(size.width / 2, 0f)
            path.lineTo(0f, 10f)
            path.moveTo(size.width / 2, 0f)
            path.lineTo(size.width, 10f)
        } else {
            path.moveTo(size.width / 2, 0f)
            path.lineTo(size.width / 2, size.height)
            path.lineTo(0f, size.height - 10f)
            path.moveTo(size.width / 2, size.height)
            path.lineTo(size.width, size.height - 10f)
        }
        drawPath(
            path = path,
            color = Color(0xFFFFEB3B),
            style = Stroke(width = 5f)
        )
    }
}