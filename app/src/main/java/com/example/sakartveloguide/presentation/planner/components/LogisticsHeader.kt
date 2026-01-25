package com.example.sakartveloguide.presentation.planner.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.R
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@Composable
fun LogisticsHeader(
    imageUrl: String,
    hasBase: Boolean,
    hasFlights: Boolean,
    onBaseSetup: () -> Unit,
    onBaseLink: (String) -> Unit,
    onFlightAction: (String) -> Unit,
    onTransportAction: (String) -> Unit,
    onRentAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(200.dp) // Standardized container height
            .clip(RoundedCornerShape(24.dp))
    ) {
        // LAYER 1: Background
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(20.dp),
            contentScale = ContentScale.Crop
        )

        // LAYER 2: Heavy Tactical Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.95f),
                            Color.Black.copy(alpha = 0.80f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // LAYER 3: Interactive UI
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.trip_essentials),
                fontSize = 10.sp, fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.4f), letterSpacing = 1.sp
            )
            Spacer(Modifier.height(16.dp))

            // THE GRID: Unified heights for all 4 tiles
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpandableLogisticTile(stringResource(R.string.label_stay), Icons.Default.Bed, hasBase, Modifier.weight(1f), listOf("BOOKING" to "booking", "AIRBNB" to "airbnb"), onBaseLink)
                ExpandableLogisticTile(stringResource(R.string.label_flight), Icons.Default.Flight, hasFlights, Modifier.weight(1f), listOf("SKYSCANNER" to "sky", "WIZZAIR" to "wizz"), onFlightAction)
                ExpandableLogisticTile(stringResource(R.string.label_taxi), Icons.Default.LocalTaxi, true, Modifier.weight(1f), listOf("BOLT" to "bolt", "YANDEX" to "yandex"), onTransportAction)

                // Unified RENT tile
                Surface(
                    onClick = onRentAction,
                    modifier = Modifier.weight(1f).height(74.dp), // FIXED HEIGHT
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Key, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.label_rent), fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center, lineHeight = 10.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp)) // ARCHITECT'S FIX: Added clearance to prevent overlap

            // HOME ACTION BUTTON
            Button(
                onClick = onBaseSetup,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasBase) Color(0xFF4CAF50).copy(alpha = 0.15f) else SakartveloRed.copy(alpha = 0.15f),
                    contentColor = if (hasBase) Color(0xFF81C784) else SakartveloRed
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (hasBase) Color(0xFF4CAF50).copy(alpha = 0.4f) else SakartveloRed.copy(alpha = 0.4f))
            ) {
                Icon(if (hasBase) Icons.Default.CheckCircle else Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (hasBase) stringResource(R.string.home_secured) else stringResource(R.string.set_home_title),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                    if (!hasBase) {
                        Text(
                            text = stringResource(R.string.set_home_sub),
                            fontSize = 8.sp, // MICROSCOPIC SUBTEXT
                            fontWeight = FontWeight.Normal,
                            color = SakartveloRed.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableLogisticTile(
    label: String,
    icon: ImageVector,
    isSet: Boolean,
    modifier: Modifier,
    links: List<Pair<String, String>>,
    onLinkClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val tint = if (isSet) Color(0xFF81C784) else SakartveloRed

    Column(
        modifier = modifier
            .height(74.dp) // FIXED HEIGHT FOR ALIGNMENT
            .animateContentSize()
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!expanded) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = tint, maxLines = 1, textAlign = TextAlign.Center)
        } else {
            links.forEach { (txt, action) ->
                Text(
                    text = txt,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onLinkClick(action) },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}