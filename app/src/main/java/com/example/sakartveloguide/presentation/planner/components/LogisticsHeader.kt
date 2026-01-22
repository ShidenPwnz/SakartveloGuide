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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@Composable
fun LogisticsHeader(
    hasBase: Boolean,
    hasFlights: Boolean,
    onBaseSetup: () -> Unit,
    onBaseLink: (String) -> Unit,
    onFlightAction: (String) -> Unit,
    onTransportAction: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Text(
            "TRIP ESSENTIALS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface.copy(0.3f),
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExpandableLogisticTile(
                label = "STAY",
                icon = Icons.Default.Bed,
                isSet = hasBase,
                modifier = Modifier.weight(1f),
                links = listOf("BOOKING" to "booking", "AIRBNB" to "airbnb"),
                onLinkClick = onBaseLink
            )
            ExpandableLogisticTile(
                label = "FLIGHT",
                icon = Icons.Default.Flight,
                isSet = hasFlights,
                modifier = Modifier.weight(1f),
                links = listOf("SKYSCANNER" to "skyscanner", "WIZZAIR" to "wizzair"),
                onLinkClick = onFlightAction
            )
            ExpandableLogisticTile(
                label = "TAXI",
                icon = Icons.Default.LocalTaxi,
                isSet = true,
                modifier = Modifier.weight(1f),
                links = listOf("BOLT" to "bolt", "YANDEX" to "yandex"),
                onLinkClick = onTransportAction
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onBaseSetup,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasBase) Color(0xFF2E7D32).copy(alpha = 0.1f) else SakartveloRed.copy(alpha = 0.1f),
                contentColor = if (hasBase) Color(0xFF2E7D32) else SakartveloRed
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (hasBase) Color(0xFF2E7D32) else SakartveloRed)
        ) {
            Icon(
                imageVector = if (hasBase) Icons.Default.CheckCircle else Icons.Default.Map,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (hasBase) "HOME LOCATION SET" else "SET HOME LOCATION",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
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
    val tint = if (isSet) Color(0xFF2E7D32) else SakartveloRed

    Column(
        modifier = modifier
            .animateContentSize()
            .background(tint.copy(0.08f), RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = tint,
            textAlign = TextAlign.Center
        )

        if (expanded) {
            Spacer(Modifier.height(8.dp))
            links.forEach { (text, action) ->
                Text(
                    text = text,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tint,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onLinkClick(action) }
                )
            }
        }
    }
}