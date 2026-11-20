package com.example.ui.dashboard

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExternalVolumeStats
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.data.model.RamStats
import com.example.data.model.StorageStats
import com.example.ui.components.AdMobBannerBar
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidPageIndicator
import com.example.ui.components.LiquidRamCard
import com.example.ui.components.LiquidSquircleBadge
import com.example.ui.components.LiquidStorageRing
import com.example.ui.theme.ApksCardTint
import com.example.ui.theme.ApksGradient
import com.example.ui.theme.AudioCardTint
import com.example.ui.theme.AudioGradient
import com.example.ui.theme.DocumentsCardTint
import com.example.ui.theme.DocumentsGradient
import com.example.ui.theme.DownloadsCardTint
import com.example.ui.theme.DownloadsGradient
import com.example.ui.theme.DriveCardTint
import com.example.ui.theme.DriveGradient
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.FilesCardTint
import com.example.ui.theme.FilesGradient
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.ImagesCardTint
import com.example.ui.theme.ImagesGradient
import com.example.ui.theme.LightGlassCardBg
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VaultCardTint
import com.example.ui.theme.VaultGradient
import com.example.ui.theme.VideosCardTint
import com.example.ui.theme.VideosGradient
import com.example.ui.theme.VoidBlack

@Composable
fun DashboardScreen(
    storageStats: StorageStats,
    ramStats: RamStats,
    showStorageBar: Boolean,
    recentFiles: List<FileItem>,
    vaultCount: Int,
    canRequestAds: Boolean,
    onCategoryClick: (FileCategory) -> Unit,
    onOptimizeClick: () -> Unit,
    onVaultClick: () -> Unit,
    onViewAllFilesClick: () -> Unit,
    onOpenSdCardClick: (String) -> Unit,
    onOpenSafPickerClick: () -> Unit,
    onGrantStoragePermissionClick: () -> Unit,
    onFileClick: (FileItem) -> Unit,
    onSearchClick: () -> Unit,
    onWirelessClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBoostRam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    var showMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Liquid Files",
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AFFFFFF))
                            .border(1.dp, GlassBorderSubtle, CircleShape)
                            .testTag("btn_dashboard_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = if (isDark) TextPrimary else LightTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AFFFFFF))
                            .border(1.dp, GlassBorderSubtle, CircleShape)
                            .testTag("btn_dashboard_search")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = if (isDark) TextPrimary else LightTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0x1AFFFFFF))
                                .border(1.dp, GlassBorderSubtle, CircleShape)
                                .testTag("btn_dashboard_more")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More Options",
                                tint = if (isDark) TextPrimary else LightTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(if (isDark) Color(0xF0101624) else Color(0xF0FFFFFF))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Open Document (System Picker)", color = if (isDark) TextPrimary else LightTextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onOpenSafPickerClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Storage Optimizer", color = if (isDark) TextPrimary else LightTextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onOptimizeClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Wi-Fi LAN File Portal", color = if (isDark) TextPrimary else LightTextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onWirelessClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Safe Vault (AES-256)", color = if (isDark) TextPrimary else LightTextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onVaultClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings & Privacy", color = if (isDark) TextPrimary else LightTextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onSettingsClick()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Storage access prompt only when All Files Access is not granted
        if (!storageStats.hasAllFilesPermission) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    accentGlowColor = NeonCyan
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Storage, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Full Storage Permission Needed",
                                    color = if (isDark) TextPrimary else LightTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Grant access to manage folders, or use System Picker.",
                                    color = if (isDark) TextSecondary else LightTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onGrantStoragePermissionClick,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_grant_storage_access")
                        ) {
                            Text("Allow", color = VoidBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 1: Real Device RAM Memory
        item {
            LiquidRamCard(
                ramStats = ramStats,
                onBoostRam = onBoostRam
            )
        }

        // Section 2: Real Storage Ring + 2x2 Category Quick Grid
        if (showStorageBar) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GlassCard(
                        modifier = Modifier
                            .weight(1.15f)
                            .height(210.dp)
                            .testTag("card_storage_ring"),
                        cornerRadius = 28.dp,
                        onClick = onOptimizeClick
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            LiquidStorageRing(
                                progress = storageStats.usedPercentage,
                                usedText = storageStats.usedFormatted,
                                totalText = if (storageStats.isAvailable) "of ${storageStats.totalFormatted}" else "Unavailable",
                                isAvailable = storageStats.isAvailable,
                                size = 140.dp,
                                strokeWidth = 12.dp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0x1A00E5FF))
                                    .border(1.dp, Color(0x3300E5FF), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (storageStats.isAvailable) "${storageStats.freeFormatted} free" else "Analyze Storage",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonCyan
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(210.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CategorySquareMini(
                                modifier = Modifier.weight(1f),
                                name = "Images",
                                icon = Icons.Filled.Image,
                                gradient = ImagesGradient,
                                testTag = "category_images",
                                onClick = { onCategoryClick(FileCategory.IMAGES) }
                            )
                            CategorySquareMini(
                                modifier = Modifier.weight(1f),
                                name = "Videos",
                                icon = Icons.Filled.Videocam,
                                gradient = VideosGradient,
                                testTag = "category_videos",
                                onClick = { onCategoryClick(FileCategory.VIDEOS) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CategorySquareMini(
                                modifier = Modifier.weight(1f),
                                name = "Archives",
                                icon = Icons.Filled.Archive,
                                gradient = listOf(Color(0xFFFFB300), Color(0xFFFF8F00)),
                                testTag = "category_archives",
                                onClick = { onCategoryClick(FileCategory.ARCHIVES) }
                            )
                            CategorySquareMini(
                                modifier = Modifier.weight(1f),
                                name = "Audio",
                                icon = Icons.Filled.MusicNote,
                                gradient = AudioGradient,
                                testTag = "category_audio",
                                onClick = { onCategoryClick(FileCategory.AUDIO) }
                            )
                        }
                    }
                }
            }

            // Documents Card & Safe Vault Card (Real counts & sizes only)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val docCount = storageStats.categoryCounts[FileCategory.DOCUMENTS] ?: 0
                    val docSize = FileItem.formatFileSize(storageStats.categorySizes[FileCategory.DOCUMENTS] ?: 0L)

                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(86.dp)
                            .testTag("card_documents"),
                        cornerRadius = 24.dp,
                        accentGlowColor = ElectricPurple,
                        onClick = { onCategoryClick(FileCategory.DOCUMENTS) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LiquidSquircleBadge(
                                icon = Icons.Filled.Description,
                                gradient = DocumentsGradient,
                                contentDescription = "Documents",
                                size = 44.dp,
                                iconSize = 22.dp,
                                cornerRadius = 14.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Documents",
                                    color = if (isDark) TextPrimary else LightTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$docSize • $docCount files",
                                    color = if (isDark) TextSecondary else LightTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    GlassCard(
                        modifier = Modifier
                            .weight(1.15f)
                            .height(86.dp)
                            .testTag("card_safe_vault"),
                        cornerRadius = 24.dp,
                        accentGlowColor = NeonCyan,
                        onClick = onVaultClick
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
                                    icon = Icons.Filled.Lock,
                                    gradient = VaultGradient,
                                    contentDescription = "Safe Vault",
                                    size = 44.dp,
                                    iconSize = 22.dp,
                                    cornerRadius = 14.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Safe Vault",
                                        color = if (isDark) TextPrimary else LightTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$vaultCount encrypted",
                                        color = if (isDark) TextSecondary else LightTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Open Vault",
                                tint = if (isDark) Color(0xFF8E9BAE) else LightTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Removable SD Card Banner (Shown ONLY when genuinely mounted and accessible)
            storageStats.sdCardStats?.let { sd ->
                item {
                    SdCardVolumeCard(
                        sd = sd,
                        isDark = isDark,
                        onClick = { onOpenSdCardClick(sd.path) }
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidPageIndicator(pageCount = 3, currentPage = 0)
                }
            }
        }

        // Section 3: Real Internal Storage Categories Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INTERNAL STORAGE • ${storageStats.totalRootItems} items",
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "View All",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onViewAllFilesClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("btn_view_all_internal")
                )
            }
        }

        // Row 1 of 3: Images, Videos, Files
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Images",
                    subtitle = formatCategoryStats(storageStats, FileCategory.IMAGES),
                    icon = Icons.Filled.Image,
                    gradient = ImagesGradient,
                    cardTint = ImagesCardTint,
                    onClick = { onCategoryClick(FileCategory.IMAGES) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Videos",
                    subtitle = formatCategoryStats(storageStats, FileCategory.VIDEOS),
                    icon = Icons.Filled.Videocam,
                    gradient = VideosGradient,
                    cardTint = VideosCardTint,
                    onClick = { onCategoryClick(FileCategory.VIDEOS) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Files",
                    subtitle = if (storageStats.isAvailable) "${storageStats.freeFormatted} free" else "Browse Root",
                    icon = Icons.Filled.Folder,
                    gradient = FilesGradient,
                    cardTint = FilesCardTint,
                    onClick = onViewAllFilesClick
                )
            }
        }

        // Row 2 of 3: Audio, Documents, Safe Vault
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Audio",
                    subtitle = formatCategoryStats(storageStats, FileCategory.AUDIO),
                    icon = Icons.Filled.MusicNote,
                    gradient = AudioGradient,
                    cardTint = AudioCardTint,
                    onClick = { onCategoryClick(FileCategory.AUDIO) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Documents",
                    subtitle = formatCategoryStats(storageStats, FileCategory.DOCUMENTS),
                    icon = Icons.Filled.Description,
                    gradient = DocumentsGradient,
                    cardTint = DocumentsCardTint,
                    onClick = { onCategoryClick(FileCategory.DOCUMENTS) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Safe Vault",
                    subtitle = "$vaultCount AES-256",
                    icon = Icons.Filled.Lock,
                    gradient = VaultGradient,
                    cardTint = VaultCardTint,
                    onClick = onVaultClick
                )
            }
        }

        // Row 3 of 3: System Picker (SAF), Downloads, APKs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Pick File",
                    subtitle = "SAF Browser",
                    icon = Icons.Filled.FolderOpen,
                    gradient = DriveGradient,
                    cardTint = DriveCardTint,
                    onClick = onOpenSafPickerClick
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Downloads",
                    subtitle = formatCategoryStats(storageStats, FileCategory.DOWNLOADS),
                    icon = Icons.Filled.FileDownload,
                    gradient = DownloadsGradient,
                    cardTint = DownloadsCardTint,
                    onClick = { onCategoryClick(FileCategory.DOWNLOADS) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "APKs",
                    subtitle = formatCategoryStats(storageStats, FileCategory.APKS),
                    icon = Icons.Filled.InstallMobile,
                    gradient = ApksGradient,
                    cardTint = ApksCardTint,
                    onClick = { onCategoryClick(FileCategory.APKS) }
                )
            }
        }

        // Non-intrusive AdMob Banner (safely separated from file actions)
        item {
            AdMobBannerBar(canRequestAds = canRequestAds)
        }

        // Section 4: Recent Activity Feed
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${recentFiles.size} file(s)",
                    color = if (isDark) TextMuted else LightTextMuted,
                    fontSize = 12.sp
                )
            }
        }

        if (recentFiles.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp
                ) {
                    Text(
                        text = if (storageStats.hasAllFilesPermission) "No recent files found on device storage." else "Grant storage access or use Pick File (SAF) to view recent files.",
                        color = if (isDark) TextSecondary else LightTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentFiles, key = { it.path }) { file ->
                RecentFileItemRow(
                    file = file,
                    onClick = { onFileClick(file) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

private fun formatCategoryStats(stats: StorageStats, cat: FileCategory): String {
    val size = stats.categorySizes[cat] ?: 0L
    val count = stats.categoryCounts[cat] ?: 0
    return "${FileItem.formatFileSize(size)} • $count"
}

@Composable
private fun SdCardVolumeCard(
    sd: ExternalVolumeStats,
    isDark: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        accentGlowColor = ElectricPurple,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiquidSquircleBadge(
                        icon = Icons.Filled.SdCard,
                        gradient = listOf(ElectricPurple, NeonCyan),
                        contentDescription = sd.label,
                        size = 40.dp,
                        iconSize = 20.dp,
                        cornerRadius = 12.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = sd.label,
                            color = if (isDark) TextPrimary else LightTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${sd.usedFormatted} / ${sd.totalFormatted} (${sd.freeFormatted} free)",
                            color = if (isDark) TextSecondary else LightTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Open SD Card",
                    tint = if (isDark) TextMuted else LightTextMuted,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x25FFFFFF) else Color(0x18000000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(sd.usedPercentage.coerceIn(0.02f, 1f))
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ElectricPurple, NeonCyan)))
                )
            }
        }
    }
}

