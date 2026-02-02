package com.example.sakartveloguide.presentation.planner.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.sakartveloguide.R
import com.example.sakartveloguide.presentation.theme.SakartveloRed

enum class TutorialStep {
    ESSENTIALS,
    HOME_NODE,
    ADD_STOP,
    FINISHED
}

@Composable
fun TacticalTutorialOverlay(
    currentStep: TutorialStep,
    onNext: () -> Unit,
    onDismiss: () -> Unit
) {
    if (currentStep == TutorialStep.FINISHED) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f)) // Slightly darker for focus
            .zIndex(1000f)
            .clickable(enabled = false) { }
    ) {
        // CLOSE BUTTON
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding()
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }

        // TACTICAL INSTRUCTION CARD
        // We move the card based on where the arrow is pointing
        val cardAlignment = when(currentStep) {
            TutorialStep.ESSENTIALS -> Alignment.BottomCenter
            TutorialStep.HOME_NODE -> Alignment.BottomCenter
            TutorialStep.ADD_STOP -> Alignment.TopCenter
            else -> Alignment.Center
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .systemBarsPadding(),
            contentAlignment = cardAlignment
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = if (cardAlignment == Alignment.BottomCenter) 100.dp else 0.dp)
                    .padding(top = if (cardAlignment == Alignment.TopCenter) 100.dp else 0.dp)
            ) {
                Text(
                    text = stringResource(getTitleResForStep(currentStep)),
                    color = SakartveloRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 4.sp
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(getDescResForStep(currentStep)),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(Modifier.height(40.dp))

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp).fillMaxWidth(0.7f)
                ) {
                    Text(stringResource(R.string.tut_next), fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                }

                TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 12.dp)) {
                    Text(stringResource(R.string.tut_skip), color = Color.White.copy(0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // DYNAMIC INDICATOR
        IndicatorArrow(currentStep)
    }
}

@Composable
private fun IndicatorArrow(step: TutorialStep) {
    // Stage 1: Point at the top Essentials Row
    // Stage 2: Point at the Home Card (Middle-Top)
    // Stage 3: Point at the [+] button (Middle-Bottom)

    val alignment = when(step) {
        TutorialStep.ESSENTIALS -> Alignment.TopCenter
        TutorialStep.HOME_NODE -> Alignment.TopCenter
        TutorialStep.ADD_STOP -> Alignment.BottomCenter
        else -> Alignment.Center
    }

    val verticalPadding = when(step) {
        TutorialStep.ESSENTIALS -> 80.dp // Point at top row
        TutorialStep.HOME_NODE -> 260.dp // Point down at home card
        TutorialStep.ADD_STOP -> 200.dp  // Point down at [+] button
        else -> 0.dp
    }

    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        Icon(
            Icons.Default.KeyboardDoubleArrowUp,
            null,
            tint = SakartveloRed,
            modifier = Modifier
                .align(alignment)
                .padding(top = if(alignment == Alignment.TopCenter) verticalPadding else 0.dp)
                .padding(bottom = if(alignment == Alignment.BottomCenter) verticalPadding else 0.dp)
                .size(56.dp)
                .graphicsLayer {
                    // Logic: Step 1 (Essentials) points UP.
                    // Step 2 (Home) and Step 3 (Add) point DOWN.
                    rotationZ = if (step == TutorialStep.ESSENTIALS) 0f else 180f
                }
        )
    }
}

private fun getTitleResForStep(step: TutorialStep): Int = when(step) {
    TutorialStep.ESSENTIALS -> R.string.tut_step1_title
    TutorialStep.HOME_NODE -> R.string.tut_step2_title
    TutorialStep.ADD_STOP -> R.string.tut_step3_title
    else -> R.string.app_name
}

private fun getDescResForStep(step: TutorialStep): Int = when(step) {
    TutorialStep.ESSENTIALS -> R.string.tut_step1_desc
    TutorialStep.HOME_NODE -> R.string.tut_step2_desc
    TutorialStep.ADD_STOP -> R.string.tut_step3_desc
    else -> R.string.app_name
}