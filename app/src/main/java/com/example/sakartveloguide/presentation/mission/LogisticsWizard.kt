package com.example.sakartveloguide.presentation.mission

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AirplaneTicket
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticsWizard(
    trip: TripPath,
    currentProfile: LogisticsProfile,
    onDismiss: () -> Unit,
    onConfirm: (LogisticsProfile) -> Unit
) {
    val context = LocalContext.current
    var isByAir by remember { mutableStateOf(currentProfile.isByAir) }

    // ARCHITECT'S FIX: Default to NULL to force selection
    var selectedTransport by remember { mutableStateOf<TransportType?>(null) }

    var needFlight by remember { mutableStateOf(currentProfile.needsFlight) }
    var needHotel by remember { mutableStateOf(currentProfile.needsAccommodation) }
    var needEsim by remember { mutableStateOf(currentProfile.needsEsim) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentProfile.startDate)
    var showDatePicker by remember { mutableStateOf(false) }
    val dateLabel = datePickerState.selectedDateMillis?.let {
        SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(it))
    } ?: "Select Mission Start"

    Scaffold(
        containerColor = MatteCharcoal,
        bottomBar = {
            Surface(color = MatteCharcoal, tonalElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(24.dp).height(64.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            val start = datePickerState.selectedDateMillis
                            val end = start?.let { it + ((trip.durationDays - 1) * 24 * 60 * 60 * 1000L) }
                            onConfirm(LogisticsProfile(isByAir = isByAir, transportType = selectedTransport ?: TransportType.OWN_CAR, needsAccommodation = needHotel, needsEsim = needEsim, needsFlight = needFlight, startDate = start, endDate = end))
                        },
                        enabled = datePickerState.selectedDateMillis != null,
                        modifier = Modifier.weight(0.8f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed, disabledContainerColor = SnowWhite.copy(0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("CONFIRM PARAMETERS", fontWeight = FontWeight.Black)
                    }
                    Button(onClick = onDismiss, modifier = Modifier.weight(0.2f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = SnowWhite.copy(0.1f)), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = SnowWhite)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Text("MISSION PARAMETERS", color = SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("LOGISTICS SETUP", color = SnowWhite, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(20.dp))

            // ARRIVAL MODE
            Text("MODE OF ARRIVAL", color = SnowWhite.copy(0.6f), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeButton("BY AIR", Icons.Default.Flight, isByAir, Modifier.weight(1f)) { isByAir = true }
                ModeButton("BY LAND", Icons.Default.DirectionsBus, !isByAir, Modifier.weight(1f)) { isByAir = false }
            }

            Spacer(Modifier.height(24.dp))
            Text("MISSION START", color = SnowWhite.copy(0.6f), style = MaterialTheme.typography.labelSmall)
            Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = SnowWhite.copy(0.05f)), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.CalendarToday, null, tint = SakartveloRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text(dateLabel, color = SnowWhite, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            // FLIGHT INTEL (Switched OFF by default, Auto-Expands)
            AnimatedVisibility(visible = isByAir) {
                Column {
                    LogisticsToggle("Flight Intel", "Route discovery", Icons.Default.AirplaneTicket, needFlight) { needFlight = it }
                    AnimatedVisibility(visible = needFlight) {
                        ReferralLinkBox("Airfare Intelligence", "Find cheapest routes.", "BOOK", Icons.AutoMirrored.Filled.AirplaneTicket, Color(0xFF00D7E1)) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.skyscanner.net"))) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // CONNECTIVITY (Auto-Expands)
            LogisticsToggle("Connectivity", "eSIM & Data Protocol", Icons.Default.SignalCellularAlt, needEsim) { needEsim = it }
            AnimatedVisibility(visible = needEsim) {
                ReferralLinkBox("Secure Digital Signal", "Pre-order local 4G.", "GET ESIM", Icons.Default.PhonelinkSetup, SakartveloRed) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.magticom.ge/en/esim"))) }
            }

            Spacer(Modifier.height(8.dp))

            // LODGING (Auto-Expands)
            LogisticsToggle("Lodging", "Secure base camps", Icons.Default.Hotel, needHotel) { needHotel = it }
            AnimatedVisibility(visible = needHotel) {
                ReferralLinkBox("Secure Base Camp", "Vetted lodging deals.", "BOOK", Icons.Default.Bed, Color(0xFF003580)) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.booking.com"))) }
            }

            Spacer(Modifier.height(16.dp))

            // TRANSPORT PLAN (BY LAND - Auto-Expands Selection)
            AnimatedVisibility(visible = !isByAir) {
                Column {
                    Text("TRANSPORT PLAN", color = SnowWhite.copy(0.6f), style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                    TransportType.entries.forEach { type ->
                        Column {
                            TacticalTransportOption(type, selectedTransport == type) { selectedTransport = it }
                            AnimatedVisibility(visible = selectedTransport == type) {
                                when(type) {
                                    TransportType.RENTAL_4X4 -> ReferralLinkBox("Local Fleet", "4x4 inventory.", "RENT", Icons.Default.DirectionsCar, Color(0xFFFF9800)) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://localrent.com"))) }
                                    TransportType.TAXI -> ReferralLinkBox("Rapid Deployment", "Call a Bolt.", "BOLT", Icons.Default.LocalTaxi, Color(0xFF32BB78)) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bolt.eu"))) }
                                    TransportType.PUBLIC_TRANSPORT -> ReferralLinkBox("Transit Network", "Check TTC schedules.", "TTC", Icons.Default.DirectionsBus, Color(0xFF673AB7)) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://ttc.com.ge/en"))) }
                                    else -> {} // Own Car = No tool
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("CONFIRM", color = SakartveloRed) } }) {
            DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(containerColor = MatteCharcoal, titleContentColor = SnowWhite, headlineContentColor = SnowWhite, selectedDayContainerColor = SakartveloRed, todayContentColor = SakartveloRed, dayContentColor = SnowWhite))
        }
    }
}