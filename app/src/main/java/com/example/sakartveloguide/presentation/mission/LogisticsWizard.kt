package com.example.sakartveloguide.presentation.mission

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sakartveloguide.R
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.theme.SakartveloRed
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

    // State Holders
    var isByAir by remember { mutableStateOf(currentProfile.isByAir) }
    var selectedTransport by remember { mutableStateOf<TransportType?>(currentProfile.transportType) }

    // Toggle States
    var needFlight by remember { mutableStateOf(currentProfile.needsFlight) }
    var needHotel by remember { mutableStateOf(currentProfile.needsAccommodation) }
    var needEsim by remember { mutableStateOf(currentProfile.needsEsim) }
    var needTransport by remember { mutableStateOf(currentProfile.needsTransport) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentProfile.startDate)
    var showDatePicker by remember { mutableStateOf(false) }

    val dateLabel = datePickerState.selectedDateMillis?.let {
        SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(it))
    } ?: stringResource(R.string.select_date)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.logistics_params),
                            color = SakartveloRed,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.logistics_setup),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 8.dp) {
                Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    Button(
                        onClick = {
                            val start = datePickerState.selectedDateMillis
                            val end = start?.let { it + ((trip.durationDays - 1) * 24 * 60 * 60 * 1000L) }
                            onConfirm(LogisticsProfile(isByAir, transportType = selectedTransport ?: TransportType.OWN_CAR, needsAccommodation = needHotel, needsEsim = needEsim, needsFlight = needFlight, needsTransport = needTransport, startDate = start, endDate = end))
                        },
                        enabled = datePickerState.selectedDateMillis != null,
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.confirm_params), fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            // 1. ARRIVAL MODE
            Text(stringResource(R.string.arrival_mode), color = MaterialTheme.colorScheme.onBackground.copy(0.6f), style = MaterialTheme.typography.labelSmall)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeButton(stringResource(R.string.by_air), Icons.Default.Flight, isByAir, Modifier.weight(1f)) { isByAir = true }
                ModeButton(stringResource(R.string.by_land), Icons.Default.DirectionsBus, !isByAir, Modifier.weight(1f)) { isByAir = false }
            }

            // 2. DATE SELECTION
            Text(stringResource(R.string.mission_start_label), color = MaterialTheme.colorScheme.onBackground.copy(0.6f), style = MaterialTheme.typography.labelSmall)
            Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.CalendarToday, null, tint = SakartveloRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text(dateLabel, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            // 3. FLIGHT INTEL (Restored Logic: Only shows if By Air is selected)
            AnimatedVisibility(visible = isByAir) {
                Column {
                    LogisticsToggle(stringResource(R.string.log_flight), stringResource(R.string.log_flight_sub), Icons.Default.AirplaneTicket, needFlight) { needFlight = it }
                    AnimatedVisibility(visible = needFlight) {
                        ReferralLinkBox("Skyscanner Protocol", "Global airbridge discovery.", "SEARCH", Icons.AutoMirrored.Filled.AirplaneTicket, Color(0xFF00D7E1)) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.skyscanner.net")))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // 4. CONNECTIVITY (Restored Logic)
            LogisticsToggle(stringResource(R.string.log_comms), stringResource(R.string.log_comms_sub), Icons.Default.SignalCellularAlt, needEsim) { needEsim = it }
            AnimatedVisibility(visible = needEsim) {
                ReferralLinkBox("Magti 4G eSIM", "Digital connectivity setup.", "GET ESIM", Icons.Default.PhonelinkSetup, SakartveloRed) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.magticom.ge/en/esim")))
                }
            }
            Spacer(Modifier.height(8.dp))

            // 5. LODGING (Restored Logic)
            LogisticsToggle(stringResource(R.string.log_lodging), stringResource(R.string.log_lodging_sub), Icons.Default.Hotel, needHotel) { needHotel = it }
            AnimatedVisibility(visible = needHotel) {
                ReferralLinkBox("Booking.com", "Secure local base camps.", "RESERVE", Icons.Default.Bed, Color(0xFF003580)) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.booking.com")))
                }
            }
            Spacer(Modifier.height(8.dp))

            // 6. TRANSPORT (Restored Logic)
            LogisticsToggle(stringResource(R.string.log_transport), stringResource(R.string.log_transport_sub), Icons.Default.DirectionsCar, needTransport) { needTransport = it }

            AnimatedVisibility(visible = needTransport) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    TransportType.entries.forEach { type ->
                        Column {
                            TacticalTransportOption(
                                label = stringResource(type.titleRes),
                                sub = stringResource(type.subRes),
                                type = type,
                                isSelected = selectedTransport == type
                            ) { selectedTransport = it }

                            // Specific Logic per Transport Type
                            AnimatedVisibility(visible = selectedTransport == type) {
                                when(type) {
                                    TransportType.RENTAL_4X4 -> ReferralLinkBox("LocalRent", "4x4 inventory check.", "RENT", Icons.Default.DirectionsCar, Color(0xFFFF9800)) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://localrent.com")))
                                    }
                                    TransportType.TAXI -> ReferralLinkBox("Bolt Protocol", "Rapid urban deployment.", "BOLT", Icons.Default.LocalTaxi, Color(0xFF32BB78)) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bolt.eu")))
                                    }
                                    TransportType.PUBLIC_TRANSPORT -> ReferralLinkBox("TTC Schedules", "Bus/Metro routing.", "CHECK", Icons.Default.DirectionsBus, Color(0xFF5D3FD3)) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://ttc.com.ge")))
                                    }
                                    else -> {}
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
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } }) {
            DatePicker(state = datePickerState)
        }
    }
}