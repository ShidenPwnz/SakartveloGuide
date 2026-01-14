package com.example.sakartveloguide.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // FIX: Added Import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest // FIX: Added Import
import com.example.sakartveloguide.domain.model.TripPath
import com.example.sakartveloguide.presentation.theme.*

@Composable
fun PathCard(
    trip: TripPath,
    showButton: Boolean = true,
    onCardClick: (String) -> Unit,
    onLockClick: () -> Unit,
    onPaywallClick: () -> Unit
) {
    Card(
        onClick = { onCardClick(trip.id) },
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. IMAGE: 40% height with Safe Fallbacks
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(trip.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop,
                // ARCHITECT'S FIX: Use built-in Vectors instead of missing Drawables
                placeholder = rememberVectorPainter(Icons.Default.Image),
                error = rememberVectorPainter(Icons.Default.BrokenImage)
            )

            // 2. CONTENT: 60% height
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(24.dp)
            ) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.headlineMedium.copy(lineHeight = 32.sp),
                    fontWeight = FontWeight.Black,
                    maxLines = 2
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = trip.description,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 3
                )

                Spacer(Modifier.weight(1f))

                PathIntelligenceRow(path = trip)

                if (showButton) {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (trip.isPremium) onPaywallClick() else onLockClick()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (trip.isPremium) WineDark else SakartveloRed
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("TRIP LOGISTICS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}