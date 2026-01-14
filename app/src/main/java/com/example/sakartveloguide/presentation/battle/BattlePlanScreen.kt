package com.example.sakartveloguide.presentation.battle

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler // REQUIRED IMPORT
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.domain.model.LogisticsProfile
import com.example.sakartveloguide.domain.model.MissionStep
import com.example.sakartveloguide.domain.model.StepStatus
import com.example.sakartveloguide.domain.model.TripPath
import com.example.sakartveloguide.presentation.home.HomeViewModel
import com.example.sakartveloguide.presentation.theme.MatteCharcoal
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import com.example.sakartveloguide.presentation.theme.SnowWhite
import com.example.sakartveloguide.presentation.theme.WineDark

@Composable
fun BattlePlanScreen(
    path: TripPath,
    viewModel: HomeViewModel,
    onFinish: () -> Unit,
    onAbort: () -> Unit
) {
    val thread by viewModel.missionThread.collectAsState()
    val activeIndex by viewModel.activeStepIndex.collectAsState()
    val profile by viewModel.logisticsProfile.collectAsState()
    val listState = rememberLazyListState()

    // 1. ARCHITECT'S FIX: RESTORED MISSION LOCK
    var showAbortDialog by remember { mutableStateOf(false) }

    // Intercept hardware/system back button
    BackHandler(enabled = true) {
        showAbortDialog = true
    }

    // 2. THE WARNING DIALOG
    if (showAbortDialog) {
        AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            containerColor = MatteCharcoal,
            title = {
                Text("MISSION IN PROGRESS", color = SakartveloRed, fontWeight = FontWeight.Black)
            },
            text = {
                Text(
                    "Leaving this screen will abort the current operation logs. Progress will be reset.\n\nAre you sure you want to abort?",
                    color = SnowWhite
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAbortDialog = false
                        onAbort() // Trigger the wipe in ViewModel
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)
                ) {
                    Text("ABORT MISSION", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbortDialog = false }) {
                    Text("HOLD POSITION", color = SnowWhite.copy(alpha = 0.7f))
                }
            }
        )
    }

    // Auto-scroll logic
    LaunchedEffect(activeIndex) {
        if (thread.isNotEmpty()) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    Scaffold(
        containerColor = MatteCharcoal,
        bottomBar = {
            if (activeIndex >= thread.size - 1) {
                Surface(color = MatteCharcoal, tonalElevation = 8.dp) {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("MISSION COMPLETE: STAMP PASSPORT", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header passes the dialog trigger instead of direct abort
            item { MissionHeaderTiny(path, profile, onAbort = { showAbortDialog = true }) }

            itemsIndexed(thread) { index, step ->
                val status = when {
                    index < activeIndex -> StepStatus.SECURED
                    index == activeIndex -> StepStatus.ACTIVE
                    else -> StepStatus.PLANNED
                }

                ObjectiveCard(
                    step = step,
                    status = status,
                    onSecure = { viewModel.onObjectiveSecured() }
                )
            }
        }
    }
}

@Composable
fun ObjectiveCard(step: MissionStep, status: StepStatus, onSecure: () -> Unit) {
    val context = LocalContext.current
    val alpha = when(status) {
        StepStatus.SECURED -> 0.35f
        StepStatus.ACTIVE -> 1f
        StepStatus.PLANNED -> 0.6f
    }

    val isPremium = step is MissionStep.PremiumExperience
    val borderColor = if (status == StepStatus.ACTIVE) {
        if (isPremium) Color(0xFFFFD700) else SakartveloRed
    } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .alpha(alpha),
        colors = CardDefaults.cardColors(containerColor = SnowWhite.copy(alpha = 0.05f)),
        border = borderColor?.let { BorderStroke(if (isPremium) 2.dp else 1.dp, it) },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        isPremium -> Icons.Default.Star
                        status == StepStatus.SECURED -> Icons.Default.CheckCircle
                        else -> Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = null,
                    tint = if (isPremium && status == StepStatus.ACTIVE) Color(0xFFFFD700)
                    else if (status == StepStatus.SECURED) Color.Green
                    else SakartveloRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = step.title,
                    fontWeight = FontWeight.Black,
                    color = if (isPremium && status == StepStatus.ACTIVE) Color(0xFFFFD700) else SnowWhite,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = step.description,
                color = SnowWhite.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 20.sp
            )

            if (status == StepStatus.ACTIVE) {
                Spacer(Modifier.height(20.dp))

                step.actionUrl?.let { url ->
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPremium) Color(0xFFFFD700).copy(alpha = 0.2f) else SnowWhite.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = if (isPremium) "VIEW EXCLUSIVE OFFER" else "EXECUTE INTEL LINK",
                            fontWeight = FontWeight.Bold,
                            color = if (isPremium) Color(0xFFFFD700) else SnowWhite
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = onSecure,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OBJECTIVE SECURED", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun MissionHeaderTiny(path: TripPath, profile: LogisticsProfile?, onAbort: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .background(WineDark)) {

        AsyncImage(
            model = path.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.4f)
        )

        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(colors = listOf(Color.Transparent, MatteCharcoal))
        ))

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Surface(color = SakartveloRed, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = "ACTIVE MISSION",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = SnowWhite,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = path.title.uppercase(),
                    color = SnowWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    lineHeight = 24.sp
                )
            }

            // Pass the onAbort callback to the header icon too
            IconButton(
                onClick = onAbort,
                modifier = Modifier.background(SnowWhite.copy(alpha = 0.1f), CircleShape).align(Alignment.Top)
            ) {
                Icon(Icons.Default.Close, null, tint = SnowWhite)
            }
        }
    }
}