@Composable
private fun CategorySquareMini(
    modifier: Modifier = Modifier,
    name: String,
    icon: ImageVector,
    gradient: List<Color>,
    testTag: String,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    GlassCard(
        modifier = modifier
            .height(98.dp)
            .testTag(testTag),
        cornerRadius = 22.dp,
        accentGlowColor = gradient.first(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LiquidSquircleBadge(
                icon = icon,
                gradient = gradient,
                contentDescription = name,
                size = 40.dp,
                iconSize = 20.dp,
                cornerRadius = 13.dp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = name,
                color = if (isDark) TextPrimary else LightTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BigSquircleCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    cardTint: Color? = null,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    GlassCard(
        modifier = modifier.height(136.dp),
        cornerRadius = 26.dp,
        accentGlowColor = gradient.first(),
        backgroundColor = if (isDark) cardTint else LightGlassCardBg,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LiquidSquircleBadge(
                icon = icon,
                gradient = gradient,
                contentDescription = title,
                size = 54.dp,
                iconSize = 26.dp,
                cornerRadius = 18.dp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = if (isDark) TextPrimary else LightTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = if (isDark) Color(0xFF8E9BAE) else LightTextSecondary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecentFileItemRow(
    file: FileItem,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val (icon, gradient) = when (file.category) {
        FileCategory.IMAGES -> Icons.Filled.Image to ImagesGradient
        FileCategory.VIDEOS -> Icons.Filled.Videocam to VideosGradient
        FileCategory.AUDIO -> Icons.Filled.MusicNote to AudioGradient
        FileCategory.DOCUMENTS -> Icons.Filled.Description to DocumentsGradient
        FileCategory.ARCHIVES -> Icons.Filled.Archive to listOf(Color(0xFFFFB300), Color(0xFFFF8F00))
        FileCategory.DOWNLOADS -> Icons.Filled.FileDownload to DownloadsGradient
        FileCategory.APKS -> Icons.Filled.InstallMobile to ApksGradient
        else -> Icons.Filled.Folder to FilesGradient
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        cornerRadius = 20.dp,
        accentGlowColor = gradient.first(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidSquircleBadge(
                icon = icon,
                gradient = gradient,
                contentDescription = file.name,
                size = 42.dp,
                iconSize = 20.dp,
                cornerRadius = 13.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${file.formattedSize} • ${file.relativeTime}",
                    color = if (isDark) TextSecondary else LightTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
