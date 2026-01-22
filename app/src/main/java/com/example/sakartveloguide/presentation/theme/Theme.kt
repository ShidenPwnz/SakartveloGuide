package com.example.sakartveloguide.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define the Palette
private val DarkColorScheme = darkColorScheme(
    primary = SakartveloRed,
    onPrimary = Color.White,
    secondary = WineDark,
    background = MatteCharcoal, // #1A1A1A
    onBackground = SnowWhite,   // #FFFFFF
    surface = Color(0xFF252525),
    onSurface = SnowWhite,
    surfaceVariant = Color(0xFF303030),
    onSurfaceVariant = Color(0xFFCCCCCC),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = SakartveloRed,
    onPrimary = Color.White,
    secondary = WineDark,
    background = MountainGrey, // #F5F5F5
    onBackground = MatteCharcoal, // #1A1A1A
    surface = Color.White,
    onSurface = MatteCharcoal,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF4A4A4A),
    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun SakartveloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SakartveloTypography,
        content = content
    )
}