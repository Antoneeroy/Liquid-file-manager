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
import androidx.compose.foundation.clickable
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

/**
 * Ambient Animated Mesh Background with multi-stop neon glows (Cyan, Electric Purple, Emerald)
 * that drift continuously across the deep obsidian canvas.
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
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_glow1"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
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
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val rad1 = Math.toRadians(animOffset1.toDouble())
            val x1 = (width * 0.22f + cos(rad1) * 70f).toFloat()
            val y1 = (height * 0.20f + sin(rad1) * 80f).toFloat()

            val rad2 = Math.toRadians((animOffset1 + 135).toDouble())
            val x2 = (width * 0.85f + cos(rad2) * 85f).toFloat()
            val y2 = (height * 0.42f + sin(rad2) * 70f).toFloat()

            val rad3 = Math.toRadians((animOffset1 + 250).toDouble())
            val x3 = (width * 0.40f + cos(rad3) * 60f).toFloat()
            val y3 = (height * 0.75f + sin(rad3) * 85f).toFloat()

            val alphaMultiplier = if (isDark) 1.0f else 0.45f

            // Mesh glow 1: Neon Cyan bloom (top-left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.18f * alphaMultiplier),
                        NeonCyan.copy(alpha = 0.05f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(x1, y1),
                    radius = (width * 0.70f) * pulseScale
                ),
                radius = (width * 0.70f) * pulseScale,
                center = Offset(x1, y1)
            )

            // Mesh glow 2: Electric Purple bloom (mid-right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricPurple.copy(alpha = 0.20f * alphaMultiplier),
                        ElectricPurple.copy(alpha = 0.05f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(x2, y2),
                    radius = (width * 0.78f) * (2f - pulseScale)
                ),
                radius = (width * 0.78f) * (2f - pulseScale),
                center = Offset(x2, y2)
            )

            // Mesh glow 3: Deep Emerald bloom (bottom-center)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DeepEmerald.copy(alpha = 0.14f * alphaMultiplier),
                        DeepEmerald.copy(alpha = 0.03f * alphaMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(x3, y3),
                    radius = width * 0.60f
                ),
                radius = width * 0.60f,
                center = Offset(x3, y3)
            )
        }

        content()
    }
}

/**
 * Premium Liquid Glass Card with:
 * - Realistic multi-layer frosted transparency
 * - Specular top glare & gloss reflection
 * - Iridescent 1px crystalline refraction border
 * - Interactive spring scale physics on press
 * - Optional atmospheric colored ambient glow
 */
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
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth tactile spring scale on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.975f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "glass_press_scale"
    )

    val shape = RoundedCornerShape(cornerRadius)

    val resolvedBg = backgroundColor ?: if (isDark) {
        if (accentGlowColor != null) {
            Color(0x75121828)
        } else {
            GlassCardBackground
        }
    } else {
        LightGlassCardBg
    }

    val resolvedBorder = borderBrush ?: if (isDark) {
        if (accentGlowColor != null) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.30f),
                    accentGlowColor.copy(alpha = 0.35f),
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

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    val shadowSpotColor = (accentGlowColor ?: NeonCyan).copy(alpha = if (isDark) 0.16f else 0.08f)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isDark) 8.dp else 4.dp,
                shape = shape,
                spotColor = shadowSpotColor,
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.5f else 0.15f)
            )
            .clip(shape)
            .background(resolvedBg)
            .drawBehind {
                if (specularHighlight) {
                    // Top Specular Optical Glare: realistic convex light reflection on top 38% of card
                    val highlightHeight = size.height * 0.38f
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.09f else 0.35f),
                                Color.White.copy(alpha = if (isDark) 0.02f else 0.10f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = highlightHeight
                        ),
                        size = Size(size.width, highlightHeight),
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                    )

                    // Hairline Specular Reflection Line at top edge
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (isDark) 0.40f else 0.60f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(size.width * 0.15f, 0.8f),
                        end = Offset(size.width * 0.85f, 0.8f),
                        strokeWidth = 1.2f
                    )
                }

                // If accent glow specified, draw subtle colored inner ambient wash
                if (accentGlowColor != null && isDark) {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentGlowColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.35f),
                            radius = size.maxDimension * 0.6f
                        ),
                        size = size,
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                    )
                }
            }
            .border(borderWidth, resolvedBorder, shape)
            .then(clickModifier),
        content = content
    )
}

/**
 * Floating 3D Squircle Icon Badge with:
 * - Rich vibrant multi-tone gradient
 * - Soft diffuse colored drop shadow / ambient glow
 * - Specular glossy top-bevel highlight
 * - Crisp centered white vector icon
 */
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
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 10.dp,
                shape = shape,
                spotColor = primaryColor.copy(alpha = 0.50f),
                ambientColor = Color.Black.copy(alpha = 0.40f)
            )
            .clip(shape)
            .background(Brush.linearGradient(gradient))
            .drawBehind {
                // Top 3D glossy highlight
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.32f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = this.size.height * 0.45f
                    ),
                    size = Size(this.size.width, this.size.height * 0.45f),
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                )

                // Ultra-fine inner top edge highlight
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.65f),
                            Color.Transparent
                        )
                    ),
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

/**
 * Page Indicator shown in Reference UI:
 * Active glowing cyan capsule pill + subtle inactive rounded dots.
 */
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
            val isSelected = i == currentPage
            if (isSelected) {
                // Active Glowing Cyan Capsule Pill
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
                // Inactive Subtle Rounded Dot
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
    val percentInt = (ramStats.usedPercentage * 100).toInt().coerceIn(1, 99)

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
                        .background(Color(0x2400E5FF))
                        .border(1.dp, Color(0x4000E5FF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Boost RAM",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Glowing Liquid RAM progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x25FFFFFF) else Color(0x18000000))
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

/**
 * Liquid Storage Ring: Dual-tone glowing radial progress bar matching Reference UI
 */
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
        initialValue = 0.75f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
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

            // Background Track with soft glass rim
            drawArc(
                color = if (isDark) Color(0x22FFFFFF) else Color(0x18000000),
                startAngle = -220f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokePx * 0.75f, cap = StrokeCap.Round)
            )

            // Progress Arc with Gradient (Neon Cyan -> Electric Purple -> Sunset Pink)
            val activeSweep = (260f * progress.coerceIn(0.05f, 1f))
            val gradientBrush = Brush.sweepGradient(
                0.0f to NeonCyan,
                0.45f to ElectricPurple,
                0.75f to SunsetPink,
                1.0f to NeonCyan
            )

            // Ambient Glow Under Arc
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.40f * glowPulse),
                        ElectricPurple.copy(alpha = 0.15f * glowPulse),
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
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = totalText,
                color = if (isDark) Color(0xFF8E9BAE) else LightTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.6.sp
            )
        }
    }
}
