package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FileItem
import com.example.ui.FileManagerViewModel
import com.example.ui.components.AmbientMeshBackground
import com.example.ui.components.CreateActionModal
import com.example.ui.components.FloatingBottomDock
import com.example.ui.components.GlassCard
import com.example.ui.components.NavigationTab
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.explorer.FileExplorerScreen
import com.example.ui.optimizer.StorageOptimizerScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import com.example.ui.vault.SafeVaultScreen
import com.example.ui.viewers.AudioPlayerBar
import com.example.ui.viewers.ImageViewerDialog
import com.example.ui.viewers.TextViewerDialog
import com.example.ui.viewers.VideoPreviewDialog
import com.example.ui.wireless.WirelessShareScreen
import java.io.File

class MainActivity : ComponentActivity() {
    private val viewModel: FileManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: FileManagerViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State flows
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val showHiddenFiles by viewModel.showHiddenFiles.collectAsStateWithLifecycle()
    val showStorageBar by viewModel.showStorageBar.collectAsStateWithLifecycle()
    val ramStats by viewModel.ramStats.collectAsStateWithLifecycle()
    val isVaultPasswordEnabled by viewModel.isVaultPasswordEnabled.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isFtpRunning by viewModel.isFtpRunning.collectAsStateWithLifecycle()
    val ftpServerUrl by viewModel.ftpServerUrl.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()

    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
    val currentPath by viewModel.currentPath.collectAsStateWithLifecycle()
    val currentFiles by viewModel.currentFiles.collectAsStateWithLifecycle()
    val recentFiles by viewModel.recentFiles.collectAsStateWithLifecycle()
    val clipboard by viewModel.clipboard.collectAsStateWithLifecycle()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsStateWithLifecycle()
    val vaultItems by viewModel.vaultItems.collectAsStateWithLifecycle()
    val trashItems by viewModel.trashItems.collectAsStateWithLifecycle()
    val junkResult by viewModel.junkResult.collectAsStateWithLifecycle()
    val duplicateGroups by viewModel.duplicateGroups.collectAsStateWithLifecycle()
    val largeFiles by viewModel.largeFiles.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    // Viewers state
    val activeImage by viewModel.activeImage.collectAsStateWithLifecycle()
    val activeText by viewModel.activeText.collectAsStateWithLifecycle()
    val activeVideo by viewModel.activeVideo.collectAsStateWithLifecycle()
    val activeAudio by viewModel.activeAudio.collectAsStateWithLifecycle()
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()

    // Dialogs
    var showQuickActions by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var showNewDocDialog by remember { mutableStateOf(false) }
    var newDocName by remember { mutableStateOf("") }
    var newDocContent by remember { mutableStateOf("") }

