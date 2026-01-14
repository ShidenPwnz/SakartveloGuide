package com.example.sakartveloguide.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
// FORCE THE CORRECT IMPORT
import androidx.compose.material3.Typography

private val DarkColorScheme = darkColorScheme(
    primary = SakartveloRed,
    secondary = WineDark,
    background = Color(0xFF0F0F0F), // Deepest Black
    surface = Color(0xFF1C1C1C),    // Lighter Charcoal for Cards
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color.White.copy(alpha = 0.12f) // Important for card borders in dark mode
)

private val LightColorScheme = lightColorScheme(
    primary = SakartveloRed,
    secondary = WineDark,
    background = Color(0xFFFFFFFF), // Snow White
    surface = Color(0xFFF5F5F5),    // Mountain Grey for secondary surfaces
    onBackground = Color(0xFF1A1A1A), // Matte Charcoal
    onSurface = Color(0xFF1A1A1A)
)

@Composable
fun SakartveloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        // Ensure this 'Typography' refers to your local UI Type.kt or the M3 class
        typography = Typography(), 
        content = content
    )
}
