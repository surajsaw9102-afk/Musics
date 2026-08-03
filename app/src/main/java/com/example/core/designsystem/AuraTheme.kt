package com.example.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AuraThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

private val DarkColorScheme = darkColorScheme(
    primary = AuraColors.ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = AuraColors.DarkSurfaceVariant,
    onPrimaryContainer = AuraColors.NeonCyan,
    secondary = AuraColors.NeonCyan,
    onSecondary = Color.Black,
    tertiary = AuraColors.MagentaPulse,
    onTertiary = Color.White,
    background = AuraColors.DarkBackground,
    onBackground = AuraColors.TextPrimaryDark,
    surface = AuraColors.DarkSurface,
    onSurface = AuraColors.TextPrimaryDark,
    surfaceVariant = AuraColors.DarkSurfaceVariant,
    onSurfaceVariant = AuraColors.TextSecondaryDark,
    outline = AuraColors.DarkGlassBorder,
    error = AuraColors.ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = AuraColors.ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = AuraColors.LightSurfaceVariant,
    onPrimaryContainer = AuraColors.ElectricPurple,
    secondary = AuraColors.NeonCyan,
    onSecondary = Color.Black,
    tertiary = AuraColors.MagentaPulse,
    onTertiary = Color.White,
    background = AuraColors.LightBackground,
    onBackground = AuraColors.TextPrimaryLight,
    surface = AuraColors.LightSurface,
    onSurface = AuraColors.TextPrimaryLight,
    surfaceVariant = AuraColors.LightSurfaceVariant,
    onSurfaceVariant = AuraColors.TextSecondaryLight,
    outline = AuraColors.LightGlassBorder,
    error = AuraColors.ErrorRed
)

@Composable
fun AuraTheme(
    themeMode: AuraThemeMode = AuraThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AuraThemeMode.DARK -> true
        AuraThemeMode.LIGHT -> false
        AuraThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuraTypography,
        content = content
    )
}
