package com.example.sakartveloguide.presentation.mission

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.domain.model.*
import com.example.sakartveloguide.presentation.theme.MatteCharcoal
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import com.example.sakartveloguide.presentation.theme.SnowWhite
import java.text.SimpleDateFormat
import java.util.*

private enum class WizardStep {
    INTRO,
    TICKETS_CHECK,
    ACQUIRE_TICKETS,
    DATE_ENTRY,
    COMMS_CHECK,
    MOBILITY_STRATEGY,
    BASE_SECURE,
    CONFIRMATION
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

    // --- ACCUMULATED DATA ---
    var startDate by remember { mutableStateOf<Long?>(null) }
    var needsEsim by remember { mutableStateOf(false) }
    var transportStrategy by remember { mutableStateOf(TransportStrategy.PASSENGER_URBAN) }
    var needsHotel by remember { mutableStateOf(false) }

    fun finalizeProfile() {
        val endDate = startDate?.let { it + ((trip.durationDays - 1) * 24 * 60 * 60 * 1000L) }

        // Map legacy transport type
        val legacyType = when(transportStrategy) {
            TransportStrategy.PASSENGER_URBAN -> TransportType.TAXI
            TransportStrategy.PASSENGER_BUDGET -> TransportType.PUBLIC_TRANSPORT
            TransportStrategy.DRIVER_RENTAL -> TransportType.RENTAL_4X4
            TransportStrategy.DRIVER_OWNER -> TransportType.OWN_CAR
        }

        onConfirm(
            LogisticsProfile(
                transportStrategy = transportStrategy,
                vehicleStatus = if (transportStrategy == TransportStrategy.DRIVER_RENTAL) VehicleStatus.TO_BE_ACQUIRED else VehicleStatus.NONE,
                transportType = legacyType,
                needsAccommodation = needsHotel,
                needsEsim = needsEsim,
                startDate = startDate,
                endDate = endDate
            )
        )
    }

    BackHandler {
        when(currentStep) {
            WizardStep.INTRO -> onDismiss()
            WizardStep.TICKETS_CHECK -> currentStep = WizardStep.INTRO
            WizardStep.ACQUIRE_TICKETS -> currentStep = WizardStep.TICKETS_CHECK
            WizardStep.DATE_ENTRY -> currentStep = WizardStep.TICKETS_CHECK
            WizardStep.COMMS_CHECK -> currentStep = WizardStep.DATE_ENTRY
            WizardStep.MOBILITY_STRATEGY -> currentStep = WizardStep.COMMS_CHECK
            WizardStep.BASE_SECURE -> currentStep = WizardStep.MOBILITY_STRATEGY
            WizardStep.CONFIRMATION -> currentStep = WizardStep.BASE_SECURE
        }
    }

    Scaffold(
        containerColor = MatteCharcoal,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    StepsProgressBar(
                        current = currentStep.ordinal,
                        total = WizardStep.entries.size
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep == WizardStep.INTRO) onDismiss() else {
                            when(currentStep) {
                                WizardStep.TICKETS_CHECK -> currentStep = WizardStep.INTRO
                                WizardStep.ACQUIRE_TICKETS -> currentStep = WizardStep.TICKETS_CHECK
                                WizardStep.DATE_ENTRY -> currentStep = WizardStep.TICKETS_CHECK
                                WizardStep.COMMS_CHECK -> currentStep = WizardStep.DATE_ENTRY
                                WizardStep.MOBILITY_STRATEGY -> currentStep = WizardStep.COMMS_CHECK
                                WizardStep.BASE_SECURE -> currentStep = WizardStep.MOBILITY_STRATEGY
                                WizardStep.CONFIRMATION -> currentStep = WizardStep.BASE_SECURE
                                else -> onDismiss()
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        startDate = System.currentTimeMillis()
                        transportStrategy = TransportStrategy.PASSENGER_URBAN
                        finalizeProfile()
                    }) {
                        Text("ROGUE MODE", color = SakartveloRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { it } togetherWith fadeOut() + slideOutHorizontally { -it }
                },
                label = "WizardTransition"
            ) { step ->
                when(step) {
                    WizardStep.INTRO -> {
                        IntroStep(onStart = { currentStep = WizardStep.TICKETS_CHECK })
                    }
                    WizardStep.TICKETS_CHECK -> {
                        QuestionStep(
                            question = "INFILTRATION STATUS",
                            subtext = "Have you secured air transport assets?",
                            icon = Icons.Default.Flight,
                            options = listOf(
                                OptionItem("CONFIRMED", "I have my flight tickets.", Icons.Default.CheckCircle) { currentStep = WizardStep.DATE_ENTRY },
                                OptionItem("NEGATIVE", "I need to find a route.", Icons.Default.Search) { currentStep = WizardStep.ACQUIRE_TICKETS }
                            )
                        )
                    }
                    WizardStep.ACQUIRE_TICKETS -> {
                        ActionStep(
                            title = "SCANNING SKYNET",
                            subtext = "Use Skyscanner to identify optimal infiltration vectors.",
                            actionLabel = "LAUNCH SKYSCANNER",
                            actionIcon = Icons.Filled.FlightTakeoff, // FIXED: Removed AutoMirrored
                            onAction = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.skyscanner.net")))
                            },
                            onContinue = { currentStep = WizardStep.DATE_ENTRY },
                            continueLabel = "I HAVE DATES NOW"
                        )
                    }
                    WizardStep.DATE_ENTRY -> {
                        DateEntryStep(
                            onDateSelected = { date ->
                                startDate = date
                                currentStep = WizardStep.COMMS_CHECK
                            }
                        )
                    }
                    WizardStep.COMMS_CHECK -> {
                        QuestionStep(
                            question = "COMMS CHECK",
                            subtext = "Connectivity is mission critical.",
                            icon = Icons.Default.SignalCellularAlt,
                            options = listOf(
                                OptionItem("ONLINE", "I have Roaming / SIM.", Icons.Default.Wifi) {
                                    needsEsim = false
                                    currentStep = WizardStep.MOBILITY_STRATEGY
                                },
                                OptionItem("OFFLINE", "I need a local eSIM.", Icons.Default.SimCardDownload) {
                                    needsEsim = true
                                    currentStep = WizardStep.MOBILITY_STRATEGY
                                }
                            )
                        )
                    }
                    WizardStep.MOBILITY_STRATEGY -> {
                        MobilityStep(
                            onSelected = { strategy ->
                                transportStrategy = strategy
                                currentStep = WizardStep.BASE_SECURE
                            }
                        )
                    }
                    WizardStep.BASE_SECURE -> {
                        QuestionStep(
                            question = "FORWARD OPERATING BASE",
                            subtext = "Do you have a safehouse secured?",
                            icon = Icons.Default.Home,
                            options = listOf(
                                OptionItem("SECURED", "Hotel / Airbnb booked.", Icons.Default.Lock) {
                                    needsHotel = false
                                    currentStep = WizardStep.CONFIRMATION
                                },
                                OptionItem("UNSECURED", "I need to find lodging.", Icons.Default.Search) {
                                    needsHotel = true
                                    currentStep = WizardStep.CONFIRMATION
                                }
                            )
                        )
                    }
                    WizardStep.CONFIRMATION -> {
                        ConfirmationStep(
                            profile = LogisticsProfile(transportStrategy, VehicleStatus.NONE, EntryPoint.AIRPORT_TBS, startDate=startDate, needsEsim=needsEsim, needsAccommodation=needsHotel),
                            onConfirm = { finalizeProfile() }
                        )
                    }
                }
            }
        }
    }
}

