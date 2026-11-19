package com.example.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FileItem
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import java.io.File

@Composable
fun TextViewerDialog(
    file: FileItem,
    onDismiss: () -> Unit,
    onSaveContent: (String) -> Unit
) {
    val fileObj = remember { File(file.path) }
    var content by remember {
        mutableStateOf(
            if (fileObj.exists()) {
                try { fileObj.readText() } catch (e: Exception) { "Error reading file content." }
            } else ""
        )
    }
    var isEditing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val lines = remember(content) { content.lines() }

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
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x30FFFFFF))
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
                        text = "${lines.size} lines • ${content.length} chars",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(file.name, content)
                            clipboard.setPrimaryClip(clip)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x30FFFFFF))
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }

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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x30FFFFFF))
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = NeonCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Code & Text Editor / Viewer Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x22131926))
                    .padding(12.dp)
            ) {
                if (isEditing) {
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
                } else {
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

private val DeepEmeraldColor = Color(0xFF00E676)
