package com.example.sakartveloguide.presentation.premium

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.presentation.theme.SnowWhite
import com.example.sakartveloguide.presentation.theme.WineDark

@Composable
fun PaywallScreen(
    onPurchase: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(containerColor = WineDark) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "SAKARTVELO PRO",
                style = MaterialTheme.typography.displaySmall,
                color = SnowWhite,
                fontWeight = FontWeight.Black
            )
            Text(
                "UNLOCK THE HIDDEN TRACKS",
                color = SnowWhite.copy(alpha = 0.7f),
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(48.dp))

            // Value Propositions
            PremiumFeatureItem(Icons.Default.Map, "Offline Topography", "Full offline maps for 0% signal zones.")
            PremiumFeatureItem(Icons.Default.Verified, "Hidden Gem Routes", "Access to Tusheti, Svaneti, and Vashlovani.")
            PremiumFeatureItem(Icons.Default.SupportAgent, "Expert Logistician", "Direct contact with 4x4 rescue drivers.")

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onPurchase,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SnowWhite)
            ) {
                Text("GET ACCESS — $4.99", color = WineDark, fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onBack) {
                Text("NOT NOW", color = SnowWhite.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun PremiumFeatureItem(icon: ImageVector, title: String, desc: String) {
    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        Icon(icon, contentDescription = null, tint = SnowWhite, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = SnowWhite, fontWeight = FontWeight.Bold)
            Text(desc, color = SnowWhite.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
        }
    }
}