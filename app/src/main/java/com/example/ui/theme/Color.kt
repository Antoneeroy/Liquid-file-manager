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
        Color(0x38FFFFFF),
        Color(0x3000E5FF),
        Color(0x257C4DFF),
        Color(0x12FFFFFF)
    )
)

// 3D Category Floating Squircle Gradients
val ImagesGradient = listOf(Color(0xFFFF3366), Color(0xFFFF5E62))
val VideosGradient = listOf(Color(0xFF7F00FF), Color(0xFF9F55FF))
val FilesGradient = listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
val AudioGradient = listOf(Color(0xFFFF0844), Color(0xFFFF4B2B))
val DocumentsGradient = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
val VaultGradient = listOf(Color(0xFF00F2FE), Color(0xFF00E5FF))
val DriveGradient = listOf(Color(0xFF0EA5E9), Color(0xFF2563EB))
val DownloadsGradient = listOf(Color(0xFFFF8A00), Color(0xFFFF5400))
val ApksGradient = listOf(Color(0xFF10B981), Color(0xFF059669))

// Atmospheric Deep Obsidian Card Background Tints
val ImagesCardTint = Color(0x55201026)
val VideosCardTint = Color(0x55161430)
val FilesCardTint = Color(0x550F2030)
val AudioCardTint = Color(0x5522101E)
val DocumentsCardTint = Color(0x55181632)
val VaultCardTint = Color(0x550E242A)
val DriveCardTint = Color(0x550F1D30)
val DownloadsCardTint = Color(0x55261A12)
val ApksCardTint = Color(0x550E261D)
