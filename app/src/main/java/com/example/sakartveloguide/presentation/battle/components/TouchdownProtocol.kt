package com.example.sakartveloguide.presentation.battle.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // ARCHITECT'S FIX: Added missing import
import com.example.sakartveloguide.domain.model.EntryPoint
import com.example.sakartveloguide.domain.model.LogisticsProfile
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import com.example.sakartveloguide.presentation.theme.SnowWhite

@Composable
fun TouchdownCard(profile: LogisticsProfile) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SnowWhite.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FlightLand, null, tint = SakartveloRed)
                Spacer(Modifier.width(12.dp))
                Text("ARRIVAL INTEL", color = SakartveloRed, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            val instruction = when(profile.entryPoint) {
                EntryPoint.AIRPORT_TBS -> "TBS Protocol: Exit Arrivals. IGNORE taxi touts. Head to Departure Level (2nd Floor) for Bolt pickup to avoid overcharging."
                EntryPoint.AIRPORT_KUT -> "KUT Protocol: Exit main doors. Locate 'Georgian Bus' or 'Omnibus' counters. Price is fixed at 20-25 GEL to Tbilisi."
                else -> "City Protocol: Use official apps only. Standard city fare is 5-15 GEL."
            }

            Text(instruction, color = SnowWhite, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)

            Spacer(Modifier.height(12.dp))

            Surface(
                color = SakartveloRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "FAIR PRICE: 30-45 GEL",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = SakartveloRed,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}