    // User message snackbars
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Permission launcher for storage
    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshAll()
    }

    // Back button behavior for tabs
    BackHandler(enabled = currentTab != NavigationTab.DASHBOARD) {
        viewModel.selectTab(NavigationTab.DASHBOARD)
    }

    AmbientMeshBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Audio Player Bar above bottom dock
                    activeAudio?.let { audioFile ->
                        AudioPlayerBar(
                            file = audioFile,
                            isPlaying = isAudioPlaying,
                            onTogglePlay = { viewModel.toggleAudioPlayback() },
                            onClose = { viewModel.closeAudioPlayer() }
                        )
                    }

                    FloatingBottomDock(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        onAddClicked = { showQuickActions = true }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavigationTab.DASHBOARD -> {
                        DashboardScreen(
                            storageStats = storageStats,
                            ramStats = ramStats,
                            showStorageBar = showStorageBar,
                            recentFiles = recentFiles,
                            onCategoryClick = { cat ->
                                viewModel.openCategory(cat)
                            },
                            onOptimizeClick = { viewModel.selectTab(NavigationTab.TOOLS) },
                            onVaultClick = { viewModel.selectTab(NavigationTab.VAULT) },
                            onViewAllFilesClick = { viewModel.openAllFiles() },
                            onFileClick = { viewModel.openFile(it) },
                            onSearchClick = { viewModel.openAllFiles() },
                            onWirelessClick = { viewModel.selectTab(NavigationTab.WIRELESS) },
                            onSettingsClick = { viewModel.selectTab(NavigationTab.SETTINGS) },
                            onBoostRam = { viewModel.boostRam() }
                        )
                    }

                    NavigationTab.EXPLORER -> {
                        FileExplorerScreen(
                            currentPath = currentPath,
                            rootPath = viewModel.rootPath,
                            files = currentFiles,
                            clipboard = clipboard,
                            selectedCategory = selectedCategoryFilter,
                            onSelectCategory = { viewModel.setCategoryFilter(it) },
                            onNavigateTo = { viewModel.loadCurrentDirectory(it) },
                            onNavigateUp = { viewModel.navigateUp() },
                            onOpenFile = { viewModel.openFile(it) },
                            onDeleteToTrash = { viewModel.deleteFilesToTrash(it) },
                            onRename = { file, name -> viewModel.renameFile(file, name) },
                            onAddToVault = { viewModel.addToVault(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onCopyFiles = { viewModel.copyFiles(it) },
                            onCutFiles = { viewModel.cutFiles(it) },
                            onPaste = { viewModel.pasteClipboard() },
                            onClearClipboard = { viewModel.clearClipboard() },
                            onShareFile = { file ->
                                shareFileIntent(context, file)
                            },
                            showHiddenFiles = showHiddenFiles,
                            onToggleShowHiddenFiles = { viewModel.toggleHiddenFiles(!showHiddenFiles) },
                            onToggleHideFile = { viewModel.toggleHideFile(it) },
                            onToggleHideMultiple = { viewModel.toggleHideMultiple(it) }
                        )
                    }

                    NavigationTab.VAULT -> {
                        SafeVaultScreen(
                            vaultItems = vaultItems,
                            isUnlocked = isVaultUnlocked,
                            isPasswordProtectionEnabled = isVaultPasswordEnabled,
                            isBiometricEnabled = isBiometricEnabled,
                            onUnlockWithPin = { pin -> viewModel.unlockVault(pin) },
                            onBiometricUnlock = { viewModel.unlockWithBiometric() },
                            onVerifyRecoveryAnswer = { answer -> viewModel.verifyRecoveryAnswer(answer) },
                            onSetNewPin = { newPin -> viewModel.setNewVaultPin(newPin) },
                            onLockVault = { viewModel.lockVault() },
                            onRestoreItem = { viewModel.restoreFromVault(it) },
                            onAddFileToVaultClick = {
                                viewModel.openAllFiles()
                            },
                            onPreviewVaultItem = {
                                // Info / preview
                            },
                            onNavigateBack = { viewModel.selectTab(NavigationTab.DASHBOARD) }
                        )
                    }

                    NavigationTab.TOOLS -> {
                        StorageOptimizerScreen(
                            junkResult = junkResult,
                            duplicateGroups = duplicateGroups,
                            largeFiles = largeFiles,
                            trashItems = trashItems,
                            isScanning = isScanning,
                            onStartScan = { viewModel.scanOptimizer(auto = false) },
                            onCleanJunk = { viewModel.cleanJunk() },
                            onDeleteDuplicate = { viewModel.deleteDuplicateFile(it) },
                            onRestoreTrash = { viewModel.restoreTrashItem(it) },
                            onEmptyTrash = { viewModel.emptyTrash() },
                            onNavigateBack = { viewModel.selectTab(NavigationTab.DASHBOARD) }
                        )
                    }

                    NavigationTab.WIRELESS -> {
                        WirelessShareScreen(
                            isFtpRunning = isFtpRunning,
                            serverUrl = ftpServerUrl,
                            onToggleFtpServer = { viewModel.toggleFtpServer() },
                            onNavigateBack = { viewModel.selectTab(NavigationTab.DASHBOARD) }
                        )
                    }

                    NavigationTab.SETTINGS -> {
                        SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            showHiddenFiles = showHiddenFiles,
                            showStorageBar = showStorageBar,
                            ramStats = ramStats,
                            isVaultPasswordEnabled = isVaultPasswordEnabled,
                            isBiometricEnabled = isBiometricEnabled,
                            onToggleTheme = { viewModel.toggleTheme(it) },
                            onToggleHiddenFiles = { viewModel.toggleHiddenFiles(it) },
                            onToggleStorageBar = { viewModel.toggleStorageBar(it) },
                            onToggleVaultPassword = { viewModel.setVaultPasswordEnabled(it) },
                            onToggleBiometric = { viewModel.setBiometricEnabled(it) },
                            onChangeVaultPin = { viewModel.setNewVaultPin(it) },
                            onBoostRam = { viewModel.boostRam() },
                            onClearAppCache = { viewModel.clearAppCache() },
                            onNavigateBack = { viewModel.selectTab(NavigationTab.DASHBOARD) }
                        )
                    }
                }
            }
        }

        // Quick Actions Bottom Sheet (+)
        if (showQuickActions) {
            CreateActionModal(
                onDismiss = { showQuickActions = false },
                onCreateFolder = { showNewFolderDialog = true },
                onCreateTextFile = { showNewDocDialog = true },
                onOpenVault = { viewModel.selectTab(NavigationTab.VAULT) },
                onStartCleaner = { viewModel.selectTab(NavigationTab.TOOLS) },
                onStartShare = { viewModel.selectTab(NavigationTab.WIRELESS) }
            )
        }

        // New Folder Dialog
        if (showNewFolderDialog) {
            AlertDialog(
                onDismissRequest = { showNewFolderDialog = false },
                containerColor = Color(0xF0101624),
                title = { Text("Create New Folder", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        placeholder = { Text("Folder Name", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GlassBorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.testTag("input_folder_name")
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                viewModel.createFolder(newFolderName.trim())
                                newFolderName = ""
                                showNewFolderDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("btn_confirm_create_folder")
                    ) {
                        Text("Create", color = VoidBlack, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewFolderDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // New Document Dialog
        if (showNewDocDialog) {
            AlertDialog(
                onDismissRequest = { showNewDocDialog = false },
                containerColor = Color(0xF0101624),
                title = { Text("Create New Document", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newDocName,
                            onValueChange = { newDocName = it },
                            placeholder = { Text("File Name (e.g. notes.txt)", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_doc_name")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newDocContent,
                            onValueChange = { newDocContent = it },
                            placeholder = { Text("Initial content...", color = TextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("input_doc_content"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newDocName.isNotBlank()) {
                                viewModel.createTextDocument(newDocName.trim(), newDocContent)
                                newDocName = ""
                                newDocContent = ""
                                showNewDocDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("btn_confirm_create_doc")
                    ) {
                        Text("Create", color = VoidBlack, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewDocDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Viewers
        activeImage?.let { file ->
            ImageViewerDialog(
                file = file,
                onDismiss = { viewModel.closeImageViewer() },
                onShare = { shareFileIntent(context, file) }
            )
        }

        activeText?.let { file ->
            TextViewerDialog(
                file = file,
                onDismiss = { viewModel.closeTextViewer() },
                onSaveContent = { viewModel.saveDocumentContent(it) }
            )
        }

        activeVideo?.let { file ->
            VideoPreviewDialog(
                file = file,
                onDismiss = { viewModel.closeVideoViewer() }
            )
        }
    }
}

private fun shareFileIntent(context: android.content.Context, fileItem: FileItem) {
    try {
        val file = File(fileItem.path)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            putExtra(Intent.EXTRA_SUBJECT, fileItem.name)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val chooser = Intent.createChooser(sendIntent, "Share ${fileItem.name}")
        chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
