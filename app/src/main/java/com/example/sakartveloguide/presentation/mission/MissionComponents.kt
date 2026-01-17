package com.example.sakartveloguide.presentation.mission

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale // ARCHITECT'S FIX: Missing Import
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.domain.model.EntryPoint
import com.example.sakartveloguide.domain.model.TransportType
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@Composable
fun ProtocolPointCard(label: String, current: EntryPoint, isAir: Boolean, onSelected: (EntryPoint) -> Unit) {
    val options = if (isAir) listOf(EntryPoint.AIRPORT_TBS, EntryPoint.AIRPORT_KUT, EntryPoint.AIRPORT_BUS)
    else listOf(EntryPoint.LAND_TURKEY, EntryPoint.LAND_ARMENIA, EntryPoint.LAND_AZERBAIJAN, EntryPoint.LAND_RUSSIA)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { point ->
                val isSelected = current == point
                Surface(
                    onClick = { onSelected(point) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    color = if (isSelected) SakartveloRed else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val name = point.name.split("_").last()
                        Text(
                            text = name,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        color = if (isSelected) SakartveloRed.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(1.dp, SakartveloRed) else null
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (isSelected) SakartveloRed else MaterialTheme.colorScheme.onSurface.copy(0.4f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(0.4f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun LogisticsToggle(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.4f), RoundedCornerShape(12.dp))
            .clickable { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(icon, null, tint = if (checked) SakartveloRed else MaterialTheme.colorScheme.onSurface.copy(0.3f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), style = MaterialTheme.typography.labelSmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = SakartveloRed),
            modifier = Modifier.scale(0.7f)
        )
    }
}

@Composable
fun TacticalTransportOption(label: String, sub: String, type: TransportType, isSelected: Boolean, onSelected: (TransportType) -> Unit) {
    Surface(
        onClick = { onSelected(type) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = if (isSelected) SakartveloRed.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.2f),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(1.dp, SakartveloRed) else null
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
        }
    }
}

@Composable
fun ReferralLinkBox(
    title: String,
    description: String,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 10.sp)
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text(buttonText, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}