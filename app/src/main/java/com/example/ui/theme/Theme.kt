package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = staticCompositionLocalOf { true }

private val LuxuryDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = VoidBlack,
    primaryContainer = Color(0x3300E5FF),
    onPrimaryContainer = NeonCyan,
    secondary = ElectricPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0x337C4DFF),
    onSecondaryContainer = ElectricPurple,
    tertiary = SunsetPink,
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderSubtle,
    outlineVariant = Color(0x12FFFFFF)
)

private val LuxuryLightColorScheme = lightColorScheme(
    primary = Color(0xFF0098B8),
    onPrimary = Color.White,
    primaryContainer = Color(0x2000E5FF),
    onPrimaryContainer = Color(0xFF007791),
    secondary = ElectricPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0x207C4DFF),
    onSecondaryContainer = ElectricPurple,
    tertiary = SunsetPink,
    background = LightFrostBg,
    onBackground = LightTextPrimary,
    surface = LightFrostSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCardSurface,
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorderSubtle,
    outlineVariant = Color(0x12000000)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LuxuryDarkColorScheme else LuxuryLightColorScheme

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
