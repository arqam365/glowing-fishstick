package com.revzion.siitglobe.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

enum class ThemeMode { LIGHT, DARK, SYSTEM }

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Grey99,
    primaryContainer = Green90,
    onPrimaryContainer = Green10,
    secondary = Teal40,
    onSecondary = Grey99,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    tertiary = DarkGreen40,
    onTertiary = Grey99,
    tertiaryContainer = DarkGreen90,
    onTertiaryContainer = DarkGreen10,
    error = Red40,
    onError = Grey99,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Grey99,
    onBackground = Grey10,
    surface = Grey99,
    onSurface = Grey10,
    surfaceVariant = GreenGrey90,
    onSurfaceVariant = GreenGrey30,
    outline = GreenGrey50,
)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Green20,
    primaryContainer = Green30,
    onPrimaryContainer = Green90,
    secondary = Teal80,
    onSecondary = Teal20,
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,
    tertiary = DarkGreen80,
    onTertiary = DarkGreen20,
    tertiaryContainer = DarkGreen30,
    onTertiaryContainer = DarkGreen90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = Grey10,
    onBackground = Grey90,
    surface = Grey10,
    onSurface = Grey90,
    surfaceVariant = GreenGrey30,
    onSurfaceVariant = GreenGrey80,
    outline = GreenGrey60,
)

@Composable
fun SiitTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = SiitTypography,
        content = content
    )
}
