package com.example.ui.optimizer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import com.example.ui.components.LiquidNativeAdCard
import com.example.ui.components.LiquidSquircleBadge
import com.example.ui.theme.DeepEmerald
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
import java.io.File

@Composable
fun StorageOptimizerScreen(
    junkResult: FileManagerRepository.JunkScanResult?,
    duplicateGroups: List<FileManagerRepository.DuplicateGroup>,
    largeFiles: List<FileItem>,
    trashItems: List<TrashEntity>,
    isScanning: Boolean,
    canRequestAds: Boolean,
    onStartScan: () -> Unit,
    onCleanJunk: () -> Unit,
    onDeleteDuplicate: (FileItem) -> Unit,
    onRestoreTrash: (TrashEntity) -> Unit,
    onEmptyTrash: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (isDark) TextPrimary else LightTextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Storage Optimizer",
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onStartScan,
                modifier = Modifier
                    .size(40.dp)
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
                            color = if (selectedTab == index) NeonCyan else if (isDark) TextMuted else LightTextMuted
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
                canRequestAds = canRequestAds,
                onCleanJunk = onCleanJunk,
                onStartScan = onStartScan,
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

private fun calculateEntriesSize(files: List<File>?): Long {
    if (files == null) return 0L
    var total = 0L
    for (file in files) {
        if (!file.exists()) continue
        if (file.isDirectory) {
            file.walkTopDown().maxDepth(4).forEach { if (it.isFile) total += it.length() }
        } else {
            total += file.length()
        }
    }
    return total
}

@Composable
private fun JunkCleanerTab(
    junkResult: FileManagerRepository.JunkScanResult?,
    isScanning: Boolean,
    canRequestAds: Boolean,
    onCleanJunk: () -> Unit,
    onStartScan: () -> Unit,
    spinAngle: Float
) {
    val isDark = LocalIsDarkTheme.current
    val junkBytes = junkResult?.totalJunkBytes ?: 0L
    val formattedJunk = remember(junkBytes) { FileItem.formatFileSize(junkBytes) }
    val tempSize = remember(junkResult) { FileItem.formatFileSize(calculateEntriesSize(junkResult?.tempFiles)) }
    val cacheSize = remember(junkResult) { FileItem.formatFileSize(calculateEntriesSize(junkResult?.cacheFiles)) }
    val tempCount = junkResult?.tempFiles?.size ?: 0
    val emptyCount = junkResult?.emptyFolders?.size ?: 0
    val cacheCount = junkResult?.cacheFiles?.size ?: 0
    val hasCleanableJunk = junkBytes > 0L || emptyCount > 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(215.dp),
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
                        modifier = Modifier
                            .size(68.dp)
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
                                .size(32.dp)
                                .rotate(if (isScanning) spinAngle else 0f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isScanning) "Scanning..." else formattedJunk,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextPrimary else LightTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when {
                                isScanning -> "Inspecting accessible temp files, caches & empty dirs..."
                                junkResult == null -> "Tap Rescan to analyze storage"
                                hasCleanableJunk -> "Residual Temp, Log & Cache Detected"
                                else -> "No residual junk detected"
                            },
                            fontSize = 12.sp,
                            color = if (isDark) TextSecondary else LightTextSecondary
                        )
                    }

                    Button(
                        onClick = if (junkResult == null) onStartScan else onCleanJunk,
                        enabled = !isScanning && (junkResult == null || hasCleanableJunk),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(44.dp)
                            .testTag("btn_clean_junk_now")
                    ) {
                        Icon(Icons.Filled.CleaningServices, contentDescription = null, tint = VoidBlack, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (junkResult == null) "Scan Storage Now" else if (hasCleanableJunk) "Clean Junk Now" else "Storage Clean",
                            color = VoidBlack,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Scan Breakdown",
                color = if (isDark) TextPrimary else LightTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            JunkItemCard(
                title = "App Explorer Cache",
                subtitle = "$cacheCount cache entry/entries",
                size = cacheSize,
                icon = Icons.Filled.Cached,
                iconColor = NeonCyan
            )
        }

        item {
            JunkItemCard(
                title = "Empty Folders",
                subtitle = "$emptyCount empty directories",
                size = "0 B",
                icon = Icons.Filled.FolderDelete,
                iconColor = SunsetPink
            )
        }

        item {
            JunkItemCard(
                title = "Temp & Log Files (.tmp, .log)",
                subtitle = "$tempCount file(s) found",
                size = tempSize,
                icon = Icons.Filled.DeleteSweep,
                iconColor = ElectricPurple
            )
        }

        // Non-intrusive Google AdMob Native Ad (safe location below analysis summary)
        item {
            LiquidNativeAdCard(canRequestAds = canRequestAds)
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
    val isDark = LocalIsDarkTheme.current
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
                Text("No Duplicates Found", color = if (isDark) TextPrimary else LightTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("No identical SHA-256 files detected in accessible folders", color = if (isDark) TextMuted else LightTextMuted, fontSize = 12.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "${duplicateGroups.size} Duplicate Group(s) Found",
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(duplicateGroups, key = { "${it.hash}_${it.size}" }) { group ->
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
                                color = if (isDark) TextMuted else LightTextMuted,
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
                                        color = if (isDark) TextPrimary else LightTextPrimary,
                                        fontSize = 12.5.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (index == 0) "Original • ${file.relativeTime}" else "Duplicate • ${file.relativeTime}",
                                        color = if (index == 0) DeepEmerald else if (isDark) TextMuted else LightTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                if (index > 0) {
                                    Button(
                                        onClick = { onDeleteDuplicate(file) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF2A85)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Trash", color = SunsetPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
    val isDark = LocalIsDarkTheme.current
    if (largeFiles.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No large files (>20MB) found", color = if (isDark) TextSecondary else LightTextSecondary, fontSize = 14.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "${largeFiles.size} Large File(s) Detected",
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            items(largeFiles, key = { it.path }) { file ->
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
                            Text(text = file.name, color = if (isDark) TextPrimary else LightTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "${file.formattedSize} • ${file.formattedDate}", color = if (isDark) TextSecondary else LightTextSecondary, fontSize = 11.sp)
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
    val isDark = LocalIsDarkTheme.current
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
                    text = "${trashItems.size} file(s) in Recycle Bin",
                    color = if (isDark) TextPrimary else LightTextPrimary,
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
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = if (isDark) TextMuted.copy(alpha = 0.5f) else LightTextMuted, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Recycle Bin is Empty", color = if (isDark) TextSecondary else LightTextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
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
                                Text(text = item.fileName, color = if (isDark) TextPrimary else LightTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = FileItem.formatFileSize(item.size), color = if (isDark) TextSecondary else LightTextSecondary, fontSize = 11.sp)
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
    val isDark = LocalIsDarkTheme.current
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
                    Text(text = title, color = if (isDark) TextPrimary else LightTextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = subtitle, color = if (isDark) TextMuted else LightTextMuted, fontSize = 11.sp)
                }
            }
            Text(text = size, color = iconColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
