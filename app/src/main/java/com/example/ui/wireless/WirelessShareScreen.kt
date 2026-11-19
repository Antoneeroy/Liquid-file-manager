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
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    var isP2PScanning by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_scale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
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

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Wireless Access & Share",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Wi-Fi FTP Server Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            cornerRadius = 28.dp,
            backgroundColor = Color(0x35101626)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Animated Ripple Rings around Wi-Fi Icon
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
                            contentDescription = null,
                            tint = if (isFtpRunning) Color.White else TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isFtpRunning) "Wi-Fi File Server Online" else "Wi-Fi File Server",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFtpRunning) NeonCyan else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFtpRunning) serverUrl else "Access & download files from any PC/Mac browser",
                        fontSize = 13.sp,
                        color = if (isFtpRunning) TextPrimary else TextSecondary,
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
                                .background(if (isFtpRunning) DeepEmerald else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFtpRunning) "Port: 8080 • Active" else "Offline",
                            fontSize = 12.sp,
                            color = TextSecondary
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
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Connect your PC/Mac and this phone to the same Wi-Fi.\n" +
                            "2. Tap 'Start Server' above to launch the web file portal.\n" +
                            "3. Type the shown address in any browser (e.g. Chrome, Edge, Safari).\n" +
                            "4. View, browse, and download mobile files with zero cables!",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Direct P2P Luxury Beam
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            onClick = { isP2PScanning = !isP2PScanning }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x337C4DFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Sensors, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Direct P2P Luxury Beam",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isP2PScanning) "Scanning nearby devices..." else "Tap to beam files offline",
                            color = if (isP2PScanning) NeonCyan else TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = isP2PScanning,
                    onCheckedChange = { isP2PScanning = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VoidBlack,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color(0x30FFFFFF)
                    )
                )
            }
        }
    }
}
