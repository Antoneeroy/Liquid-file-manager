package com.example.ui.wireless

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.CyanPurpleGradient
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.GlassBorderSubtle
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

@Composable
fun WirelessShareScreen(
    isFtpRunning: Boolean = false,
    serverUrl: String = "Offline",
    onToggleFtpServer: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_scale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (isDark) TextPrimary else LightTextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Wi-Fi File Portal",
                color = if (isDark) TextPrimary else LightTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Local Wi-Fi HTTP Server Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            cornerRadius = 28.dp,
            backgroundColor = if (isDark) Color(0x35101626) else Color(0xEBFFFFFF)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFtpRunning) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = NeonCyan.copy(alpha = rippleAlpha * 0.4f),
                                radius = (size.minDimension / 2f) * rippleScale
                            )
                            drawCircle(
                                color = ElectricPurple.copy(alpha = rippleAlpha * 0.25f),
                                radius = (size.minDimension / 2f) * (rippleScale * 0.8f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFtpRunning) CyanPurpleGradient else Brush.linearGradient(listOf(Color(0x20FFFFFF), Color(0x10FFFFFF)))
                            )
                            .border(1.5.dp, GlassBorderSubtle, CircleShape)
                            .clickable { onToggleFtpServer() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WifiTethering,
                            contentDescription = "Toggle Wi-Fi Server",
                            tint = if (isFtpRunning) Color.White else if (isDark) TextMuted else LightTextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isFtpRunning) "Local Wi-Fi HTTP Server Online" else "Local Wi-Fi HTTP Server",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFtpRunning) NeonCyan else if (isDark) TextPrimary else LightTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFtpRunning) serverUrl else if (serverUrl != "Offline") serverUrl else "Browse & download shared files on your local Wi-Fi",
                        fontSize = 13.sp,
                        color = if (isFtpRunning) (if (isDark) TextPrimary else LightTextPrimary) else (if (isDark) TextSecondary else LightTextSecondary),
                        fontWeight = if (isFtpRunning) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isFtpRunning) DeepEmerald else if (isDark) TextMuted else LightTextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFtpRunning) "Port: 8080 • Active" else "Offline",
                            fontSize = 12.sp,
                            color = if (isDark) TextSecondary else LightTextSecondary
                        )
                    }

                    if (isFtpRunning) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Server URL", serverUrl)
                                    clipboard.setPrimaryClip(clip)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x2800E5FF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy URL", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onToggleFtpServer,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF2A85)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Stop", color = SunsetPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = onToggleFtpServer,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_start_server")
                        ) {
                            Text("Start Server", color = VoidBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // PC Connection Guide Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Laptop, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "How to connect from your PC / Mac",
                        color = if (isDark) TextPrimary else LightTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Connect your computer and this device to the same local Wi-Fi.\n" +
                        "2. Tap 'Start Server' above to start the local HTTP portal.\n" +
                        "3. Open the shown http:// address in your desktop browser to browse & download files.",
                    color = if (isDark) TextSecondary else LightTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Security confinement card (replaces removed fake P2P Beam)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x2800E676)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = DeepEmerald, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "LAN-Only & Vault Isolated",
                        color = if (isDark) TextPrimary else LightTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Traffic never leaves your local Wi-Fi. Safe Vault & private app data remain inaccessible.",
                        color = if (isDark) TextSecondary else LightTextSecondary,
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}
