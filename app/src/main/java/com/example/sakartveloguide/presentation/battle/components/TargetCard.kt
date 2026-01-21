package com.example.sakartveloguide.presentation.battle.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.domain.model.BattleNode
import com.example.sakartveloguide.domain.model.TargetStatus
import com.example.sakartveloguide.presentation.battle.TacticalAction
import com.example.sakartveloguide.presentation.theme.MatteCharcoal
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import com.example.sakartveloguide.presentation.theme.SnowWhite

@Composable
fun TargetCard(
    node: BattleNode,
    status: TargetStatus,
    action: TacticalAction, // <--- The single source of truth
    language: String,
    distanceKm: Double,
    onEngage: () -> Unit,
    onExecuteAction: (TacticalAction) -> Unit
) {
    val isEngaged = status == TargetStatus.ENGAGED
    val isNeutralized = status == TargetStatus.NEUTRALIZED

    // Dim the card if it's already done
    val cardAlpha = if (isNeutralized) 0.5f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(cardAlpha)
            .animateContentSize(),
        border = if (isEngaged) BorderStroke(2.dp, SakartveloRed) else BorderStroke(1.dp, Color.White.copy(0.1f)),
        colors = CardDefaults.cardColors(containerColor = MatteCharcoal.copy(0.9f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // 1. VISUAL INTEL (Image)
            // Only show image if we are focused on this target or it's done
            if (isEngaged || isNeutralized) {
                Box(modifier = Modifier.height(160.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = node.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient for text readability
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MatteCharcoal))))

                    if (isNeutralized) {
                        Badge(
                            containerColor = Color(0xFF4CAF50),
                            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                        ) {
                            Text("SECURED", modifier = Modifier.padding(4.dp), color = MatteCharcoal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. TACTICAL DATA
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = node.title.get(language).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = SnowWhite
                        )
                        if (!isNeutralized) {
                            Text(
                                text = "${String.format("%.1f", distanceKm)} KM TO TARGET",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isEngaged) SakartveloRed else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (isEngaged) {
                        Icon(Icons.Default.Radar, null, tint = SakartveloRed, modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Description: Collapsed unless engaged
                Text(
                    text = node.description.get(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SnowWhite.copy(0.7f),
                    maxLines = if (isEngaged) Int.MAX_VALUE else 2,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(16.dp))

                // 3. THE "MAGIC" BUTTON
                // This replaces the complex row of buttons.
                when {
                    isNeutralized -> { /* No button needed */ }

                    !isEngaged -> {
                        // State: Idle. User must tap to focus.
                        Button(
                            onClick = onEngage,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("SELECT TARGET", fontWeight = FontWeight.Bold, color = SakartveloRed)
                        }
                    }

                    action is TacticalAction.Execute -> {
                        // State: Engaged. Show the specific calculated action.
                        val btnColor = Color(action.colorHex)
                        val txtColor = if (action.colorHex == 0xFFFFFFFFL) MatteCharcoal else SnowWhite

                        Button(
                            onClick = { onExecuteAction(action) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Icon(action.icon, null, tint = txtColor)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = action.label,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = txtColor,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}