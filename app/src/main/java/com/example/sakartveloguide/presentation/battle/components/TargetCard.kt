package com.example.sakartveloguide.presentation.battle.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.domain.model.BattleNode
import com.example.sakartveloguide.domain.model.TargetStatus // IMPORTED FROM DOMAIN
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@Composable
fun TargetCard(
    node: BattleNode,
    status: TargetStatus,
    language: String,
    distanceKm: Double,
    onEngage: () -> Unit,
    onSecure: () -> Unit,
    onNavigate: (String) -> Unit,
    onBoltClick: () -> Unit,
    onRentalClick: () -> Unit
) {
    val alpha = if (status == TargetStatus.NEUTRALIZED) 0.4f else 1f
    val isEngaged = status == TargetStatus.ENGAGED

    val borderColor = when(status) {
        TargetStatus.ENGAGED -> SakartveloRed
        TargetStatus.NEUTRALIZED -> Color(0xFF4CAF50)
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(alpha)
            .animateContentSize(),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            AnimatedVisibility(visible = isEngaged) {
                AsyncImage(
                    model = node.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(node.title.get(language).uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text("${"%.1f".format(distanceKm)} KM FROM CURRENT", color = SakartveloRed, style = MaterialTheme.typography.labelSmall)
                    }
                    if (status == TargetStatus.NEUTRALIZED) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(node.description.get(language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))

                if (status == TargetStatus.AVAILABLE) {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onEngage,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("INITIATE SORTIE", color = SakartveloRed, fontWeight = FontWeight.Bold)
                    }
                }

                if (isEngaged) {
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onNavigate("driving") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("DRIVE", fontSize = 10.sp)
                        }
                        Button(
                            onClick = onSecure,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("SECURE", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onBoltClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32BB78)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.LocalTaxi, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("BOLT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onRentalClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Key, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("RENT 4X4", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}