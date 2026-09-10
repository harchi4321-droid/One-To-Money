package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5B),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = ElectricIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF331C79),
    onSecondaryContainer = Color(0xFFE8DDFF),
    tertiary = BrightEmerald,
    onTertiary = Color(0xFF003919),
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkOutline,
    error = DangerCoral,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    primary = Color(0xFF007A8A),
    onPrimary = Color.White,
    secondary = ElectricIndigo,
    tertiary = BrightEmerald,
    background = DarkBg,
    surface = DarkSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

