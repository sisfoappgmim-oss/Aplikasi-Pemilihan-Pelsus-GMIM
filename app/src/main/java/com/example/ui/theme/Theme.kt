package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyMedium,
    onPrimaryContainer = Color.White,
    secondary = PenatuaBlue,
    onSecondary = Color.White,
    secondaryContainer = PenatuaBlueLight,
    onSecondaryContainer = PenatuaBlue,
    tertiary = DiakenGold,
    onTertiary = Color.White,
    tertiaryContainer = DiakenGoldLight,
    onTertiaryContainer = DiakenGold,
    background = BgLight,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = DangerRed,
    errorContainer = DangerRedLight,
    onError = Color.White,
    onErrorContainer = DangerRed
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = NavyDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFFCD34D),
    onTertiary = Color(0xFF0F172A),
    background = Color(0xFF0B132B),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1C2541),
    onSurface = Color(0xFFF8FAFC),
    outline = Color(0xFF334155),
    error = Color(0xFFF87171),
    onError = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent GMIM brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
