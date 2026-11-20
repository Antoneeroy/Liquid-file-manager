package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdConsentManager
import com.example.data.model.FileItem
import com.example.ui.FileManagerViewModel
import com.example.ui.components.AmbientMeshBackground
import com.example.ui.components.CreateActionModal
import com.example.ui.components.FloatingBottomDock
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
import com.example.ui.viewers.PdfViewerDialog
import com.example.ui.viewers.TextViewerDialog
import com.example.ui.viewers.VideoPreviewDialog
import com.example.ui.wireless.WirelessShareScreen
import java.io.File

class MainActivity : FragmentActivity() {
    private val viewModel: FileManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdConsentManager.gatherConsentAndInitialize(this)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppScreen(
                    viewModel = viewModel,
                    onRequestBiometricPrompt = { onSuccess, onError ->
                        triggerBiometricPrompt(onSuccess, onError)
                    },
                    onOpenPrivacyOptions = {
                        AdConsentManager.showPrivacyOptionsForm(this) { err ->
                            if (!err.isNullOrBlank()) {
                                viewModel.refreshAll()
                            }
                        }
                    },
                    onOptimizerSafeExit = {
                        AdConsentManager.maybeShowInterstitialOnSafeBreak(this) {
                            viewModel.selectTab(NavigationTab.DASHBOARD)
                        }
                    }
                )
            }
        }
    }

    private fun triggerBiometricPrompt(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val biometricManager = BiometricManager.from(this)
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            if (biometricManager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
                onError("Biometrics unavailable or not enrolled on this device.")
                return
            }
            val executor = ContextCompat.getMainExecutor(this)
            val prompt = BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        ) {
                            onError(errString.toString())
                        }
                    }
                }
            )
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Safe Vault")
                .setSubtitle("Authenticate using device biometrics")
                .setNegativeButtonText("Use PIN")
                .build()
            prompt.authenticate(info)
        } catch (e: Exception) {
            onError("Biometric prompt failed on this device.")
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: FileManagerViewModel,
    onRequestBiometricPrompt: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onOpenPrivacyOptions: () -> Unit,
    onOptimizerSafeExit: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val showHiddenFiles by viewModel.showHiddenFiles.collectAsStateWithLifecycle()
    val showStorageBar by viewModel.showStorageBar.collectAsStateWithLifecycle()
    val ramStats by viewModel.ramStats.collectAsStateWithLifecycle()
    val isPinConfigured by viewModel.isPinConfigured.collectAsStateWithLifecycle()
    val isVaultPasswordEnabled by viewModel.isVaultPasswordEnabled.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val vaultFailedAttempts by viewModel.vaultFailedAttempts.collectAsStateWithLifecycle()
    val vaultLockoutSeconds by viewModel.vaultLockoutSeconds.collectAsStateWithLifecycle()
    val hasRecoveryConfigured by viewModel.hasRecoveryConfigured.collectAsStateWithLifecycle()
    val recoveryQuestion by viewModel.recoveryQuestion.collectAsStateWithLifecycle()
    val isFtpRunning by viewModel.isFtpRunning.collectAsStateWithLifecycle()
    val ftpServerUrl by viewModel.ftpServerUrl.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()

    val canRequestAds by AdConsentManager.canRequestAds.collectAsStateWithLifecycle()
    val isPrivacyOptionsRequired by AdConsentManager.isPrivacyOptionsRequired.collectAsStateWithLifecycle()

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
    val imageGallery by viewModel.imageGallery.collectAsStateWithLifecycle()
    val activeText by viewModel.activeText.collectAsStateWithLifecycle()
    val activePdf by viewModel.activePdf.collectAsStateWithLifecycle()
    val activeVideo by viewModel.activeVideo.collectAsStateWithLifecycle()

    // Real Audio Player state
    val activeAudio by viewModel.activeAudio.collectAsStateWithLifecycle()
    val audioTitle by viewModel.audioTitle.collectAsStateWithLifecycle()
    val audioArtist by viewModel.audioArtist.collectAsStateWithLifecycle()
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()
    val audioPositionMs by viewModel.audioPositionMs.collectAsStateWithLifecycle()
    val audioDurationMs by viewModel.audioDurationMs.collectAsStateWithLifecycle()
    val hasNextAudio by viewModel.hasNextAudio.collectAsStateWithLifecycle()
    val hasPreviousAudio by viewModel.hasPreviousAudio.collectAsStateWithLifecycle()
    val audioErrorMessage by viewModel.audioErrorMessage.collectAsStateWithLifecycle()

    // Dialogs
    var showQuickActions by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var showNewDocDialog by remember { mutableStateOf(false) }
    var newDocName by remember { mutableStateOf("") }
    var newDocContent by remember { mutableStateOf("") }
    var localSnackMsg by remember { mutableStateOf<String?>(null) }

    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }
    LaunchedEffect(localSnackMsg) {
        localSnackMsg?.let {
            snackbarHostState.showSnackbar(it)
            localSnackMsg = null
        }
    }

    // All-Files Access Launcher (Android 11+ / R+)
    val allFilesAccessLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshAll()
    }

    // Scoped Runtime Storage/Media Permissions Launcher (Android 8 - 16)
    val runtimePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshAll()
    }

    // Storage Access Framework (SAF) Open Document Launcher
    val safPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Not all SAF providers allow persistable grants; immediate read still works
            }
            viewModel.openContentUri(it)
        }
    }

    val requestStoragePermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                allFilesAccessLauncher.launch(intent)
            } catch (_: Exception) {
                try {
                    allFilesAccessLauncher.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                } catch (_: Exception) {
                    localSnackMsg = "Open system settings to grant file access or use Pick File (SAF)."
                }
            }
        } else {
            runtimePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    // Request scoped media permissions once on Android 13+ or legacy storage on 8-10 if needed (non-blocking)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runtimePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            )
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            runtimePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    BackHandler(enabled = currentTab != NavigationTab.DASHBOARD) {
        viewModel.selectTab(NavigationTab.DASHBOARD)
    }

    AmbientMeshBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.navigationBars,
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    activeAudio?.let { audioFile ->
                        AudioPlayerBar(
                            file = audioFile,
                            title = audioTitle,
                            artist = audioArtist,
                            isPlaying = isAudioPlaying,
                            positionMs = audioPositionMs,
                            durationMs = audioDurationMs,
                            hasPrevious = hasPreviousAudio,
                            hasNext = hasNextAudio,
                            errorMessage = audioErrorMessage,
                            onTogglePlay = { viewModel.toggleAudioPlayback() },
                            onSeekTo = { viewModel.seekAudioTo(it) },
                            onPrevious = { viewModel.playPreviousAudio() },
                            onNext = { viewModel.playNextAudio() },
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
                            vaultCount = vaultItems.size,
                            canRequestAds = canRequestAds,
                            onCategoryClick = { viewModel.openCategory(it) },
                            onOptimizeClick = { viewModel.selectTab(NavigationTab.TOOLS) },
                            onVaultClick = { viewModel.selectTab(NavigationTab.VAULT) },
                            onViewAllFilesClick = { viewModel.openAllFiles() },
                            onOpenSdCardClick = { viewModel.openVolumeRoot(it) },
                            onOpenSafPickerClick = { safPickerLauncher.launch(arrayOf("*/*")) },
                            onGrantStoragePermissionClick = requestStoragePermission,
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
                            hasAllFilesPermission = storageStats.hasAllFilesPermission,
                            canRequestAds = canRequestAds,
                            selectedCategory = selectedCategoryFilter,
                            onSelectCategory = { viewModel.setCategoryFilter(it) },
                            onSearchQueryChange = { viewModel.searchInExplorer(it) },
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
                                val shared = shareFileIntent(context, file)
                                if (!shared) {
                                    localSnackMsg = "Unable to share this file securely."
                                }
                            },
                            onRequestStoragePermission = requestStoragePermission,
                            onOpenSafPicker = { safPickerLauncher.launch(arrayOf("*/*")) }
                        )
                    }

                    NavigationTab.VAULT -> {
                        SafeVaultScreen(
                            vaultItems = vaultItems,
                            isUnlocked = isVaultUnlocked,
                            isPinConfigured = isPinConfigured,
                            isPasswordProtectionEnabled = isVaultPasswordEnabled,
                            isBiometricEnabled = isBiometricEnabled,
                            failedAttempts = vaultFailedAttempts,
                            lockoutRemainingSeconds = vaultLockoutSeconds,
                            hasRecoveryConfigured = hasRecoveryConfigured,
                            recoveryQuestion = recoveryQuestion,
                            onSetupVaultPin = { pin, q, a -> viewModel.setupVaultCredentials(pin, q, a) },
                            onUnlockWithPin = { pin -> viewModel.unlockVault(pin) },
                            onRequestBiometricUnlock = {
                                onRequestBiometricPrompt(
                                    { viewModel.unlockWithBiometric() },
                                    { err -> localSnackMsg = err }
                                )
                            },
                            onVerifyRecoveryAnswer = { answer -> viewModel.verifyRecoveryAnswer(answer) },
                            onSetNewPin = { newPin -> viewModel.setNewVaultPin(newPin) },
                            onLockVault = { viewModel.lockVault() },
                            onRestoreItem = { viewModel.restoreFromVault(it) },
                            onAddFileToVaultClick = { viewModel.openAllFiles() },
                            onPreviewVaultItem = { viewModel.previewVaultItem(it) },
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
                            canRequestAds = canRequestAds,
                            onStartScan = { viewModel.scanOptimizer(auto = false) },
                            onCleanJunk = { viewModel.cleanJunk() },
                            onDeleteDuplicate = { viewModel.deleteDuplicateFile(it) },
                            onRestoreTrash = { viewModel.restoreTrashItem(it) },
                            onEmptyTrash = { viewModel.emptyTrash() },
                            onNavigateBack = onOptimizerSafeExit
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
                            isPinConfigured = isPinConfigured,
                            isVaultPasswordEnabled = isVaultPasswordEnabled,
                            isBiometricEnabled = isBiometricEnabled,
                            isPrivacyOptionsRequired = isPrivacyOptionsRequired,
                            onToggleTheme = { viewModel.toggleTheme(it) },
                            onToggleHiddenFiles = { viewModel.toggleHiddenFiles(it) },
                            onToggleStorageBar = { viewModel.toggleStorageBar(it) },
                            onToggleVaultPassword = { viewModel.setVaultPasswordEnabled(it) },
                            onToggleBiometric = { viewModel.setBiometricEnabled(it) },
                            onChangeVaultPin = { viewModel.setNewVaultPin(it) },
                            onUpdateRecoverySecurity = { q, a -> viewModel.updateRecoverySecurity(q, a) },
                            onOpenAdPrivacyOptions = onOpenPrivacyOptions,
                            onBoostRam = { viewModel.boostRam() },
                            onClearAppCache = { viewModel.clearAppCache() },
                            onNavigateBack = { viewModel.selectTab(NavigationTab.DASHBOARD) }
                        )
                    }
                }
            }
        }

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

        // Viewers (Image, PDF, Text, Video)
        activeImage?.let { file ->
            ImageViewerDialog(
                file = file,
                imageList = imageGallery,
                onDismiss = { viewModel.closeImageViewer() },
                onShare = { target ->
                    shareFileIntent(context, target)
                }
            )
        }

        activePdf?.let { file ->
            PdfViewerDialog(
                file = file,
                onDismiss = { viewModel.closePdfViewer() }
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

private fun shareFileIntent(context: Context, fileItem: FileItem): Boolean {
    return try {
        val shareUri: Uri = if (fileItem.isContentUri) {
            Uri.parse(fileItem.path)
        } else {
            val file = File(fileItem.path)
            if (!file.exists()) return false
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = fileItem.mimeType ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            putExtra(Intent.EXTRA_SUBJECT, fileItem.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share ${fileItem.name}").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
        true
    } catch (_: Exception) {
        false
    }
}
