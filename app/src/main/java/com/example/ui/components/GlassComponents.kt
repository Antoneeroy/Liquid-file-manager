package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RamStats
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassIridescentBorder
import com.example.ui.theme.LightFrostBg
import com.example.ui.theme.LightGlassBorderSubtle
import com.example.ui.theme.LightGlassCardBg
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ambient Animated Mesh Background tuned for Android 8+ 60fps performance:
 * caches brushes and color arrays while gently animating bloom coordinates.
 */
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
            animation = tween(32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_glow1"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val bgBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF06080F),
                    Color(0xFF0A0E1A),
                    Color(0xFF07090E)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF8FAFC),
                    Color(0xFFEEF2F6),
                    LightFrostBg
                )
            )
        }
    }

    val cyanColors = remember(isDark) {
        val mult = if (isDark) 1.0f else 0.42f
        listOf(
            NeonCyan.copy(alpha = 0.17f * mult),
            NeonCyan.copy(alpha = 0.04f * mult),
            Color.Transparent
        )
    }

    val purpleColors = remember(isDark) {
        val mult = if (isDark) 1.0f else 0.42f
        listOf(
            ElectricPurple.copy(alpha = 0.19f * mult),
            ElectricPurple.copy(alpha = 0.04f * mult),
            Color.Transparent
        )
    }

    val emeraldColors = remember(isDark) {
        val mult = if (isDark) 1.0f else 0.42f
        listOf(
            DeepEmerald.copy(alpha = 0.13f * mult),
            DeepEmerald.copy(alpha = 0.03f * mult),
            Color.Transparent
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val rad1 = Math.toRadians(animOffset1.toDouble())
            val x1 = (width * 0.22f + cos(rad1) * 60f).toFloat()
            val y1 = (height * 0.20f + sin(rad1) * 70f).toFloat()

            val rad2 = Math.toRadians((animOffset1 + 135).toDouble())
            val x2 = (width * 0.85f + cos(rad2) * 75f).toFloat()
            val y2 = (height * 0.42f + sin(rad2) * 60f).toFloat()

            val rad3 = Math.toRadians((animOffset1 + 250).toDouble())
            val x3 = (width * 0.40f + cos(rad3) * 55f).toFloat()
            val y3 = (height * 0.75f + sin(rad3) * 75f).toFloat()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = cyanColors,
                    center = Offset(x1, y1),
                    radius = (width * 0.68f) * pulseScale
                ),
                radius = (width * 0.68f) * pulseScale,
                center = Offset(x1, y1)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = purpleColors,
                    center = Offset(x2, y2),
                    radius = (width * 0.74f) * (2f - pulseScale)
                ),
                radius = (width * 0.74f) * (2f - pulseScale),
                center = Offset(x2, y2)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = emeraldColors,
                    center = Offset(x3, y3),
                    radius = width * 0.58f
                ),
                radius = width * 0.58f,
                center = Offset(x3, y3)
            )
        }

        content()
    }
}

