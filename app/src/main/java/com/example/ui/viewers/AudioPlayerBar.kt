package com.example.ui.viewers

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@Composable
fun AudioPlayerBar(
    file: FileItem,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0.35f) }

    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w3"
    )
    val wave4 by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(620, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w4"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp),
        cornerRadius = 24.dp,
        backgroundColor = Color(0xF00F1522)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Waveform / Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x3300E5FF)),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    Canvas(modifier = Modifier.size(24.dp)) {
                        val barWidth = 3.dp.toPx()
                        val spacing = 3.dp.toPx()
                        val maxHeight = size.height * 0.8f
                        val heights = listOf(wave1, wave2, wave3, wave4)

                        heights.forEachIndexed { i, h ->
                            val x = i * (barWidth + spacing) + 2.dp.toPx()
                            val barH = maxHeight * h
                            val y = (size.height - barH) / 2f
                            drawRoundRect(
                                color = NeonCyan,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(2.dp.toPx())
                            )
                        }
                    }
                } else {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Title & Scrub slider
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color(0x30FFFFFF)
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play / Pause Button
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = VoidBlack,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}
