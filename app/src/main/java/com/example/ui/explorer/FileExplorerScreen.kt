package com.example.ui.explorer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClipboardAction
import com.example.data.model.ClipboardItem
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.data.model.SortBy
import com.example.data.model.SortOrder
import com.example.data.model.ViewMode
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidSquircleBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ApksGradient
import com.example.ui.theme.AudioGradient
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.DocumentsGradient
import com.example.ui.theme.DownloadsGradient
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.FilesGradient
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.ImagesGradient
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VideosGradient
import com.example.ui.theme.VoidBlack
import java.io.File

@Composable
fun FileExplorerScreen(
    currentPath: String,
    rootPath: String,
    files: List<FileItem>,
    clipboard: ClipboardItem?,
    onNavigateTo: (String) -> Unit,
    onNavigateUp: () -> Boolean,
    onOpenFile: (FileItem) -> Unit,
    onDeleteToTrash: (List<FileItem>) -> Unit,
    onRename: (FileItem, String) -> Unit,
    onAddToVault: (FileItem) -> Unit,
    onToggleFavorite: (FileItem) -> Unit,
    onCopyFiles: (List<FileItem>) -> Unit,
    onCutFiles: (List<FileItem>) -> Unit,
    onPaste: () -> Unit,
    onClearClipboard: () -> Unit,
    onShareFile: (FileItem) -> Unit,
    selectedCategory: FileCategory? = null,
    onSelectCategory: (FileCategory?) -> Unit = {},
    showHiddenFiles: Boolean = false,
    onToggleShowHiddenFiles: () -> Unit = {},
    onToggleHideFile: (FileItem) -> Unit = {},
    onToggleHideMultiple: (List<FileItem>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var sortBy by remember { mutableStateOf(SortBy.NAME) }
    var sortOrder by remember { mutableStateOf(SortOrder.ASCENDING) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Multi-selection state
    val selectedFiles = remember { mutableStateListOf<FileItem>() }
    val isSelectionMode = selectedFiles.isNotEmpty()

    // File action dialogs
    var fileToRename by remember { mutableStateOf<FileItem?>(null) }
    var newRenameName by remember { mutableStateOf("") }
    var fileToDelete by remember { mutableStateOf<FileItem?>(null) }
    var activeContextMenuFile by remember { mutableStateOf<FileItem?>(null) }

    val focusManager = LocalFocusManager.current

    // Handle back button: if in selection mode, clear selection; if in subfolder, navigate up
    BackHandler(enabled = isSelectionMode || currentPath != rootPath) {
        if (isSelectionMode) {
            selectedFiles.clear()
        } else {
            onNavigateUp()
        }
    }

    // Filter and Sort files
    val displayedFiles = remember(files, searchQuery, selectedCategory, sortBy, sortOrder) {
        files.filter { file ->
            val matchesQuery = searchQuery.isBlank() || file.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || file.category == selectedCategory
            matchesQuery && matchesCategory
        }.sortedWith { a, b ->
            if (a.isDirectory != b.isDirectory) {
                if (a.isDirectory) -1 else 1
            } else {
                val comparison = when (sortBy) {
                    SortBy.NAME -> a.name.compareTo(b.name, ignoreCase = true)
                    SortBy.DATE -> a.lastModified.compareTo(b.lastModified)
                    SortBy.SIZE -> a.size.compareTo(b.size)
                    SortBy.TYPE -> a.extension.compareTo(b.extension)
                }
                if (sortOrder == SortOrder.ASCENDING) comparison else -comparison
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Top Toolbar: Back, Title/Search, ViewMode Toggle, Sort
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isSelectionMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedFiles.clear() }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Cancel Selection", tint = TextPrimary)
                        }
                        Text(
                            text = "${selectedFiles.size} selected",
                            color = NeonCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            if (selectedFiles.size == displayedFiles.size) {
                                selectedFiles.clear()
                            } else {
                                selectedFiles.clear()
                                selectedFiles.addAll(displayedFiles)
                            }
                        }) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Select All", tint = TextPrimary)
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (currentPath != rootPath) {
                            IconButton(
                                onClick = { onNavigateUp() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x1AFFFFFF))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "Explorer",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Toggle View Mode
                        IconButton(
                            onClick = {
                                viewMode = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x1AFFFFFF))
                        ) {
                            Icon(
                                imageVector = if (viewMode == ViewMode.LIST) Icons.Filled.GridView else Icons.Filled.ViewList,
                                contentDescription = "Toggle View",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Sort Menu
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x1AFFFFFF))
                            ) {
                                Icon(Icons.Filled.Sort, contentDescription = "Sort", tint = TextPrimary, modifier = Modifier.size(20.dp))
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(Color(0xF0101624))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sort by Name", color = TextPrimary) },
                                    onClick = { sortBy = SortBy.NAME; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sort by Date", color = TextPrimary) },
                                    onClick = { sortBy = SortBy.DATE; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sort by Size", color = TextPrimary) },
                                    onClick = { sortBy = SortBy.SIZE; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sort by Type", color = TextPrimary) },
                                    onClick = { sortBy = SortBy.TYPE; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (sortOrder == SortOrder.ASCENDING) "Order: Ascending" else "Order: Descending",
                                            color = NeonCyan
                                        )
                                    },
                                    onClick = {
                                        sortOrder = if (sortOrder == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Breadcrumbs Path Bar
            BreadcrumbsBar(
                currentPath = currentPath,
                rootPath = rootPath,
                onPathSegmentClick = onNavigateTo
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_explorer_search"),
                placeholder = { Text("Search files & folders...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = NeonCyan, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0x22131926),
                    unfocusedContainerColor = Color(0x18131926),
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = GlassBorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (All, Images, Videos, Audio, Documents, Archives)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipPill(
                    label = "All",
                    selected = selectedCategory == null,
                    onClick = { onSelectCategory(null) }
                )
                FileCategory.values().filter { it != FileCategory.FOLDER && it != FileCategory.OTHER }.forEach { cat ->
                    FilterChipPill(
                        label = cat.label,
                        selected = selectedCategory == cat,
                        onClick = {
                            onSelectCategory(if (selectedCategory == cat) null else cat)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // File items: Grid or List
            if (displayedFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = "Empty",
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching files" else "Folder is empty",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (viewMode == ViewMode.LIST) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(displayedFiles, key = { it.path }) { file ->
                        val isSelected = selectedFiles.contains(file)
                        FileListItem(
                            file = file,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (isSelectionMode) {
                                    if (isSelected) selectedFiles.remove(file) else selectedFiles.add(file)
                                } else {
                                    if (file.isDirectory) onNavigateTo(file.path) else onOpenFile(file)
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedFiles.add(file)
                                }
                            },
                            onMoreClick = { activeContextMenuFile = file }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(displayedFiles, key = { it.path }) { file ->
                        val isSelected = selectedFiles.contains(file)
                        FileGridItem(
                            file = file,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (isSelectionMode) {
                                    if (isSelected) selectedFiles.remove(file) else selectedFiles.add(file)
                                } else {
                                    if (file.isDirectory) onNavigateTo(file.path) else onOpenFile(file)
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedFiles.add(file)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Floating Selection Action Bar (when files are selected)
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                cornerRadius = 24.dp,
                backgroundColor = Color(0xF0101626)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionButtonMini(
                        icon = Icons.Filled.ContentCopy,
                        label = "Copy",
                        onClick = {
                            onCopyFiles(selectedFiles.toList())
                            selectedFiles.clear()
                        }
                    )
                    ActionButtonMini(
                        icon = Icons.Filled.ContentCut,
                        label = "Move",
                        onClick = {
                            onCutFiles(selectedFiles.toList())
                            selectedFiles.clear()
                        }
                    )
                    ActionButtonMini(
                        icon = Icons.Filled.Delete,
                        label = "Trash",
                        color = SunsetPink,
                        onClick = {
                            onDeleteToTrash(selectedFiles.toList())
                            selectedFiles.clear()
                        }
                    )
                    if (selectedFiles.size == 1 && !selectedFiles[0].isDirectory) {
                        ActionButtonMini(
                            icon = Icons.Filled.Share,
                            label = "Share",
                            onClick = {
                                onShareFile(selectedFiles[0])
                                selectedFiles.clear()
                            }
                        )
                    }
                }
            }
        }

        // Floating Clipboard Paste Bar (when items are copied/cut)
        AnimatedVisibility(
            visible = clipboard != null && !isSelectionMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 84.dp, start = 16.dp, end = 16.dp)
        ) {
            clipboard?.let { clip ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    cornerRadius = 22.dp,
                    backgroundColor = Color(0xF0131E30)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (clip.action == ClipboardAction.CUT) Icons.Filled.ContentCut else Icons.Filled.ContentCopy,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${clip.files.size} items ready to paste",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onClearClipboard,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.Clear, contentDescription = "Cancel", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                            Button(
                                onClick = onPaste,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Filled.ContentPaste, contentDescription = null, tint = VoidBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paste", color = VoidBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // File Context Menu (More Options)
        activeContextMenuFile?.let { file ->
            DropdownMenu(
                expanded = true,
                onDismissRequest = { activeContextMenuFile = null },
                modifier = Modifier.background(Color(0xF0101624))
            ) {
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, tint = NeonCyan) },
                    text = { Text("Rename", color = TextPrimary) },
                    onClick = {
                        activeContextMenuFile = null
                        fileToRename = file
                        newRenameName = file.name
                    }
                )
                if (!file.isDirectory) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = NeonCyan) },
                        text = { Text("Move to Safe Vault (AES-256)", color = TextPrimary) },
                        onClick = {
                            activeContextMenuFile = null
                            onAddToVault(file)
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null, tint = NeonCyan) },
                        text = { Text("Share", color = TextPrimary) },
                        onClick = {
                            activeContextMenuFile = null
                            onShareFile(file)
                        }
                    )
                }
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            if (file.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFB300)
                        )
                    },
                    text = { Text(if (file.isFavorite) "Remove from Favorites" else "Add to Favorites", color = TextPrimary) },
                    onClick = {
                        activeContextMenuFile = null
                        onToggleFavorite(file)
                    }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = SunsetPink) },
                    text = { Text("Move to Trash", color = SunsetPink) },
                    onClick = {
                        activeContextMenuFile = null
                        onDeleteToTrash(listOf(file))
                    }
                )
            }
        }

        // Rename Dialog
        fileToRename?.let { file ->
            AlertDialog(
                onDismissRequest = { fileToRename = null },
                containerColor = Color(0xF0101624),
                title = { Text("Rename", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newRenameName,
                        onValueChange = { newRenameName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GlassBorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newRenameName.isNotBlank()) {
                                onRename(file, newRenameName.trim())
                            }
                            fileToRename = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Rename", color = VoidBlack, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { fileToRename = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun BreadcrumbsBar(
    currentPath: String,
    rootPath: String,
    onPathSegmentClick: (String) -> Unit
) {
    val segments = remember(currentPath, rootPath) {
        val rel = if (currentPath.startsWith(rootPath)) {
            currentPath.removePrefix(rootPath).trimStart('/')
        } else {
            currentPath.trimStart('/')
        }
        val parts = if (rel.isEmpty()) emptyList() else rel.split('/')
        val list = mutableListOf("Internal Storage" to rootPath)
        var cumulative = rootPath
        for (part in parts) {
            cumulative = "$cumulative/$part"
            list.add(part to cumulative)
        }
        list
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x14FFFFFF))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        segments.forEachIndexed { index, (label, path) ->
            val isLast = index == segments.size - 1
            Text(
                text = label,
                color = if (isLast) NeonCyan else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onPathSegmentClick(path) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
            if (!isLast) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier
                        .size(10.dp)
                        .padding(horizontal = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) NeonCyan else Color(0x18FFFFFF))
            .border(1.dp, if (selected) NeonCyan else GlassBorderSubtle, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) VoidBlack else TextSecondary
        )
    }
}

@Composable
private fun FileListItem(
    file: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val (icon, gradient, accentColor) = getCategoryVisuals(file)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        cornerRadius = 20.dp,
        accentGlowColor = if (isSelected) NeonCyan else accentColor,
        borderBrush = if (isSelected) Brush.linearGradient(listOf(NeonCyan, ElectricPurple)) else null,
        backgroundColor = if (isSelected) Color(0x3500E5FF) else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) NeonCyan else Color(0x20FFFFFF))
                        .border(1.dp, if (isSelected) NeonCyan else Color(0x50FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = VoidBlack, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            LiquidSquircleBadge(
                icon = icon,
                gradient = gradient,
                contentDescription = file.name,
                size = 44.dp,
                iconSize = 22.dp,
                cornerRadius = 14.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = if (file.isDirectory) {
                    "${file.itemCount} items • ${file.relativeTime}"
                } else {
                    "${file.formattedSize} • ${file.relativeTime}"
                }
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FileGridItem(
    file: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val (icon, gradient, accentColor) = getCategoryVisuals(file)

    GlassCard(
        modifier = Modifier.height(128.dp),
        cornerRadius = 22.dp,
        accentGlowColor = if (isSelected) NeonCyan else accentColor,
        borderBrush = if (isSelected) Brush.linearGradient(listOf(NeonCyan, ElectricPurple)) else null,
        backgroundColor = if (isSelected) Color(0x3500E5FF) else null,
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
                contentDescription = file.name,
                size = 50.dp,
                iconSize = 24.dp,
                cornerRadius = 16.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = file.name,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (file.isDirectory) "${file.itemCount} items" else file.formattedSize,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ActionButtonMini(
    icon: ImageVector,
    label: String,
    color: Color = TextPrimary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

private fun getCategoryVisuals(file: FileItem): Triple<ImageVector, List<Color>, Color> {
    if (file.isDirectory) return Triple(Icons.Filled.Folder, FilesGradient, NeonCyan)
    return when (file.category) {
        FileCategory.IMAGES -> Triple(Icons.Filled.Image, ImagesGradient, SunsetPink)
        FileCategory.VIDEOS -> Triple(Icons.Filled.Videocam, VideosGradient, ElectricPurple)
        FileCategory.AUDIO -> Triple(Icons.Filled.MusicNote, AudioGradient, SunsetPink)
        FileCategory.DOCUMENTS -> Triple(Icons.Filled.Description, DocumentsGradient, ElectricBlue)
        FileCategory.ARCHIVES -> Triple(Icons.Filled.Archive, listOf(Color(0xFFFFB300), Color(0xFFFF8F00)), AmberGold)
        FileCategory.DOWNLOADS -> Triple(Icons.Filled.FileDownload, DownloadsGradient, AmberGold)
        FileCategory.APKS -> Triple(Icons.Filled.InstallMobile, ApksGradient, DeepEmerald)
        else -> Triple(Icons.Filled.Description, listOf(Color(0xFF64748B), Color(0xFF475569)), Color(0xFF94A3B8))
    }
}