/**
 * High-performance Liquid Glass Card with cached shapes/brushes for 60fps scrolling.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderBrush: Brush? = null,
    backgroundColor: Color? = null,
    accentGlowColor: Color? = null,
    borderWidth: Dp = 1.dp,
    specularHighlight: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && (onClick != null || onLongClick != null)) 0.975f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "glass_press_scale"
    )

    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }

    val resolvedBg = remember(backgroundColor, isDark, accentGlowColor) {
        backgroundColor ?: if (isDark) {
            if (accentGlowColor != null) Color(0x75121828) else GlassCardBackground
        } else {
            LightGlassCardBg
        }
    }

    val resolvedBorder = remember(borderBrush, isDark, accentGlowColor) {
        borderBrush ?: if (isDark) {
            if (accentGlowColor != null) {
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        accentGlowColor.copy(alpha = 0.34f),
                        Color.White.copy(alpha = 0.10f),
                        accentGlowColor.copy(alpha = 0.05f)
                    )
                )
            } else {
                GlassIridescentBorder
            }
        } else {
            SolidColor(LightGlassBorderSubtle)
        }
    }

    val topGlareColors = remember(isDark) {
        listOf(
            Color.White.copy(alpha = if (isDark) 0.09f else 0.35f),
            Color.White.copy(alpha = if (isDark) 0.02f else 0.10f),
            Color.Transparent
        )
    }

    val hairlineBrush = remember(isDark) {
        Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = if (isDark) 0.38f else 0.58f),
                Color.Transparent
            )
        )
    }

    val accentColors = remember(accentGlowColor) {
        accentGlowColor?.let { listOf(it.copy(alpha = 0.08f), Color.Transparent) }
    }

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick
        )
    } else Modifier

    val shadowSpotColor = remember(accentGlowColor, isDark) {
        (accentGlowColor ?: NeonCyan).copy(alpha = if (isDark) 0.15f else 0.08f)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isDark) 6.dp else 3.dp,
                shape = shape,
                spotColor = shadowSpotColor,
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.12f)
            )
            .clip(shape)
            .background(resolvedBg)
            .drawBehind {
                val radiusPx = cornerRadius.toPx()
                if (specularHighlight) {
                    val highlightHeight = size.height * 0.38f
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = topGlareColors,
                            startY = 0f,
                            endY = highlightHeight
                        ),
                        size = Size(size.width, highlightHeight),
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )

                    drawLine(
                        brush = hairlineBrush,
                        start = Offset(size.width * 0.15f, 0.8f),
                        end = Offset(size.width * 0.85f, 0.8f),
                        strokeWidth = 1.2f
                    )
                }

                if (accentColors != null && isDark) {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = accentColors,
                            center = Offset(size.width * 0.5f, size.height * 0.35f),
                            radius = size.maxDimension * 0.6f
                        ),
                        size = size,
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )
                }
            }
            .border(borderWidth, resolvedBorder, shape)
            .then(clickModifier),
        content = content
    )
}

@Composable
fun LiquidSquircleBadge(
    icon: ImageVector,
    gradient: List<Color>,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    iconSize: Dp = 26.dp,
    cornerRadius: Dp = 18.dp
) {
    val primaryColor = gradient.firstOrNull() ?: NeonCyan
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val gradientBrush = remember(gradient) { Brush.linearGradient(gradient) }
    val glossColors = remember {
        listOf(
            Color.White.copy(alpha = 0.30f),
            Color.White.copy(alpha = 0.06f),
            Color.Transparent
        )
    }
    val edgeHighlight = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.60f),
                Color.Transparent
            )
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 8.dp,
                shape = shape,
                spotColor = primaryColor.copy(alpha = 0.45f),
                ambientColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(gradientBrush)
            .drawBehind {
                val rPx = cornerRadius.toPx()
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = glossColors,
                        startY = 0f,
                        endY = this.size.height * 0.45f
                    ),
                    size = Size(this.size.width, this.size.height * 0.45f),
                    cornerRadius = CornerRadius(rPx, rPx)
                )

                drawLine(
                    brush = edgeHighlight,
                    start = Offset(this.size.width * 0.15f, 1f),
                    end = Offset(this.size.width * 0.85f, 1f),
                    strokeWidth = 1.2f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun LiquidPageIndicator(
    pageCount: Int = 3,
    currentPage: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            if (i == currentPage) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(4.5.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            spotColor = NeonCyan.copy(alpha = 0.8f)
                        )
                        .clip(CircleShape)
                        .background(NeonCyan)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(4.5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B485A))
                )
            }
        }
    }
}

@Composable
fun LiquidRamCard(
    ramStats: RamStats,
    onBoostRam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val percentInt = if (ramStats.isAvailable) (ramStats.usedPercentage * 100).toInt().coerceIn(0, 100) else 0
    val barBrush = remember { Brush.linearGradient(listOf(NeonCyan, ElectricPurple)) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        accentGlowColor = NeonCyan
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
                    LiquidSquircleBadge(
                        icon = Icons.Filled.Memory,
                        gradient = listOf(Color(0xFF00E5FF), Color(0xFF0091EA)),
                        contentDescription = "RAM",
                        size = 42.dp,
                        iconSize = 22.dp,
                        cornerRadius = 14.dp
                    )
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
                            text = if (ramStats.isAvailable) {
                                "${ramStats.usedFormatted} / ${ramStats.totalFormatted} ($percentInt% used)"
                            } else {
                                "Unavailable"
                            },
                            color = if (isDark) TextSecondary else LightTextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                IconButton(
                    onClick = onBoostRam,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x2400E5FF))
                        .border(1.dp, Color(0x4000E5FF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Refresh & trim memory",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x25FFFFFF) else Color(0x18000000))
            ) {
                if (ramStats.isAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ramStats.usedPercentage.coerceIn(0.02f, 1f))
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(barBrush)
                    )
                }
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
    isAvailable: Boolean = true,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp
) {
    val isDark = LocalIsDarkTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "ring_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val ringSweepBrush = remember {
        Brush.sweepGradient(
            0.0f to NeonCyan,
            0.45f to ElectricPurple,
            0.75f to SunsetPink,
            1.0f to NeonCyan
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size.minDimension
            val strokePx = strokeWidth.toPx()
            val arcSize = canvasSize - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            drawArc(
                color = if (isDark) Color(0x22FFFFFF) else Color(0x18000000),
                startAngle = -220f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx * 0.75f, cap = StrokeCap.Round)
            )

            if (isAvailable) {
                val activeSweep = (260f * progress.coerceIn(0.03f, 1f))

                drawArc(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.36f * glowPulse),
                            ElectricPurple.copy(alpha = 0.14f * glowPulse),
                            Color.Transparent
                        ),
                        center = center,
                        radius = canvasSize * 0.58f
                    ),
                    startAngle = -220f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx * 1.8f, cap = StrokeCap.Round)
                )

                drawArc(
                    brush = ringSweepBrush,
                    startAngle = -220f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isAvailable) usedText else "Unavailable",
                color = if (isDark) TextPrimary else LightTextPrimary,
                fontSize = if (isAvailable) 20.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isAvailable) totalText else "Storage info denied",
                color = if (isDark) Color(0xFF8E9BAE) else LightTextSecondary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.4.sp
            )
        }
    }
}
