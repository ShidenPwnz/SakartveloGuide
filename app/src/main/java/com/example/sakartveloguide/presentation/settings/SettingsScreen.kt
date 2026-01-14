package com.example.sakartveloguide.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakartveloguide.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onWipeData: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = MatteCharcoal,
            title = { Text(text = "NUCLEAR RESET", color = SakartveloRed, fontWeight = FontWeight.Black) },
            text = { Text(text = "This will permanently delete all mission logs, stamps, and saved logistics. This cannot be undone.", color = SnowWhite) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onWipeData()
                    }, 
                    colors = ButtonDefaults.buttonColors(containerColor = SakartveloRed)
                ) {
                    Text("WIPE EVERYTHING")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = "CANCEL", color = SnowWhite.copy(alpha = 0.6f))
                }
            }
        )
    }

    Scaffold(
        containerColor = MatteCharcoal,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "SYSTEM SETTINGS", color = SnowWhite, fontWeight = FontWeight.Black, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = SnowWhite) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MatteCharcoal)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            
            Text(text = "DATA & PRIVACY", color = SakartveloRed, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(12.dp))

            // Privacy Link (Play Store Requirement)
            SettingsItem(
                title = "Privacy Protocol", 
                desc = "Legal data handling overview", 
                icon = Icons.Default.Shield
            ) {
                // Replace with your actual Privacy Policy URL
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                context.startActivity(intent)
            }

            // Wipe Button (GDPR Requirement)
            SettingsItem(
                title = "Nuclear Reset", 
                desc = "Wipe all local data & mission progress", 
                icon = Icons.Default.DeleteForever, 
                isWarning = true
            ) {
                showDeleteConfirm = true
            }

            Spacer(Modifier.weight(1f))

            Text(text = "SYSTEM INFORMATION", color = SnowWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall)
            Text(text = "Sakartvelo Guide v1.0.8-Alpha", color = SnowWhite.copy(alpha = 0.4f), fontSize = 12.sp)
            Text(text = "Encrypted Local Storage Active", color = Color.Green.copy(alpha = 0.5f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun SettingsItem(
    title: String, 
    desc: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    isWarning: Boolean = false, 
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
        color = SnowWhite.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isWarning) SakartveloRed else SnowWhite.copy(alpha = 0.6f))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = title, color = if (isWarning) SakartveloRed else SnowWhite, fontWeight = FontWeight.Bold)
                Text(text = desc, color = SnowWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}