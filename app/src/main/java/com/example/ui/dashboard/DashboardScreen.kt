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
import com.example.ui.components.LiquidRamCard
import com.example.ui.components.LiquidStorageRing
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
                                iconTint = SunsetPink,
                                iconBg = Color(0x33FF2A85),
                                testTag = "category_images",
                                onClick = { onCategoryClick(FileCategory.IMAGES) }
                            )
                            CategorySquareMini(
                                modifier = Modifier.weight(1f),
                                name = "Videos",
                                icon = Icons.Filled.Videocam,
                                iconTint = ElectricPurple,
                                iconBg = Color(0x337C4DFF),
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
                                iconTint = Color(0xFFFFB300),
                                iconBg = Color(0x33FFB300),
                                testTag = "category_archives",
                                onClick = { onCategoryClick(FileCategory.ARCHIVES) }
                            )
                            CategorySquareMini(
                                modifier = Modifier.weight(1f),
                                name = "Audio",
                                icon = Icons.Filled.MusicNote,
                                iconTint = NeonCyan,
                                iconBg = Color(0x3300E5FF),
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
                            .height(84.dp)
                            .testTag("card_documents"),
                        cornerRadius = 24.dp,
                        onClick = { onCategoryClick(FileCategory.DOCUMENTS) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x287C4DFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = "Documents",
                                    tint = ElectricPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
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
                    GlassCard(
                        modifier = Modifier
                            .weight(1.15f)
                            .height(84.dp)
                            .testTag("card_safe_vault"),
                        cornerRadius = 24.dp,
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
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0x2800E5FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Safe Vault",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
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
                                tint = if (isDark) TextMuted else LightTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Images",
                    subtitle = "238 MB • Edit Oct",
                    icon = Icons.Filled.Image,
                    gradient = listOf(SunsetPink, Color(0xFF9C27B0)),
                    onClick = { onCategoryClick(FileCategory.IMAGES) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Videos",
                    subtitle = "55.3 MB • Edit Oct",
                    icon = Icons.Filled.Videocam,
                    gradient = listOf(ElectricPurple, ElectricBlue),
                    onClick = { onCategoryClick(FileCategory.VIDEOS) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Files",
                    subtitle = "58.0 GB • Internal",
                    icon = Icons.Filled.Folder,
                    gradient = listOf(Color(0xFF00B0FF), Color(0xFF00E5FF)),
                    onClick = onViewAllFilesClick
                )
            }
        }

        // Row 2 of 3: Audio foo..., Documents, Safe Vault
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Audio foo...",
                    subtitle = "2.5 MB • 08h Oct",
                    icon = Icons.Filled.MusicNote,
                    gradient = listOf(Color(0xFFFF2A85), Color(0xFFD81B60)),
                    onClick = { onCategoryClick(FileCategory.AUDIO) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Documents",
                    subtitle = "2.6 MB • 09h Oct",
                    icon = Icons.Filled.Description,
                    gradient = listOf(Color(0xFF7C4DFF), Color(0xFF536DFE)),
                    onClick = { onCategoryClick(FileCategory.DOCUMENTS) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Safe Vault",
                    subtitle = "367 MB • 08h Oct",
                    icon = Icons.Filled.Lock,
                    gradient = listOf(Color(0xFF00E5FF), Color(0xFF00BFA5)),
                    onClick = onVaultClick
                )
            }
        }

        // Row 3 of 3: Drive Sync, Downloads, APKs & Back...
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Drive Sync",
                    subtitle = "14.2 GB • 12h Oct",
                    icon = Icons.Filled.CloudSync,
                    gradient = listOf(Color(0xFF2979FF), Color(0xFF0288D1)),
                    onClick = onWirelessClick
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "Downloads",
                    subtitle = "1.2 GB • 14h Oct",
                    icon = Icons.Filled.FileDownload,
                    gradient = listOf(Color(0xFFFF9100), Color(0xFFFF6D00)),
                    onClick = { onCategoryClick(FileCategory.DOWNLOADS) }
                )
                BigSquircleCard(
                    modifier = Modifier.weight(1f),
                    title = "APKs & Back...",
                    subtitle = "480 MB • 15h Oct",
                    icon = Icons.Filled.InstallMobile,
                    gradient = listOf(Color(0xFF00E676), Color(0xFF00C853)),
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
    iconTint: Color,
    iconBg: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    GlassCard(
        modifier = modifier
            .height(98.dp)
            .testTag(testTag),
        cornerRadius = 22.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
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
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Box(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(if (isDark) Color(0x35121724) else Color(0xEEFFFFFF))
            .border(
                1.dp,
                if (isDark) {
                    Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x10FFFFFF)))
                } else {
                    Brush.linearGradient(listOf(Color(0x1A000000), Color(0x0A000000)))
                },
                RoundedCornerShape(26.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = if (isDark) TextPrimary else LightTextPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = if (isDark) TextMuted else LightTextMuted,
                fontSize = 9.5.sp,
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
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp),
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, iconColor) = when (file.category) {
                FileCategory.IMAGES -> Icons.Filled.Image to SunsetPink
                FileCategory.VIDEOS -> Icons.Filled.Videocam to ElectricPurple
                FileCategory.AUDIO -> Icons.Filled.MusicNote to NeonCyan
                FileCategory.DOCUMENTS -> Icons.Filled.Description to ElectricBlue
                FileCategory.ARCHIVES -> Icons.Filled.Archive to Color(0xFFFFB300)
                else -> Icons.Filled.Folder to NeonCyan
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = file.name,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 13.sp,
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
