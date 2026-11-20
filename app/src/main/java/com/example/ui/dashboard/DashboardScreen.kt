package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
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
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.data.model.RamStats
import com.example.data.model.StorageStats
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidPageIndicator
import com.example.ui.components.LiquidRamCard
import com.example.ui.components.LiquidSquircleBadge
import com.example.ui.components.LiquidStorageRing
import com.example.ui.theme.ApksCardTint
import com.example.ui.theme.ApksGradient
import com.example.ui.theme.AudioCardTint
import com.example.ui.theme.AudioGradient
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.DocumentsCardTint
import com.example.ui.theme.DocumentsGradient
import com.example.ui.theme.DownloadsCardTint
import com.example.ui.theme.DownloadsGradient
import com.example.ui.theme.DriveCardTint
import com.example.ui.theme.DriveGradient
import com.example.ui.theme.ElectricBlue
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
import com.example.ui.theme.SunsetPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VaultCardTint
import com.example.ui.theme.VaultGradient
import com.example.ui.theme.VideosCardTint
import com.example.ui.theme.VideosGradient

@Composable
fun DashboardScreen(
    storageStats: StorageStats,
    ramStats: RamStats,
    showStorageBar: Boolean,
    recentFiles: List<FileItem>,
    onCategoryClick: (FileCategory) -> Unit,
    onOptimizeClick: () -> Unit,
    onVaultClick: () -> Unit,
    onViewAllFilesClick: () -> Unit,
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
            // Header: Luxury Storage + Settings + Search + More
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Luxury Storage",
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(42.dp)
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
                            .size(42.dp)
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
                                .size(42.dp)
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
                                text = { Text("Settings", color = if (isDark) TextPrimary else LightTextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onSettingsClick()
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
                                text = { Text("Wireless FTP Share", color = if (isDark) TextPrimary else LightTextPrimary) },
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
                        }
                    }
                }
            }
        }

        // Section 1: RAM Storage Meter (Always visible or toggleable)
        item {
            LiquidRamCard(
                ramStats = ramStats,
                onBoostRam = onBoostRam
            )
        }

        // Section 2: Dual-tone Radial Storage Ring Card + 2x2 Mini Categories (Controlled by showStorageBar)
        if (showStorageBar) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Left: Radial Ring Card
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
                                totalText = "${storageStats.totalFormatted} NVME",
                                size = 140.dp,
                                strokeWidth = 12.dp
                            )

                            // Glowing Tap to Optimize button
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
                                    text = "Tap to Optimize",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonCyan
                                )
                            }
                        }
                    }

                    // Right: 2x2 Category Grid (Images, Videos, Archives, Audio)
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

            // Documents Card & Safe Vault Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Documents Card
                    val docCount = storageStats.categoryCounts[FileCategory.DOCUMENTS] ?: 80
                    val docSize = storageStats.categorySizes[FileCategory.DOCUMENTS]?.let { FileItem.formatFileSize(it) } ?: "2.6 MB"

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
                                    text = "$docSize • $docCount Docs",
                                    color = if (isDark) TextSecondary else LightTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Safe Vault Card
                    val vaultSize = storageStats.categorySizes[FileCategory.OTHER]?.let { FileItem.formatFileSize(it) } ?: "367 MB"
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
                                        text = "Encrypted AES-256",
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

            // Liquid Page Indicator (Exact match from Screenshot 1 & 3)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidPageIndicator(
                        pageCount = 3,
                        currentPage = 0
                    )
                }
            }
        }

        // Section 3: INTERNAL STORAGE • 9 items (Exact 3x3 layout from User's Screenshot!)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INTERNAL STORAGE • 9 items",
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
            val imgSize = storageStats.categorySizes[FileCategory.IMAGES]?.let { FileItem.formatFileSize(it) } ?: "238 MB"
            val vidSize = storageStats.categorySizes[FileCategory.VIDEOS]?.let { FileItem.formatFileSize(it) } ?: "55.3 MB"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Images",
                    subtitle = "$imgSize • Edit Oct",
                    icon = Icons.Filled.Image,
                    gradient = ImagesGradient,
                    cardTint = ImagesCardTint,
                    onClick = { onCategoryClick(FileCategory.IMAGES) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Videos",
                    subtitle = "$vidSize • Edit Oct",
                    icon = Icons.Filled.Videocam,
                    gradient = VideosGradient,
                    cardTint = VideosCardTint,
                    onClick = { onCategoryClick(FileCategory.VIDEOS) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Files",
                    subtitle = "${storageStats.totalFormatted} • Internal",
                    icon = Icons.Filled.Folder,
                    gradient = FilesGradient,
                    cardTint = FilesCardTint,
                    onClick = onViewAllFilesClick
                )
            }
        }

        // Row 2 of 3: Audio foo..., Documents, Safe Vault
        item {
            val audioSize = storageStats.categorySizes[FileCategory.AUDIO]?.let { FileItem.formatFileSize(it) } ?: "2.5 MB"
            val docSize = storageStats.categorySizes[FileCategory.DOCUMENTS]?.let { FileItem.formatFileSize(it) } ?: "2.6 MB"
            val vaultSize = storageStats.categorySizes[FileCategory.OTHER]?.let { FileItem.formatFileSize(it) } ?: "367 MB"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Audio foo...",
                    subtitle = "$audioSize • 08h Oct",
                    icon = Icons.Filled.MusicNote,
                    gradient = AudioGradient,
                    cardTint = AudioCardTint,
                    onClick = { onCategoryClick(FileCategory.AUDIO) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Documents",
                    subtitle = "$docSize • 09h Oct",
                    icon = Icons.Filled.Description,
                    gradient = DocumentsGradient,
                    cardTint = DocumentsCardTint,
                    onClick = { onCategoryClick(FileCategory.DOCUMENTS) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Safe Vault",
                    subtitle = "$vaultSize • 08h Oct",
                    icon = Icons.Filled.Lock,
                    gradient = VaultGradient,
                    cardTint = VaultCardTint,
                    onClick = onVaultClick
                )
            }
        }

        // Row 3 of 3: Drive Sync, Downloads, APKs & Back...
        item {
            val downSize = storageStats.categorySizes[FileCategory.DOWNLOADS]?.let { FileItem.formatFileSize(it) } ?: "1.2 GB"
            val apkSize = storageStats.categorySizes[FileCategory.APKS]?.let { FileItem.formatFileSize(it) } ?: "480 MB"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Drive Sync",
                    subtitle = "14.2 GB • 12h Oct",
                    icon = Icons.Filled.CloudSync,
                    gradient = DriveGradient,
                    cardTint = DriveCardTint,
                    onClick = onWirelessClick
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Downloads",
                    subtitle = "$downSize • 14h Oct",
                    icon = Icons.Filled.FileDownload,
                    gradient = DownloadsGradient,
                    cardTint = DownloadsCardTint,
                    onClick = { onCategoryClick(FileCategory.DOWNLOADS) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "APKs & Back...",
                    subtitle = "$apkSize • 15h Oct",
                    icon = Icons.Filled.InstallMobile,
                    gradient = ApksGradient,
                    cardTint = ApksCardTint,
                    onClick = { onCategoryClick(FileCategory.APKS) }
                )
            }
        }

        // Section 4: Recent Activity Feed
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
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
                    text = "${recentFiles.size} files",
                    color = if (isDark) TextMuted else LightTextMuted,
                    fontSize = 12.sp
                )
            }
        }

        items(recentFiles) { file ->
            RecentFileItemRow(
                file = file,
                onClick = { onFileClick(file) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
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
