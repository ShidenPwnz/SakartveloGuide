package com.example.sakartveloguide.presentation.mission

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.R
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.theme.MatteCharcoal
import com.example.sakartveloguide.presentation.theme.SakartveloRed

private enum class WizardStep {
    INTRO, TICKETS_CHECK, ACQUIRE_TICKETS, DATE_ENTRY, COMMS_CHECK, MOBILITY_STRATEGY, BASE_SECURE, CONFIRMATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticsWizard(
    trip: TripPath,
    currentProfile: LogisticsProfile,
    onDismiss: () -> Unit,
    onConfirm: (LogisticsProfile) -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(WizardStep.INTRO) }

    var startDate by remember { mutableStateOf<Long?>(null) }
    var needsEsim by remember { mutableStateOf(false) }
    var transportStrategy by remember { mutableStateOf(TransportStrategy.PASSENGER_URBAN) }
    var needsHotel by remember { mutableStateOf(false) }

    // ARCHITECT'S REFINEMENT: Named arguments ensure data integrity
    fun finalizeProfile() {
        val endDate = startDate?.let { it + ((trip.durationDays - 1) * 24 * 60 * 60 * 1000L) }
        val legacyType = when(transportStrategy) {
            TransportStrategy.PASSENGER_URBAN -> TransportType.TAXI
            TransportStrategy.PASSENGER_BUDGET -> TransportType.PUBLIC_TRANSPORT
            TransportStrategy.DRIVER_RENTAL -> TransportType.RENTAL_4X4
            TransportStrategy.DRIVER_OWNER -> TransportType.OWN_CAR
        }

        onConfirm(
            LogisticsProfile(
                transportStrategy = transportStrategy,
                vehicleStatus = VehicleStatus.NONE,
                entryPoint = EntryPoint.AIRPORT_TBS,
                exitPoint = EntryPoint.AIRPORT_TBS,
                startDate = startDate,
                endDate = endDate,
                isByAir = true,
                needsFlight = false,
                needsTransport = false,
                needsAccommodation = needsHotel,
                needsEsim = needsEsim,
                transportType = legacyType
            )
        )
    }

    BackHandler {
        when(currentStep) {
            WizardStep.INTRO -> onDismiss()
            WizardStep.TICKETS_CHECK -> currentStep = WizardStep.INTRO
            WizardStep.ACQUIRE_TICKETS -> currentStep = WizardStep.TICKETS_CHECK
            else -> { /* Navigation back logic if required */ }
        }
    }

