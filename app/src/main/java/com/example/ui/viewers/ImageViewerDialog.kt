package com.example.ui.viewers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.FileItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import java.io.File

@Composable
fun ImageViewerDialog(
    file: FileItem,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
        ) {
            // Top Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x30FFFFFF))
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextPrimary)
                }

                Text(
                    text = file.name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showInfo = !showInfo },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x30FFFFFF))
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = "Info", tint = NeonCyan)
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x30FFFFFF))
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextPrimary)
                    }
                }
            }

            // Image Preview Center
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                val imageFile = File(file.path)
                if (imageFile.exists() && imageFile.length() > 0) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = file.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(file.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(file.formattedSize, color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            // Info Sheet Overlay
            if (showInfo) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    cornerRadius = 22.dp,
                    backgroundColor = Color(0xF0101626)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Image Details", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Path: ${file.path}", color = TextSecondary, fontSize = 11.sp)
                        Text("Size: ${file.formattedSize}", color = TextSecondary, fontSize = 11.sp)
                        Text("Modified: ${file.formattedDate}", color = TextSecondary, fontSize = 11.sp)
                        Text("Type: ${file.extension.uppercase()}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
