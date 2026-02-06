package com.example.sakartveloguide.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sakartveloguide.R
import com.example.sakartveloguide.domain.model.SakartveloUser
import com.example.sakartveloguide.domain.model.UserSession
import com.example.sakartveloguide.presentation.theme.SakartveloRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: SakartveloUser?,
    session: UserSession,
    onBack: () -> Unit,
    onWipeData: () -> Unit,
    onLogout: () -> Unit,
    onLanguageChange: (String) -> Unit
) {
    // ARCHITECT'S FIX: State to manage the confirmation dialog
    var showNukeConfirm by remember { mutableStateOf(false) }

    if (showNukeConfirm) {
        AlertDialog(
            onDismissRequest = { showNukeConfirm = false },
            title = { Text(stringResource(R.string.nuke_data), color = SakartveloRed, fontWeight = FontWeight.Black) },
            text = { Text("This will permanently delete all your journey progress, passport stamps, and local mission intelligence. This action cannot be reversed.") },
            confirmButton = {
                TextButton(onClick = {
                    showNukeConfirm = false
                    onWipeData()
                }) {
                    Text("ERASE EVERYTHING", color = SakartveloRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNukeConfirm = false }) {
                    Text("CANCEL")
                }
            },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.7f)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Black) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            user?.let {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.1f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (it.photoUrl != null) {
                            AsyncImage(model = it.photoUrl, contentDescription = null, modifier = Modifier.size(50.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Box(modifier = Modifier.size(50.dp).background(SakartveloRed.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = SakartveloRed)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(text = it.displayName ?: "Traveler", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(text = it.email, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                        IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, stringResource(R.string.logout_label), tint = SakartveloRed) }
                    }
                }
            }



            Text(text = stringResource(R.string.lang_protocol), color = SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageChip("EN", "English", session.language == "en") { onLanguageChange("en") }
                LanguageChip("GE", "ქართული", session.language == "ka") { onLanguageChange("ka") }
                LanguageChip("RU", "Русский", session.language == "ru") { onLanguageChange("ru") }
                LanguageChip("TR", "Türkçe", session.language == "tr") { onLanguageChange("tr") }
                LanguageChip("HY", "Հայերեն", session.language == "hy") { onLanguageChange("hy") }
                LanguageChip("IW", "עברית", session.language == "iw") { onLanguageChange("iw") }
                LanguageChip("AR", "العربية", session.language == "ar") { onLanguageChange("ar") }
            }
// ...

            Spacer(Modifier.height(40.dp))

            Text(text = stringResource(R.string.data_mgmt), color = SakartveloRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))

            // ARCHITECT'S FIX: Triggers Dialog instead of direct action
            SettingsItem(
                title = stringResource(R.string.nuke_data),
                desc = stringResource(R.string.nuke_desc),
                icon = Icons.Default.DeleteForever,
                isWarning = true
            ) { showNukeConfirm = true }
        }
    }
}

@Composable
fun LanguageChip(code: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.width(90.dp).height(56.dp), color = if (isSelected) SakartveloRed.copy(0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.3f), shape = RoundedCornerShape(12.dp), border = if (isSelected) BorderStroke(2.dp, SakartveloRed) else null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = code, color = if (isSelected) SakartveloRed else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text(text = label, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), fontSize = 9.sp)
        }
    }
}

@Composable
private fun SettingsItem(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isWarning: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isWarning) SakartveloRed else MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = title, color = if (isWarning) SakartveloRed else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(text = desc, color = MaterialTheme.colorScheme.onSurface.copy(0.4f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}