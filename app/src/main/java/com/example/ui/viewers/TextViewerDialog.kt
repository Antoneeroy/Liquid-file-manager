package com.example.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FileItem
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

private const val MAX_TEXT_PREVIEW_BYTES = 512 * 1024 // 512 KB cap to prevent OOM/ANR

@Composable
fun TextViewerDialog(
    file: FileItem,
    onDismiss: () -> Unit,
    onSaveContent: (String) -> Unit
) {
    val context = LocalContext.current
    var content by remember(file.path) { mutableStateOf("") }
    var isLoading by remember(file.path) { mutableStateOf(true) }
    var errorMsg by remember(file.path) { mutableStateOf<String?>(null) }
    var wasTruncated by remember(file.path) { mutableStateOf(false) }
    var isEditing by remember(file.path) { mutableStateOf(false) }

    LaunchedEffect(file.path) {
        isLoading = true
        errorMsg = null
        val result = withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = if (file.isContentUri) {
                    context.contentResolver.openInputStream(Uri.parse(file.path))
                } else {
                    val f = File(file.path)
                    if (f.exists() && f.canRead()) f.inputStream() else null
                }

                if (inputStream == null) {
                    Triple("", false, "Cannot open file (missing or access denied).")
                } else {
                    inputStream.use { stream ->
                        val buffer = ByteArray(MAX_TEXT_PREVIEW_BYTES + 1)
                        var totalRead = 0
                        while (totalRead < buffer.size) {
                            val read = stream.read(buffer, totalRead, buffer.size - totalRead)
                            if (read == -1) break
                            totalRead += read
                        }
                        val truncated = totalRead > MAX_TEXT_PREVIEW_BYTES
                        val actualBytes = buffer.copyOf(totalRead.coerceAtMost(MAX_TEXT_PREVIEW_BYTES))
                        // Check if mostly binary (null bytes)
                        val sampleCheck = actualBytes.take(512).count { it == 0.toByte() }
                        if (sampleCheck > 8) {
                            Triple("", false, "Binary or unsupported document format (${file.extension.uppercase()}). Open with an external app via Share.")
                        } else {
                            Triple(String(actualBytes, Charsets.UTF_8), truncated, null)
                        }
                    }
                }
            } catch (e: Exception) {
                Triple("", false, "Failed to read file safely.")
            }
        }
        content = result.first
        wasTruncated = result.second
        errorMsg = result.third
        isLoading = false
    }

    val lines = remember(content) { content.lines() }
    val canEdit = !file.isContentUri && !file.isProtected && !wasTruncated && errorMsg == null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x30FFFFFF))
                        .testTag("btn_close_text_viewer")
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextPrimary)
                }

                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        text = file.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = if (wasTruncated) "${lines.size} lines (preview capped at 512 KB)" else "${lines.size} lines • ${content.length} chars",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (errorMsg == null && !isLoading) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(file.name, content)
                                clipboard.setPrimaryClip(clip)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0x30FFFFFF))
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (canEdit && !isLoading) {
                        if (isEditing) {
                            Button(
                                onClick = {
                                    onSaveContent(content)
                                    isEditing = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Save, contentDescription = null, tint = VoidBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", color = VoidBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            IconButton(
                                onClick = { isEditing = true },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x30FFFFFF))
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = NeonCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x22131926))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                    errorMsg != null -> {
                        Text(errorMsg ?: "", color = SunsetPink, fontSize = 13.sp)
                    }
                    isEditing -> {
                        BasicTextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            ),
                            cursorBrush = SolidColor(NeonCyan)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            itemsIndexed(lines) { index, line ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = "%3d ".format(index + 1),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        modifier = Modifier.width(36.dp)
                                    )
                                    Text(
                                        text = line,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = if (line.startsWith("#") || line.startsWith("//")) DeepEmeraldColor else TextPrimary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val DeepEmeraldColor = Color(0xFF00E676)