// --- SUB COMPONENTS ---

@Composable
fun StepsProgressBar(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(total) { index ->
            val color = if (index <= current) SakartveloRed else Color.White.copy(alpha = 0.2f)
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun IntroStep(onStart: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Use a friendly icon like Handshake or EmojiEmotions if available, or keep Architecture
        Icon(Icons.Default.TravelExplore, null, tint = SakartveloRed, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(24.dp))
        Text("WELCOME TO GEORGIA", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            "Let's set up your trip so we can guide you better. Don't worry, you can change this later.",
            color = Color.White.copy(0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("PLAN MY TRIP", fontWeight = FontWeight.Black)
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
            Surface(
                onClick = option.onClick,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(0.1f),
                border = BorderStroke(1.dp, Color.White.copy(0.2f)), // FIXED: Explicit Import
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(option.icon, null, tint = SakartveloRed)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(option.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(option.subtitle, color = Color.White.copy(0.5f), fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White.copy(0.3f))
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

        Button(
            onClick = onAction,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D7E1)), // Skyscanner blue
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(actionIcon, null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text(actionLabel, color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onContinue) {
            Text(continueLabel, color = Color.White.copy(0.5f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateEntryStep(onDateSelected: (Long) -> Unit) {
    val dateState = rememberDatePickerState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("T-MINUS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
        Text("When do boots hit the ground?", color = Color.White.copy(0.7f))

        Spacer(Modifier.height(24.dp))

        DatePicker(
            state = dateState,
            colors = DatePickerDefaults.colors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                headlineContentColor = SakartveloRed,
                weekdayContentColor = Color.White,
                dayContentColor = Color.White,
                selectedDayContainerColor = SakartveloRed,
                todayDateBorderColor = SakartveloRed
            )
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { dateState.selectedDateMillis?.let { onDateSelected(it) } },
            enabled = dateState.selectedDateMillis != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("CONFIRM ARRIVAL", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun MobilityStep(onSelected: (TransportStrategy) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("MOBILITY PROTOCOL", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
        Text("How will you traverse the terrain?", color = Color.White.copy(0.7f))

        Spacer(Modifier.height(32.dp))

        Text("PASSENGER", color = SakartveloRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        OptionCard("Hybrid (Taxi + Walk)", "Recommended. Flexible & Cheap.", Icons.Default.LocalTaxi) { onSelected(TransportStrategy.PASSENGER_URBAN) }
        OptionCard("Budget (Public)", "Metro & Marshrutka.", Icons.Default.DirectionsBus) { onSelected(TransportStrategy.PASSENGER_BUDGET) }

        Spacer(Modifier.height(24.dp))
        Text("PILOT", color = SakartveloRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        OptionCard("Rental 4x4", "Total freedom. Parking required.", Icons.Default.Key) { onSelected(TransportStrategy.DRIVER_RENTAL) }
        OptionCard("Own Vehicle", "Assets already secured.", Icons.Default.DirectionsCar) { onSelected(TransportStrategy.DRIVER_OWNER) }
    }
}

@Composable
fun ConfirmationStep(profile: LogisticsProfile, onConfirm: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.VerifiedUser, null, tint = SakartveloRed, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(24.dp))

        Text("PARAMETERS LOCKED", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(Modifier.height(32.dp))

        SummaryRow("STRATEGY", profile.transportStrategy.name.replace("_", " "))
        SummaryRow("COMMS", if(profile.needsEsim) "ACQUIRE ESIM" else "SECURE")
        SummaryRow("BASE", if(profile.needsAccommodation) "BOOKING REQ" else "SECURE")

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("INITIATE MISSION", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Color.White.copy(0.05f), RoundedCornerShape(8.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = SakartveloRed, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OptionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.1f)) // FIXED: Explicit Import
    ) {
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