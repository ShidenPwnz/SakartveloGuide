package com.example.sakartveloguide.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.R
import com.example.sakartveloguide.domain.model.UserSession
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    session: UserSession,
    onBack: () -> Unit,
    onWipeData: () -> Unit,
    onLanguageChange: (String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onBackground) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            Text(stringResource(R.string.lang_protocol), color = SakartveloRed, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageChip("EN", "English", session.language == "en") { onLanguageChange("en") }
                LanguageChip("GE", "ქართული", session.language == "ka") { onLanguageChange("ka") }
                LanguageChip("RU", "Русский", session.language == "ru") { onLanguageChange("ru") }
                LanguageChip("TR", "Türkçe", session.language == "tr") { onLanguageChange("tr") }
                LanguageChip("HY", "Հայերեն", session.language == "hy") { onLanguageChange("hy") }
                LanguageChip("HE", "עברית", session.language == "iw") { onLanguageChange("iw") }
                LanguageChip("AR", "العربية", session.language == "ar") { onLanguageChange("ar") }
            }
            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.data_mgmt), color = SakartveloRed, style = MaterialTheme.typography.labelSmall)
            SettingsItem(stringResource(R.string.nuke_data), stringResource(R.string.nuke_desc), Icons.Default.DeleteForever, true) { onWipeData() }
            Spacer(Modifier.weight(1f))
            Text("Sakartvelo Guide v1.2.0", color = MaterialTheme.colorScheme.onBackground.copy(0.4f), fontSize = 10.sp)
        }
    }
}

@Composable
fun LanguageChip(code: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(90.dp).height(55.dp),
        color = if (isSelected) SakartveloRed.copy(0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(1.dp, SakartveloRed) else null
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(code, color = if (isSelected) SakartveloRed else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text(label, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), fontSize = 9.sp)
        }
    }
}

@Composable
private fun SettingsItem(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isWarning: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isWarning) SakartveloRed else MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = if (isWarning) SakartveloRed else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(desc, color = MaterialTheme.colorScheme.onSurface.copy(0.4f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}