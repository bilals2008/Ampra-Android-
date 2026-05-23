package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppTheme {
    LIGHT, DARK, AMOLED, SYSTEM, CYBERPUNK, SOLAR_AMBER, FOREST_MOSS, RETRO_LAVENDER
}

private val CyberpunkColorScheme = darkColorScheme(
    primary = CyberpunkPrimary,
    secondary = CyberpunkSecondary,
    tertiary = CyberpunkTertiary,
    background = CyberpunkBackground,
    surface = CyberpunkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = CyberpunkSurfaceVariant,
    onSurfaceVariant = CyberpunkOnSurfaceVariant,
    primaryContainer = CyberpunkPrimaryContainer,
    onPrimaryContainer = CyberpunkOnPrimaryContainer
)

private val SolarAmberColorScheme = darkColorScheme(
    primary = SolarPrimary,
    secondary = SolarSecondary,
    tertiary = SolarTertiary,
    background = SolarBackground,
    surface = SolarSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFFFFF6F0),
    onSurface = Color(0xFFFFF6F0),
    surfaceVariant = SolarSurfaceVariant,
    onSurfaceVariant = SolarOnSurfaceVariant,
    primaryContainer = SolarPrimaryContainer,
    onPrimaryContainer = SolarOnPrimaryContainer
)

private val ForestMossColorScheme = darkColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = ForestTertiary,
    background = ForestBackground,
    surface = ForestSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFF0FFF4),
    onSurface = Color(0xFFF0FFF4),
    surfaceVariant = ForestSurfaceVariant,
    onSurfaceVariant = ForestOnSurfaceVariant,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestOnPrimaryContainer
)

private val RetroLavenderColorScheme = darkColorScheme(
    primary = LavenderPrimary,
    secondary = LavenderSecondary,
    tertiary = LavenderTertiary,
    background = LavenderBackground,
    surface = LavenderSurface,
    onPrimary = Color(0xFF1A1B26),
    onSecondary = Color(0xFF1A1B26),
    onTertiary = Color(0xFF1A1B26),
    onBackground = Color(0xFFE0E0E6),
    onSurface = Color(0xFFE0E0E6),
    surfaceVariant = LavenderSurfaceVariant,
    onSurfaceVariant = LavenderOnSurfaceVariant,
    primaryContainer = LavenderPrimaryContainer,
    onPrimaryContainer = LavenderOnPrimaryContainer
)


private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D2FF),
    secondary = Color(0xFF10B981),
    tertiary = Color(0xFF9061FF),
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    primaryContainer = Color(0xFF0F172A),
    onPrimaryContainer = Color(0xFF38BDF8)
)

private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFF00FFCC),
    secondary = Color(0xFF00FF66),
    tertiary = Color(0xFFE2E8F0),
    background = AmoledBackground,
    surface = AmoledSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF161616),
    onSurfaceVariant = Color(0xFFA1A1AA),
    primaryContainer = Color(0xFF111111),
    onPrimaryContainer = Color(0xFF00FFCC)
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    secondary = ElectricGreen,
    tertiary = Color(0xFF6366F1),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E40AF)
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.AMOLED -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.CYBERPUNK -> true
        AppTheme.SOLAR_AMBER -> true
        AppTheme.FOREST_MOSS -> true
        AppTheme.RETRO_LAVENDER -> true
    }

    val colorScheme = when (appTheme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.AMOLED -> AmoledColorScheme
        AppTheme.SYSTEM -> if (darkTheme) DarkColorScheme else LightColorScheme
        AppTheme.CYBERPUNK -> CyberpunkColorScheme
        AppTheme.SOLAR_AMBER -> SolarAmberColorScheme
        AppTheme.FOREST_MOSS -> ForestMossColorScheme
        AppTheme.RETRO_LAVENDER -> RetroLavenderColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
