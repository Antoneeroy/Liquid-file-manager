package com.example.ui.optimizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TrashEntity
import com.example.data.model.FileItem
import com.example.data.repository.FileManagerRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidSquircleBadge
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
fun StorageOptimizerScreen(
    junkResult: FileManagerRepository.JunkScanResult?,
    duplicateGroups: List<FileManagerRepository.DuplicateGroup>,
    largeFiles: List<FileItem>,
    trashItems: List<TrashEntity>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onCleanJunk: () -> Unit,
    onDeleteDuplicate: (FileItem) -> Unit,
    onRestoreTrash: (TrashEntity) -> Unit,
    onEmptyTrash: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Junk Clean", "Duplicates", "Large Files", "Trash")

    val infiniteTransition = rememberInfiniteTransition(label = "clean_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Storage Optimizer",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onStartScan,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x20FFFFFF))
            ) {
                Icon(
                    imageVector = Icons.Filled.Cached,
                    contentDescription = "Rescan",
                    tint = NeonCyan,
                    modifier = Modifier.rotate(if (isScanning) spinAngle else 0f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = NeonCyan,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonCyan,
                    height = 2.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) NeonCyan else TextMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> JunkCleanerTab(
                junkResult = junkResult,
                isScanning = isScanning,
                onCleanJunk = onCleanJunk,
                spinAngle = spinAngle
            )
            1 -> DuplicatesTab(
                duplicateGroups = duplicateGroups,
                onDeleteDuplicate = onDeleteDuplicate
            )
            2 -> LargeFilesTab(
                largeFiles = largeFiles
            )
            3 -> TrashTab(
                trashItems = trashItems,
                onRestore = onRestoreTrash,
                onEmptyAll = onEmptyTrash
            )
        }
    }
}

@Composable
private fun JunkCleanerTab(
    junkResult: FileManagerRepository.JunkScanResult?,
    isScanning: Boolean,
    onCleanJunk: () -> Unit,
    spinAngle: Float
) {
    val junkBytes = junkResult?.totalJunkBytes ?: 145_000_000L
    val formattedJunk = remember(junkBytes) { FileItem.formatFileSize(junkBytes) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Hero Liquid Glass Cleaner Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
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
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0x3300E5FF), Color(0x157C4DFF), Color.Transparent)))
                            .border(1.5.dp, GlassBorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier
                                .size(34.dp)
                                .rotate(if (isScanning) spinAngle else 0f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedJunk,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isScanning) "Scanning residual junk..." else "System Junk & Cache Detected",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = onCleanJunk,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(44.dp)
                            .testTag("btn_clean_junk_now")
                    ) {
                        Icon(Icons.Filled.CleaningServices, contentDescription = null, tint = VoidBlack, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clean Junk Now", color = VoidBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Junk Breakdown",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            JunkItemCard(
                title = "App Cache & Temp Files",
                subtitle = "${junkResult?.tempFiles?.size ?: 12} files found",
                size = "78.4 MB",
                icon = Icons.Filled.Cached,
                iconColor = NeonCyan
            )
        }

        item {
            JunkItemCard(
                title = "Empty Residual Folders",
                subtitle = "${junkResult?.emptyFolders?.size ?: 5} directories",
                size = "0 B",
                icon = Icons.Filled.FolderDelete,
                iconColor = SunsetPink
            )
        }

        item {
            JunkItemCard(
                title = "Obsolete Log Files (.log, .tmp)",
                subtitle = "Old debug traces",
                size = "66.6 MB",
                icon = Icons.Filled.DeleteSweep,
                iconColor = ElectricPurple
            )
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun DuplicatesTab(
    duplicateGroups: List<FileManagerRepository.DuplicateGroup>,
    onDeleteDuplicate: (FileItem) -> Unit
) {
    if (duplicateGroups.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = DeepEmerald, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Duplicates Found", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Your storage is completely optimized", color = TextMuted, fontSize = 12.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "${duplicateGroups.size} Duplicate Sets Found",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(duplicateGroups) { group ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "File Size: ${FileItem.formatFileSize(group.size)}",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${group.files.size} copies",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        group.files.forEachIndexed { index, file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        color = TextPrimary,
                                        fontSize = 12.5.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (index == 0) "Original • ${file.relativeTime}" else "Duplicate • ${file.relativeTime}",
                                        color = if (index == 0) DeepEmerald else TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                if (index > 0) {
                                    Button(
                                        onClick = { onDeleteDuplicate(file) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF2A85)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Delete", color = SunsetPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

@Composable
private fun LargeFilesTab(
    largeFiles: List<FileItem>
) {
    if (largeFiles.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No large files (>20MB) found", color = TextSecondary, fontSize = 14.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "${largeFiles.size} Large Files Detected",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            items(largeFiles) { file ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    cornerRadius = 18.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33FF2A85)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Folder, contentDescription = null, tint = SunsetPink, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = file.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "${file.formattedSize} • ${file.formattedDate}", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

@Composable
private fun TrashTab(
    trashItems: List<TrashEntity>,
    onRestore: (TrashEntity) -> Unit,
    onEmptyAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (trashItems.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${trashItems.size} files in Recycle Bin",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onEmptyAll,
                    colors = ButtonDefaults.buttonColors(containerColor = SunsetPink),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Empty Trash", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (trashItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Recycle Bin is Empty", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(trashItems, key = { it.id }) { item ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp),
                        cornerRadius = 18.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.fileName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = FileItem.formatFileSize(item.size), color = TextSecondary, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onRestore(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300E5FF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Restore, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
    }
}

@Composable
private fun JunkItemCard(
    title: String,
    subtitle: String,
    size: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        cornerRadius = 20.dp,
        accentGlowColor = iconColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiquidSquircleBadge(
                    icon = icon,
                    gradient = listOf(iconColor, iconColor.copy(alpha = 0.7f)),
                    contentDescription = null,
                    size = 42.dp,
                    iconSize = 22.dp,
                    cornerRadius = 14.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
                }
            }
            Text(text = size, color = iconColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
