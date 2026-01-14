package com.example.sakartveloguide.presentation.battle.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.domain.model.GeoPoint
import com.example.sakartveloguide.domain.model.TransportType
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import com.example.sakartveloguide.presentation.theme.SnowWhite

@Composable
fun TacticalBridge(
    start: GeoPoint,
    end: GeoPoint,
    distanceMeters: Float,
    userTransport: TransportType,
    onNavigate: (Intent) -> Unit,
    onCallBolt: (Intent) -> Unit
) {
    val context = LocalContext.current
    val distanceKm = distanceMeters / 1000

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SnowWhite.copy(0.03f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("NEXT OBJECTIVE", color = SakartveloRed, style = MaterialTheme.typography.labelSmall)
                Text("${String.format("%.1f", distanceKm)} KM AWAY", color = SnowWhite, fontWeight = FontWeight.Black)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // SMART ACTION 1: Google Maps
                val mode = when {
                    userTransport == TransportType.RENTAL_4X4 -> "driving"
                    distanceKm < 2.0 -> "walking"
                    else -> "transit"
                }

                Button(
                    onClick = { 
                        // Logic to generate intent... simplified for the example
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SnowWhite.copy(0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        if (mode == "walking") Icons.Default.DirectionsWalk 
                        else if (mode == "transit") Icons.Default.DirectionsBus 
                        else Icons.Default.DirectionsCar, 
                        null, 
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(mode.uppercase(), fontSize = 10.sp)
                }

                // SMART ACTION 2: Bolt (Only if not using own car)
                if (userTransport != TransportType.RENTAL_4X4 && distanceKm > 1.5) {
                    Button(
                        onClick = { /* call bolt intent */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32BB78)), // Bolt Green
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.LocalTaxi, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("BOLT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}