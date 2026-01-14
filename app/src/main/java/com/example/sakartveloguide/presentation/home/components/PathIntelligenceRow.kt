package com.example.sakartveloguide.presentation.home.components

import androidx.compose.foundation.layout.* // FIX: Missing fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow // FIX: Missing TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sakartveloguide.domain.model.TripPath
import com.example.sakartveloguide.presentation.theme.SakartveloRed // FIX: Missing color

@Composable
fun PathIntelligenceRow(
    path: TripPath,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. TIME: e.g., Icon + "6h"
        InfoBadge(Icons.Default.Timer, "${path.totalRideTimeMinutes / 60}h")

        // 2. DAYS: e.g., Icon + "2 Days"
        InfoBadge(Icons.Default.CalendarToday, "${path.durationDays} Days")

        // 3. DIFFICULTY: e.g., Icon + "EXPLORER"
        InfoBadge(Icons.Default.Terrain, path.difficulty.name)
    }
}

@Composable
private fun InfoBadge(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = SakartveloRed, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black, // Heavier weight for readability
            color = SakartveloRed,
            maxLines = 1
        )
    }
}