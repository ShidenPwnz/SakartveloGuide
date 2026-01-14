package com.example.sakartveloguide.presentation.passport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.data.local.entity.PassportEntity
import com.example.sakartveloguide.presentation.theme.SakartveloRed
import com.example.sakartveloguide.presentation.theme.SnowWhite
import com.example.sakartveloguide.presentation.theme.WineDark
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassportScreen(
    stamps: List<PassportEntity>,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = WineDark, // The cover of the passport
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("GEORGIAN PASSPORT", color = SnowWhite, fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SnowWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WineDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(SnowWhite) // The "Paper" of the internal pages
                .padding(24.dp)
        ) {
            Text("REGION VISAS", style = MaterialTheme.typography.labelLarge, color = Color.Gray)

            Spacer(Modifier.height(24.dp))

            if (stamps.isEmpty()) {
                EmptyPassportState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(stamps) { stamp ->
                        InkStamp(stamp)
                    }
                }
            }
        }
    }
}

@Composable
fun InkStamp(stamp: PassportEntity) {
    val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(stamp.dateUnlocked))

    Box(
        modifier = Modifier
            .size(150.dp)
            .drawBehind {
                drawCircle(
                    color = SakartveloRed,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    ),
                    alpha = 0.6f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stamp.regionName.uppercase(),
                color = SakartveloRed,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
            Text(
                text = "APPROVED",
                color = SakartveloRed.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = dateStr,
                color = SakartveloRed,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyPassportState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "NO VISAS ISSUED YET.\nCOMPLETE A MISSION TO EARN A STAMP.",
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 20.sp
        )
    }
}