    Scaffold(
        containerColor = MatteCharcoal,
        topBar = {
            CenterAlignedTopAppBar(
                title = { StepsProgressBar(currentStep.ordinal, WizardStep.entries.size) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep == WizardStep.INTRO) onDismiss() else {
                            // Back step logic
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { finalizeProfile() }) {
                        Text(stringResource(R.string.wiz_rogue_mode), color = SakartveloRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            AnimatedContent(targetState = currentStep, label = "WizTrans") { step ->
                when(step) {
                    WizardStep.INTRO -> IntroStep(onStart = { currentStep = WizardStep.TICKETS_CHECK })
                    WizardStep.TICKETS_CHECK -> QuestionStep(
                        question = stringResource(R.string.wiz_infiltration_status),
                        subtext = stringResource(R.string.wiz_flight_question),
                        icon = Icons.Default.Flight,
                        options = listOf(
                            OptionItem(stringResource(R.string.wiz_flight_confirmed_title), stringResource(R.string.wiz_flight_confirmed_desc), Icons.Default.CheckCircle) { currentStep = WizardStep.DATE_ENTRY },
                            OptionItem(stringResource(R.string.wiz_flight_negative_title), stringResource(R.string.wiz_flight_negative_desc), Icons.Default.Search) { currentStep = WizardStep.ACQUIRE_TICKETS }
                        )
                    )
                    WizardStep.ACQUIRE_TICKETS -> ActionStep(
                        title = stringResource(R.string.wiz_scan_skynet),
                        subtext = stringResource(R.string.wiz_skyscanner_desc),
                        actionLabel = stringResource(R.string.wiz_launch_skyscanner),
                        actionIcon = Icons.Filled.FlightTakeoff,
                        onAction = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.skyscanner.net"))) },
                        onContinue = { currentStep = WizardStep.DATE_ENTRY },
                        continueLabel = stringResource(R.string.wiz_have_dates)
                    )
                    WizardStep.DATE_ENTRY -> DateEntryStep { startDate = it; currentStep = WizardStep.COMMS_CHECK }
                    WizardStep.COMMS_CHECK -> QuestionStep(
                        question = stringResource(R.string.wiz_comms_check),
                        subtext = stringResource(R.string.wiz_comms_sub),
                        icon = Icons.Default.SignalCellularAlt,
                        options = listOf(
                            OptionItem(stringResource(R.string.wiz_online_title), stringResource(R.string.wiz_online_desc), Icons.Default.Wifi) { needsEsim = false; currentStep = WizardStep.MOBILITY_STRATEGY },
                            OptionItem(stringResource(R.string.wiz_offline_title), stringResource(R.string.wiz_offline_desc), Icons.Default.SimCardDownload) { needsEsim = true; currentStep = WizardStep.MOBILITY_STRATEGY }
                        )
                    )
                    WizardStep.MOBILITY_STRATEGY -> MobilityStep { transportStrategy = it; currentStep = WizardStep.BASE_SECURE }
                    WizardStep.BASE_SECURE -> QuestionStep(
                        question = stringResource(R.string.wiz_fob_title),
                        subtext = stringResource(R.string.wiz_fob_sub),
                        icon = Icons.Default.Home,
                        options = listOf(
                            OptionItem(stringResource(R.string.wiz_fob_secured), "Hotel / Airbnb booked.", Icons.Default.Lock) { needsHotel = false; currentStep = WizardStep.CONFIRMATION },
                            OptionItem(stringResource(R.string.wiz_fob_unsecured), "I need to find lodging.", Icons.Default.Search) { needsHotel = true; currentStep = WizardStep.CONFIRMATION }
                        )
                    )
                    WizardStep.CONFIRMATION -> ConfirmationStep(LogisticsProfile(transportStrategy, needsEsim = needsEsim, needsAccommodation = needsHotel)) { finalizeProfile() }
                }
            }
        }
    }
}

@Composable
fun IntroStep(onStart: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.TravelExplore, null, tint = SakartveloRed, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.auth_welcome), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed), modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) {
            Text(stringResource(R.string.builder_proceed), fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun MobilityStep(onSelected: (TransportStrategy) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.wiz_mobility_protocol), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(Modifier.height(32.dp))
        OptionCard(stringResource(R.string.wiz_hybrid_title), stringResource(R.string.wiz_hybrid_desc), Icons.Default.LocalTaxi) { onSelected(TransportStrategy.PASSENGER_URBAN) }
        OptionCard(stringResource(R.string.wiz_budget_title), stringResource(R.string.wiz_budget_desc), Icons.Default.DirectionsBus) { onSelected(TransportStrategy.PASSENGER_BUDGET) }
        OptionCard(stringResource(R.string.wiz_rental_title), stringResource(R.string.wiz_rental_desc), Icons.Default.Key) { onSelected(TransportStrategy.DRIVER_RENTAL) }
    }
}

@Composable
fun ConfirmationStep(profile: LogisticsProfile, onConfirm: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.VerifiedUser, null, tint = SakartveloRed, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.wiz_params_locked), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(Modifier.weight(1f))
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed), shape = RoundedCornerShape(16.dp)) {
            Text(stringResource(R.string.wiz_initiate), fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StepsProgressBar(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(total) { index ->
            val color = if (index <= current) SakartveloRed else Color.White.copy(alpha = 0.2f)
            Box(Modifier.width(8.dp).height(4.dp).background(color, RoundedCornerShape(2.dp)))
        }
    }
}

data class OptionItem(val title: String, val subtitle: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun QuestionStep(question: String, subtext: String, icon: ImageVector, options: List<OptionItem>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color.White.copy(0.5f), modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(24.dp))
        Text(question, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
        Text(subtext, color = Color.White.copy(0.7f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        options.forEach { option ->
            Surface(onClick = option.onClick, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), color = Color.White.copy(0.05f), border = BorderStroke(1.dp, Color.White.copy(0.1f)), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(option.icon, null, tint = SakartveloRed)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(option.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(option.subtitle, color = Color.White.copy(0.5f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ActionStep(title: String, subtext: String, actionLabel: String, actionIcon: ImageVector, onAction: () -> Unit, continueLabel: String, onContinue: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
        Text(subtext, color = Color.White.copy(0.7f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onAction, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D7E1)), shape = RoundedCornerShape(12.dp)) {
            Icon(actionIcon, null, tint = Color.Black); Spacer(Modifier.width(8.dp)); Text(actionLabel, color = Color.Black, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onContinue) { Text(continueLabel, color = Color.White.copy(0.5f)) }
    }
}

@Composable
fun OptionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), color = Color.White.copy(0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(0.5f), fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateEntryStep(onDateSelected: (Long) -> Unit) {
    val dateState = rememberDatePickerState()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DatePicker(state = dateState, colors = DatePickerDefaults.colors(containerColor = Color.Transparent, titleContentColor = Color.White, headlineContentColor = SakartveloRed, selectedDayContainerColor = SakartveloRed))
        Spacer(Modifier.height(24.dp))
        Button(onClick = { dateState.selectedDateMillis?.let { onDateSelected(it) } }, enabled = dateState.selectedDateMillis != null, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed), shape = RoundedCornerShape(12.dp)) {
            Text(stringResource(R.string.builder_proceed), fontWeight = FontWeight.Black)
        }
    }
}