package com.revzion.siitglobe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val primaryBlue = Color(0xFF1A73E8)
private val primaryDark = Color(0xFF4DA3FF)
private val secondaryTeal = Color(0xFF00BCD4)
private val backgroundLight = Color(0xFFF5F7FA)
private val backgroundDark = Color(0xFF0F1117)
private val surfaceDark = Color(0xFF1A1D26)
private val errorRed = Color(0xFFE53935)

private val LightColors = lightColorScheme(
    primary = primaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FD),
    onPrimaryContainer = Color(0xFF001C3A),
    secondary = secondaryTeal,
    onSecondary = Color.White,
    background = backgroundLight,
    onBackground = Color(0xFF1A1A2E),
    surface = Color.White,
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFEEF2F8),
    error = errorRed,
    outline = Color(0xFFB0BEC5),
)

private val DarkColors = darkColorScheme(
    primary = primaryDark,
    onPrimary = Color(0xFF001C3A),
    primaryContainer = Color(0xFF00305A),
    onPrimaryContainer = Color(0xFFD3E4FD),
    secondary = secondaryTeal,
    onSecondary = Color(0xFF003544),
    background = backgroundDark,
    onBackground = Color(0xFFE8EAF0),
    surface = surfaceDark,
    onSurface = Color(0xFFE8EAF0),
    surfaceVariant = Color(0xFF252836),
    error = Color(0xFFFF6B6B),
    outline = Color(0xFF455A64),
)

@Composable
fun SIITGlobeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
