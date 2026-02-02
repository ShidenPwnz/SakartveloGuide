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
    onBaseLink: (String) -> Unit,
    onFlightAction: (String) -> Unit,
    onTransportAction: (String) -> Unit,
    onRentAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.matchParentSize().blur(20.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .matchParentSize()
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

        Column(modifier = Modifier.padding(16.dp).animateContentSize()) {
            Text(
                text = stringResource(R.string.trip_essentials),
                fontSize = 10.sp, fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.4f), letterSpacing = 1.sp
            )
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpandableLogisticTile(stringResource(R.string.label_stay), Icons.Default.Bed, hasBase, Modifier.weight(1f), listOf("BOOKING" to "booking", "AIRBNB" to "airbnb"), onBaseLink)
                ExpandableLogisticTile(stringResource(R.string.label_flight), Icons.Default.Flight, hasFlights, Modifier.weight(1f), listOf("SKYSCANNER" to "sky", "WIZZAIR" to "wizz"), onFlightAction)
                ExpandableLogisticTile(stringResource(R.string.label_taxi), Icons.Default.LocalTaxi, true, Modifier.weight(1f), listOf("BOLT" to "bolt", "YANDEX" to "yandex"), onTransportAction)

                Surface(
                    onClick = onRentAction,
                    modifier = Modifier.weight(1f).height(74.dp),
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
            .defaultMinSize(minHeight = 74.dp)
            .animateContentSize()
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (expanded) Arrangement.Top else Arrangement.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))

        if (!expanded) {
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = tint, maxLines = 1, textAlign = TextAlign.Center)
        } else {
            Spacer(Modifier.height(4.dp))
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