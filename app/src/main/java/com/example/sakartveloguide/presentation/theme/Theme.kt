package com.example.sakartveloguide.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SakartveloRed,
    secondary = WineDark,
    background = MatteCharcoal,
    surface = Color(0xFF252525),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SakartveloRed,
    secondary = WineDark,
    background = Color.White,
    surface = MountainGrey,
    onBackground = MatteCharcoal,
    onSurface = MatteCharcoal
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