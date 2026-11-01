package com.example.byhm.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.byhm.data.repository.AppThemeMode

private val RedWhiteLightColorScheme = lightColorScheme(
    primary = LightPrimaryRed,
    onPrimary = Color.White,
    primaryContainer = LightSelectedSurface,
    onPrimaryContainer = LightDeepRed,
    secondary = LightSecondaryText,
    onSecondary = Color.White,
    secondaryContainer = LightSecondarySurface,
    onSecondaryContainer = LightPrimaryText,
    background = LightMainBackground,
    onBackground = LightPrimaryText,
    surface = LightSurfaceCard,
    onSurface = LightPrimaryText,
    surfaceVariant = LightSecondarySurface,
    onSurfaceVariant = LightSecondaryText,
    outline = LightBorder
)

private val EmergencyDarkColorScheme = darkColorScheme(
    primary = DarkPrimaryEmergencyRed,
    onPrimary = Color.White,
    primaryContainer = DarkDeepRedSurface,
    onPrimaryContainer = DarkPrimaryText,
    secondary = DarkSecondaryText,
    onSecondary = DarkMainBackground,
    secondaryContainer = DarkElevatedSurface,
    onSecondaryContainer = DarkPrimaryText,
    background = DarkMainBackground,
    onBackground = DarkPrimaryText,
    surface = DarkSurfaceCard,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkSecondaryBackground,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkDividerBorder
)

@Composable
fun BYHMTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) EmergencyDarkColorScheme else RedWhiteLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
