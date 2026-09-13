package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RaagaDarkColorScheme = darkColorScheme(
    primary = RaagaAmber,
    onPrimary = Color(0xFF1E1000),
    primaryContainer = Color(0xFF381F00),
    onPrimaryContainer = RaagaAmberLight,
    secondary = RaagaViolet,
    onSecondary = Color(0xFF26003B),
    secondaryContainer = Color(0xFF3F0B61),
    onSecondaryContainer = RaagaVioletLight,
    tertiary = RaagaEmerald,
    onTertiary = Color(0xFF002213),
    background = RaagaDarkBackground,
    onBackground = RaagaTextPrimary,
    surface = RaagaDarkSurface,
    onSurface = RaagaTextPrimary,
    surfaceVariant = RaagaDarkSurfaceVariant,
    onSurfaceVariant = RaagaTextSecondary,
    outline = RaagaDarkSurfaceHighlight,
    outlineVariant = RaagaDivider
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RaagaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
