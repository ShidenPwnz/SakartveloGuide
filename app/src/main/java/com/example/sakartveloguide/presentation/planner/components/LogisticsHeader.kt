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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.R
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
        modifier = Modifier.fillMaxWidth().padding(16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp)).padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.trip_essentials),
            fontSize = 11.sp, fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface.copy(0.3f), letterSpacing = 1.sp
        )
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpandableLogisticTile(stringResource(R.string.label_stay), Icons.Default.Bed, hasBase, Modifier.weight(1f), listOf("BOOKING" to "booking", "AIRBNB" to "airbnb"), onBaseLink)
            ExpandableLogisticTile(stringResource(R.string.label_flight), Icons.Default.Flight, hasFlights, Modifier.weight(1f), listOf("SKYSCANNER" to "sky", "WIZZAIR" to "wizz"), onFlightAction)
            ExpandableLogisticTile(stringResource(R.string.label_taxi), Icons.Default.LocalTaxi, true, Modifier.weight(1f), listOf("BOLT" to "bolt", "YANDEX" to "yandex"), onTransportAction)
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onBaseSetup,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasBase) Color(0xFF2E7D32).copy(0.1f) else SakartveloRed.copy(0.1f),
                contentColor = if (hasBase) Color(0xFF2E7D32) else SakartveloRed
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (hasBase) Color(0xFF2E7D32) else SakartveloRed)
        ) {
            Icon(if (hasBase) Icons.Default.CheckCircle else Icons.Default.Map, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (hasBase) stringResource(R.string.home_secured) else stringResource(R.string.set_home_title), fontWeight = FontWeight.Black, fontSize = 11.sp)
                if (!hasBase) Text(stringResource(R.string.set_home_sub), fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun ExpandableLogisticTile(label: String, icon: ImageVector, isSet: Boolean, modifier: Modifier, links: List<Pair<String, String>>, onLinkClick: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val tint = if (isSet) Color(0xFF2E7D32) else SakartveloRed
    Column(
        modifier = modifier.animateContentSize().background(tint.copy(0.08f), RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = tint)
        if (expanded) {
            links.forEach { (txt, action) ->
                Text(txt, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = tint, modifier = Modifier.padding(vertical = 6.dp).clickable { onLinkClick(action) })
            }
        }
    }
}