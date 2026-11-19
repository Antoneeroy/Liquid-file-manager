package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RamStats
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassIridescentBorder
import com.example.ui.theme.LightFrostBg
import com.example.ui.theme.LightGlassBorderSubtle
import com.example.ui.theme.LightGlassCardBg
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmbientMeshBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "mesh_glow")
    val animOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_glow1"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) VoidBlack else LightFrostBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val rad1 = Math.toRadians(animOffset1.toDouble())
            val x1 = (width * 0.25f + cos(rad1) * 60f).toFloat()
            val y1 = (height * 0.22f + sin(rad1) * 70f).toFloat()

            val rad2 = Math.toRadians((animOffset1 + 140).toDouble())
            val x2 = (width * 0.82f + cos(rad2) * 80f).toFloat()
            val y2 = (height * 0.45f + sin(rad2) * 60f).toFloat()

            val rad3 = Math.toRadians((animOffset1 + 250).toDouble())
            val x3 = (width * 0.45f + cos(rad3) * 50f).toFloat()
            val y3 = (height * 0.78f + sin(rad3) * 70f).toFloat()

            val alphaMultiplier = if (isDark) 1f else 0.5f

            // Mesh glow 1: Neon Cyan
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.16f * alphaMultiplier),
                        NeonCyan.copy(alpha = 0.05f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(x1, y1),
                    radius = (width * 0.65f) * pulseScale
                ),
                radius = (width * 0.65f) * pulseScale,
                center = Offset(x1, y1)
            )

            // Mesh glow 2: Electric Purple
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricPurple.copy(alpha = 0.18f * alphaMultiplier),
                        ElectricPurple.copy(alpha = 0.04f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(x2, y2),
                    radius = (width * 0.72f) * (2f - pulseScale)
                ),
                radius = (width * 0.72f) * (2f - pulseScale),
                center = Offset(x2, y2)
            )

            // Mesh glow 3: Deep Emerald
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DeepEmerald.copy(alpha = 0.12f * alphaMultiplier),
                        DeepEmerald.copy(alpha = 0.03f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(x3, y3),
                    radius = width * 0.55f
                ),
                radius = width * 0.55f,
                center = Offset(x3, y3)
            )
        }

        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderBrush: Brush? = null,
    backgroundColor: Color? = null,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val resolvedBg = backgroundColor ?: if (isDark) GlassCardBackground else LightGlassCardBg
    val resolvedBorder = borderBrush ?: if (isDark) GlassIridescentBorder else SolidColor(LightGlassBorderSubtle)

    val shape = RoundedCornerShape(cornerRadius)
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .background(resolvedBg)
            .border(borderWidth, resolvedBorder, shape)
            .then(clickModifier),
        content = content
    )
}

@Composable
fun LiquidRamCard(
    ramStats: RamStats,
    onBoostRam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val percentInt = (ramStats.usedPercentage * 100).toInt().coerceIn(1, 99)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2800E5FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = "RAM",
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "RAM Memory",
                            color = if (isDark) TextPrimary else LightTextPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${ramStats.usedFormatted} / ${ramStats.totalFormatted} ($percentInt% used)",
                            color = if (isDark) TextSecondary else LightTextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                IconButton(
                    onClick = onBoostRam,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x2000E5FF))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Boost RAM",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Glowing Liquid RAM progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x20FFFFFF) else Color(0x18000000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(ramStats.usedPercentage.coerceIn(0.08f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NeonCyan, ElectricPurple)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun LiquidStorageRing(
    progress: Float,
    usedText: String,
    totalText: String,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp
) {
    val isDark = LocalIsDarkTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "ring_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size.minDimension
            val strokePx = strokeWidth.toPx()
            val arcSize = canvasSize - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background Track
            drawArc(
                color = if (isDark) Color(0x22FFFFFF) else Color(0x18000000),
                startAngle = -220f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx * 0.75f, cap = StrokeCap.Round)
            )

            // Progress Arc with Gradient
            val activeSweep = (260f * progress.coerceIn(0.05f, 1f))
            val gradientBrush = Brush.sweepGradient(
                0.0f to NeonCyan,
                0.5f to ElectricPurple,
                0.8f to SunsetPink,
                1.0f to NeonCyan
            )

            // Ambient Glow Under Arc
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.35f * glowPulse), Color.Transparent),
                    center = center,
                    radius = canvasSize * 0.55f
                ),
                startAngle = -220f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx * 1.6f, cap = StrokeCap.Round)
            )

            // Foreground Active Arc
            drawArc(
                brush = gradientBrush,
                startAngle = -220f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = usedText,
                color = if (isDark) TextPrimary else LightTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = totalText,
                color = if (isDark) TextSecondary else LightTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}
