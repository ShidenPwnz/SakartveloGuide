package com.example.sakartveloguide.presentation.mission

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
    onDismiss: () -> Unit,
    onConfirm: (LogisticsProfile) -> Unit
) {
    // 1. STATES
    var selectedEntry by remember { mutableStateOf(EntryPoint.AIRPORT_TBS) }
    var selectedTransport by remember { mutableStateOf(TransportType.RENTAL_4X4) }
    var needHotel by remember { mutableStateOf(true) }
    var needEsim by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Date Logic
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val dateLabel = datePickerState.selectedDateMillis?.let {
        SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(it))
    } ?: "Select Start Date"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MatteCharcoal,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = SnowWhite.copy(alpha = 0.2f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(scrollState)
        ) {
            Text("MISSION PARAMETERS", color = SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("LOGISTICS SETUP", color = SnowWhite, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)

            Spacer(Modifier.height(20.dp))

            // 1. ARCHITECT'S FIX: RESTORED ENTRY POINT SELECTION
            Text("INFILTRATION POINT", color = SnowWhite.copy(0.6f), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()), // Allow scrolling if names are long
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EntryPoint.entries.forEach { point ->
                    val isSelected = selectedEntry == point
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedEntry = point },
                        label = { Text(point.title, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SakartveloRed,
                            selectedLabelColor = SnowWhite,
                            containerColor = SnowWhite.copy(alpha = 0.05f),
                            labelColor = SnowWhite.copy(alpha = 0.7f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.Transparent,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. DATE SELECTION
            Text("MISSION START (Duration: ${trip.durationDays} Days)", color = SnowWhite.copy(0.6f), style = MaterialTheme.typography.labelSmall)
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SnowWhite.copy(0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = SakartveloRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text(dateLabel, color = SnowWhite, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            // 3. ASSETS
            LogisticsToggle("Magti eSIM", "Pre-order digital SIM", Icons.Default.SignalCellularAlt, needEsim) { needEsim = it }
            LogisticsToggle("Booking.com", "Secure base camps", Icons.Default.Hotel, needHotel) { needHotel = it }

            Spacer(Modifier.height(16.dp))

            // 4. MOBILITY
            Text("MOBILITY PROTOCOL", color = SnowWhite.copy(0.6f), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))
            TransportType.entries.forEach { type ->
                TacticalTransportOption(type, selectedTransport == type) { selectedTransport = type }
            }

            Spacer(Modifier.height(32.dp))

            // 5. CONFIRM
            Button(
                onClick = {
                    val start = datePickerState.selectedDateMillis
                    val end = start?.let {
                        it + ((trip.durationDays - 1) * 24 * 60 * 60 * 1000L)
                    }
                    onConfirm(LogisticsProfile(
                        entryPoint = selectedEntry,
                        transportType = selectedTransport,
                        needsAccommodation = needHotel,
                        needsEsim = needEsim,
                        startDate = start,
                        endDate = end
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("CONFIRM PROTOCOLS", fontWeight = FontWeight.Black)
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("CONFIRM", color = SakartveloRed, fontWeight = FontWeight.Bold) } },
            colors = DatePickerDefaults.colors(containerColor = MatteCharcoal)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MatteCharcoal,
                    titleContentColor = SnowWhite,
                    headlineContentColor = SnowWhite,
                    selectedDayContainerColor = SakartveloRed,
                    todayContentColor = SakartveloRed,
                    dayContentColor = SnowWhite
                )
            )
        }
    }
}

@Composable
private fun TacticalTransportOption(type: TransportType, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        color = if (isSelected) SakartveloRed.copy(0.1f) else SnowWhite.copy(0.03f),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(1.dp, SakartveloRed) else null
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(type.icon, null, tint = if (isSelected) SakartveloRed else SnowWhite.copy(0.4f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(type.title, color = SnowWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(type.subtitle, color = SnowWhite.copy(0.5f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun LogisticsToggle(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SnowWhite.copy(0.05f)).clickable { onCheckedChange(!checked) }.padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(icon, null, tint = if (checked) SakartveloRed else SnowWhite.copy(0.3f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = SnowWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = SnowWhite.copy(0.5f), style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = SakartveloRed, checkedThumbColor = SnowWhite), modifier = Modifier.scale(0.75f))
    }
}