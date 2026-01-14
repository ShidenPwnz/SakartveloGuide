package com.example.sakartveloguide.presentation.mission

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.domain.model.LogisticsProfile
import com.example.sakartveloguide.domain.model.TripPath
import com.example.sakartveloguide.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MissionControlScreen(
    trip: TripPath,
    profile: LogisticsProfile?,
    onStartTrip: () -> Unit,
    onReconfigure: () -> Unit
) {
    val context = LocalContext.current
    var isEsimSecured by remember { mutableStateOf(profile?.needsEsim != true) }
    var isTransportSecured by remember { mutableStateOf(false) }
    var isHotelSecured by remember { mutableStateOf(profile?.needsAccommodation != true) }

    val canInitiate = isEsimSecured && isTransportSecured && isHotelSecured
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())

    val dateRangeText = if (profile?.startDate != null && profile.endDate != null) {
        "${sdf.format(profile.startDate)} — ${sdf.format(profile.endDate)} (${trip.durationDays} Days)"
    } else "DATES NOT ASSIGNED"

    Scaffold(
        containerColor = MatteCharcoal,
        bottomBar = {
            Surface(color = MatteCharcoal, tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp).height(64.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onStartTrip,
                        enabled = canInitiate,
                        modifier = Modifier.weight(0.8f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed, disabledContainerColor = SnowWhite.copy(0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("START MISSION", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }

                    Button(
                        onClick = onReconfigure,
                        modifier = Modifier.weight(0.2f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = SnowWhite.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Tune, "Reconfigure", tint = SnowWhite)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Text("MISSION PROTOCOL", color = SakartveloRed, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
            Text(trip.title.uppercase(), color = SnowWhite, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                Icon(Icons.Default.DateRange, null, tint = SakartveloRed, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(dateRangeText, color = SakartveloRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))
            Text("REQUIRED ASSETS", color = SnowWhite.copy(0.5f), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (profile?.needsEsim == true) {
                    AssetCard("Magti eSIM", if (isEsimSecured) "SECURED" else "REQUIRED", Icons.Default.Language, isEsimSecured, { isEsimSecured = it }, "WEB") {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.magticom.ge/en/esim")))
                    }
                }
                AssetCard("Transport: ${profile?.transportType?.title}", if (isTransportSecured) "READY" else "STANDBY", Icons.Default.LocalTaxi, isTransportSecured, { isTransportSecured = it }, "BOLT") {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bolt.eu")))
                }
                if (profile?.needsAccommodation == true) {
                    AssetCard("Base Camp", if (isHotelSecured) "BOOKED" else "PENDING", Icons.Default.NightShelter, isHotelSecured, { isHotelSecured = it }, "DEALS") {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.booking.com")))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("ITINERARY SUMMARY", color = SnowWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            trip.itinerary.forEachIndexed { index, node ->
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${index + 1}", color = SakartveloRed, fontWeight = FontWeight.Black, modifier = Modifier.width(24.dp))
                    Column {
                        Text(node.title, color = SnowWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(node.timeLabel, color = SnowWhite.copy(0.5f), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun AssetCard(title: String, status: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit, actionLabel: String, onClick: () -> Unit) {
    Surface(color = SnowWhite.copy(0.05f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isChecked, onCheckedChange = onCheckedChange, colors = CheckboxDefaults.colors(checkedColor = SakartveloRed, uncheckedColor = SnowWhite.copy(0.2f)))
            Spacer(Modifier.width(8.dp))
            Icon(icon, null, tint = if(isChecked) SakartveloRed else SnowWhite.copy(0.4f), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = SnowWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(status, color = if(isChecked) Color.Green else SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            if (!isChecked) {
                Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed.copy(0.2f)), contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.height(32.dp)) {
                    Text(actionLabel, color = SakartveloRed, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}