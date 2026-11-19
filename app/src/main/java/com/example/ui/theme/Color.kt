package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val VoidBlack = Color(0xFF07090E)
val ObsidianSurface = Color(0xFF0D1117)
val CardSurfaceDark = Color(0xFF131822)

// Neon & Luxury Accent Colors
val NeonCyan = Color(0xFF00E5FF)
val ElectricPurple = Color(0xFF7C4DFF)
val DeepEmerald = Color(0xFF00E676)
val ElectricBlue = Color(0xFF2979FF)
val SunsetPink = Color(0xFFFF2A85)
val AmberGold = Color(0xFFFFB300)

// Glassmorphism Tones
val GlassPanelBg = Color(0x14FFFFFF)
val GlassPanelBgElevated = Color(0x20FFFFFF)
val GlassCardBackground = Color(0x660F141F)
val GlassBorderSubtle = Color(0x1FFFFFFF)
val GlassBorderHighlight = Color(0x33FFFFFF)

// Text Colors
val TextPrimary = Color(0xFFF3F6FC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Light Theme Tones
val LightFrostBg = Color(0xFFF1F5F9)
val LightFrostSurface = Color(0xFFFFFFFF)
val LightCardSurface = Color(0xFFF8FAFC)
val LightGlassCardBg = Color(0xEBFFFFFF)
val LightGlassBorderSubtle = Color(0x18000000)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF94A3B8)

// Dual-Tone Gradient Brushes
val StorageRingBrush = Brush.sweepGradient(
    colors = listOf(
        NeonCyan,
        ElectricPurple,
        SunsetPink,
        NeonCyan
    )
)

val CyanPurpleGradient = Brush.linearGradient(
    colors = listOf(NeonCyan, ElectricPurple)
)

val PurplePinkGradient = Brush.linearGradient(
    colors = listOf(ElectricPurple, SunsetPink)
)

val EmeraldCyanGradient = Brush.linearGradient(
    colors = listOf(DeepEmerald, NeonCyan)
)

val GlassIridescentBorder = Brush.linearGradient(
    colors = listOf(
        Color(0x3500E5FF),
        Color(0x257C4DFF),
        Color(0x20FFFFFF),
        Color(0x1000E5FF)
    )
)
