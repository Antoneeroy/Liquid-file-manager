package com.example.ui

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.crypto.VaultSecurityManager
import com.example.data.db.TrashEntity
import com.example.data.db.VaultEntity
import com.example.data.model.ClipboardAction
import com.example.data.model.ClipboardItem
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.data.model.RamStats
import com.example.data.model.StorageStats
import com.example.data.repository.FileManagerRepository
import com.example.data.server.LocalWebServer
import com.example.ui.components.NavigationTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FileManagerRepository(application)
    private val securityManager = VaultSecurityManager(application)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(NavigationTab.DASHBOARD)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    // Settings state
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _showHiddenFiles = MutableStateFlow(false)
    val showHiddenFiles: StateFlow<Boolean> = _showHiddenFiles.asStateFlow()

    private val _showStorageBar = MutableStateFlow(true)
    val showStorageBar: StateFlow<Boolean> = _showStorageBar.asStateFlow()

    // RAM stats
    private val _ramStats = MutableStateFlow(RamStats(isAvailable = false))
    val ramStats: StateFlow<RamStats> = _ramStats.asStateFlow()

    // Safe Vault persistent security state
    private val _isPinConfigured = MutableStateFlow(securityManager.isPinConfigured())
    val isPinConfigured: StateFlow<Boolean> = _isPinConfigured.asStateFlow()

    private val _isVaultPasswordEnabled = MutableStateFlow(securityManager.isPasswordProtectionEnabled())
    val isVaultPasswordEnabled: StateFlow<Boolean> = _isVaultPasswordEnabled.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(securityManager.isBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _vaultFailedAttempts = MutableStateFlow(securityManager.getFailedAttempts())
    val vaultFailedAttempts: StateFlow<Int> = _vaultFailedAttempts.asStateFlow()

    private val _vaultLockoutSeconds = MutableStateFlow(securityManager.getLockoutRemainingSeconds())
    val vaultLockoutSeconds: StateFlow<Long> = _vaultLockoutSeconds.asStateFlow()

    private val _hasRecoveryConfigured = MutableStateFlow(securityManager.hasRecoveryConfigured())
    val hasRecoveryConfigured: StateFlow<Boolean> = _hasRecoveryConfigured.asStateFlow()

    private val _recoveryQuestion = MutableStateFlow(securityManager.getRecoveryQuestion())
    val recoveryQuestion: StateFlow<String> = _recoveryQuestion.asStateFlow()

    private var sessionUnlockedPin: String? = null
    private var lockoutTickerJob: Job? = null

    // Category filter in explorer
    private val _selectedCategoryFilter = MutableStateFlow<FileCategory?>(null)
    val selectedCategoryFilter: StateFlow<FileCategory?> = _selectedCategoryFilter.asStateFlow()

    // Hardened Wi-Fi HTTP File Server
    private val localWebServer = LocalWebServer(application) { repository.rootPath }
    val isFtpRunning: StateFlow<Boolean> = localWebServer.isRunning
    val ftpServerUrl: StateFlow<String> = localWebServer.serverUrl

    // Real storage stats (unavailable until inspected on IO)
    private val _storageStats = MutableStateFlow(StorageStats(isAvailable = false))
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    // Root and Current Directory Path
    val rootPath: String get() = repository.rootPath
    private val _currentPath = MutableStateFlow(repository.rootPath)
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    // Current folder files
    private val _currentFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val currentFiles: StateFlow<List<FileItem>> = _currentFiles.asStateFlow()

    // Recent activity files
    private val _recentFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val recentFiles: StateFlow<List<FileItem>> = _recentFiles.asStateFlow()

    // Clipboard for copy / move
    private val _clipboard = MutableStateFlow<ClipboardItem?>(null)
    val clipboard: StateFlow<ClipboardItem?> = _clipboard.asStateFlow()

    // Safe Vault state
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    val vaultItems: StateFlow<List<VaultEntity>> = repository.vaultDao
        .getAllVaultItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trash items
    val trashItems: StateFlow<List<TrashEntity>> = repository.trashDao
        .getAllTrash()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Optimizer state (lazy scan instead of blocking cold boot)
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _junkResult = MutableStateFlow<FileManagerRepository.JunkScanResult?>(null)
    val junkResult: StateFlow<FileManagerRepository.JunkScanResult?> = _junkResult.asStateFlow()

    private val _duplicateGroups = MutableStateFlow<List<FileManagerRepository.DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<FileManagerRepository.DuplicateGroup>> = _duplicateGroups.asStateFlow()

    private val _largeFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val largeFiles: StateFlow<List<FileItem>> = _largeFiles.asStateFlow()

    // Viewers
    private val _activeImage = MutableStateFlow<FileItem?>(null)
    val activeImage: StateFlow<FileItem?> = _activeImage.asStateFlow()

    private val _imageGallery = MutableStateFlow<List<FileItem>>(emptyList())
    val imageGallery: StateFlow<List<FileItem>> = _imageGallery.asStateFlow()

    private val _activeText = MutableStateFlow<FileItem?>(null)
    val activeText: StateFlow<FileItem?> = _activeText.asStateFlow()

    private val _activePdf = MutableStateFlow<FileItem?>(null)
    val activePdf: StateFlow<FileItem?> = _activePdf.asStateFlow()

    private val _activeVideo = MutableStateFlow<FileItem?>(null)
    val activeVideo: StateFlow<FileItem?> = _activeVideo.asStateFlow()

    // Real Media3 Audio Player state
    private var audioPlayer: ExoPlayer? = null
    private var audioProgressJob: Job? = null
    private var audioQueue: List<FileItem> = emptyList()
    private var audioQueueIndex: Int = -1

    private val _activeAudio = MutableStateFlow<FileItem?>(null)
    val activeAudio: StateFlow<FileItem?> = _activeAudio.asStateFlow()

    private val _audioTitle = MutableStateFlow<String?>(null)
    val audioTitle: StateFlow<String?> = _audioTitle.asStateFlow()

    private val _audioArtist = MutableStateFlow<String?>(null)
    val audioArtist: StateFlow<String?> = _audioArtist.asStateFlow()

    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()

    private val _audioPositionMs = MutableStateFlow(0L)
    val audioPositionMs: StateFlow<Long> = _audioPositionMs.asStateFlow()

    private val _audioDurationMs = MutableStateFlow(0L)
    val audioDurationMs: StateFlow<Long> = _audioDurationMs.asStateFlow()

    private val _hasNextAudio = MutableStateFlow(false)
    val hasNextAudio: StateFlow<Boolean> = _hasNextAudio.asStateFlow()

    private val _hasPreviousAudio = MutableStateFlow(false)
    val hasPreviousAudio: StateFlow<Boolean> = _hasPreviousAudio.asStateFlow()

    private val _audioErrorMessage = MutableStateFlow<String?>(null)
    val audioErrorMessage: StateFlow<String?> = _audioErrorMessage.asStateFlow()

    // User message (Snackbar)
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        refreshAll()
        if (_vaultLockoutSeconds.value > 0L) {
            startLockoutCountdown()
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
        if (tab == NavigationTab.TOOLS && _junkResult.value == null && !_isScanning.value) {
            scanOptimizer(auto = true)
        }
    }

    // Settings actions
    fun toggleTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        showMessage(if (isDark) "Switched to Luxury Night Theme" else "Switched to Frosted Day Theme")
    }

    fun toggleHiddenFiles(show: Boolean) {
        _showHiddenFiles.value = show
        showMessage(if (show) "Hidden files visible (.nomedia, .cache)" else "Hidden files concealed")
        loadCurrentDirectory(_currentPath.value)
    }

    fun toggleStorageBar(show: Boolean) {
        _showStorageBar.value = show
        showMessage(if (show) "Dashboard storage bar shown" else "Dashboard storage bar hidden")
    }

    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
        showMessage(if (enabled) "Biometric unlock enabled" else "Biometric unlock disabled")
    }

    fun unlockWithBiometric() {
        securityManager.resetFailedAttempts()
        _vaultFailedAttempts.value = 0
        _vaultLockoutSeconds.value = 0L
        _isVaultUnlocked.value = true
        showMessage("Biometric verified. Safe Vault unlocked.")
    }

    fun toggleHideFile(file: FileItem) {
        viewModelScope.launch {
            val wasHidden = file.name.startsWith(".")
            val success = repository.toggleHideFile(file)
            if (success) {
                showMessage(if (wasHidden) "Unhid '${file.name.removePrefix(".")}'" else "Hid as '.${file.name}'")
                loadCurrentDirectory(_currentPath.value)
            } else {
                showMessage("Failed to update file visibility")
            }
        }
    }

    fun toggleHideMultiple(files: List<FileItem>) {
        viewModelScope.launch {
            var count = 0
            for (f in files) {
                if (repository.toggleHideFile(f)) count++
            }
            showMessage("$count item(s) updated")
            loadCurrentDirectory(_currentPath.value)
        }
    }

    fun boostRam() {
        viewModelScope.launch {
            val before = withContext(Dispatchers.IO) { repository.getRamStats() }
            withContext(Dispatchers.Default) { System.gc() }
            delay(250)
            val after = withContext(Dispatchers.IO) { repository.getRamStats() }
            _ramStats.value = after
            if (!after.isAvailable) {
                showMessage("Memory stats unavailable on this device")
                return@launch
            }
            val freed = (after.availBytes - before.availBytes).coerceAtLeast(0L)
            if (freed > 0) {
                showMessage("Memory trimmed! Freed ${FileItem.formatFileSize(freed)}")
            } else {
                showMessage("Memory status refreshed (${after.availFormatted} free)")
            }
        }
    }

    fun clearAppCache() {
        viewModelScope.launch {
            val freed = repository.clearAppCache()
            showMessage("Cache cleared: reclaimed ${FileItem.formatFileSize(freed)}")
            loadStorageStats()
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            // Fast path first: RAM, root listing, and basic storage stats
            _ramStats.value = withContext(Dispatchers.IO) { repository.getRamStats() }
            loadStorageStats()
            loadCurrentDirectory(_currentPath.value)
            loadRecentFiles()
        }
    }

    private suspend fun loadStorageStats() {
        _storageStats.value = repository.getStorageStats(includeCategoryBreakdown = true)
    }

    fun loadCurrentDirectory(path: String) {
        _currentPath.value = path
        viewModelScope.launch {
            _currentFiles.value = repository.getFilesInDirectory(path, showHidden = _showHiddenFiles.value)
        }
    }

    fun openVolumeRoot(path: String) {
        _selectedCategoryFilter.value = null
        _currentTab.value = NavigationTab.EXPLORER
        loadCurrentDirectory(path)
    }

    fun navigateUp(): Boolean {
        val currentPathVal = _currentPath.value
        val sdRoot = repository.getAccessibleSdCardRoot()
        if (currentPathVal == rootPath || (sdRoot != null && currentPathVal == sdRoot)) {
            return false
        }
        val current = File(currentPathVal)
        val parent = current.parentFile
        if (parent != null && parent.canRead()) {
            loadCurrentDirectory(parent.absolutePath)
            return true
        }
        return false
    }

    private suspend fun loadRecentFiles() {
        _recentFiles.value = repository.getRecentFiles(12)
    }

    fun openCategory(category: FileCategory) {
        viewModelScope.launch {
            _selectedCategoryFilter.value = category
            _currentTab.value = NavigationTab.EXPLORER
            val catFiles = repository.getFilesByCategory(category)
            _currentFiles.value = catFiles
            showMessage("${category.label}: ${catFiles.size} item(s)")
        }
    }

    fun openAllFiles() {
        _selectedCategoryFilter.value = null
        _currentTab.value = NavigationTab.EXPLORER
        loadCurrentDirectory(rootPath)
    }

    fun setCategoryFilter(category: FileCategory?) {
        _selectedCategoryFilter.value = category
        viewModelScope.launch {
            if (category == null) {
                _currentFiles.value = repository.getFilesInDirectory(_currentPath.value, showHidden = _showHiddenFiles.value)
            } else {
                _currentFiles.value = repository.getFilesByCategory(category)
            }
        }
    }

    private var searchJob: Job? = null

    fun searchInExplorer(query: String) {
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            val activeCat = _selectedCategoryFilter.value
            viewModelScope.launch {
                _currentFiles.value = if (activeCat == null) {
                    repository.getFilesInDirectory(_currentPath.value, showHidden = _showHiddenFiles.value)
                } else {
                    repository.getFilesByCategory(activeCat)
                }
            }
            return
        }
        if (trimmed.length < 2) return
        searchJob = viewModelScope.launch {
            delay(180L)
            _currentFiles.value = repository.searchFiles(trimmed, showHidden = _showHiddenFiles.value)
        }
    }

    fun openContentUri(uri: Uri) {
        viewModelScope.launch {
            val resolved = repository.resolveContentUriToFileItem(uri)
            if (resolved == null) {
                showMessage("Unable to open selected document")
            } else {
                openFile(resolved)
            }
        }
    }

    fun openFile(file: FileItem) {
        when {
            file.isPdf -> _activePdf.value = file
            file.category == FileCategory.IMAGES -> {
                val gallerySource = (_currentFiles.value + _recentFiles.value)
                    .filter { !it.isDirectory && it.category == FileCategory.IMAGES }
                    .distinctBy { it.path }
                _imageGallery.value = if (gallerySource.any { it.path == file.path }) gallerySource else listOf(file) + gallerySource
                _activeImage.value = file
            }
            file.category == FileCategory.AUDIO -> startAudioPlayback(file)
            file.category == FileCategory.VIDEOS -> _activeVideo.value = file
            else -> _activeText.value = file
        }
    }

    fun closeImageViewer() {
        _activeImage.value = null
        repository.clearVaultPreviewCache()
    }

    fun closeTextViewer() {
        _activeText.value = null
        repository.clearVaultPreviewCache()
    }

    fun closePdfViewer() {
        _activePdf.value = null
        repository.clearVaultPreviewCache()
    }

    fun closeVideoViewer() {
        _activeVideo.value = null
        repository.clearVaultPreviewCache()
    }

    // --- Real Media3 ExoPlayer Audio Engine ---

    private fun getOrCreateAudioPlayer(): ExoPlayer {
        val existing = audioPlayer
        if (existing != null) return existing

        val attrs = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(getApplication())
            .setAudioAttributes(attrs, true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _audioDurationMs.value = player.duration.coerceAtLeast(0L)
                        _audioErrorMessage.value = null
                    }
                    Player.STATE_ENDED -> {
                        if (_hasNextAudio.value) {
                            playNextAudio()
                        } else {
                            _isAudioPlaying.value = false
                            _audioPositionMs.value = 0L
                            player.seekTo(0L)
                            player.pause()
                        }
                    }
                    else -> Unit
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                _isAudioPlaying.value = playing
                if (playing) startAudioProgressTicker() else audioProgressJob?.cancel()
            }

            override fun onPlayerError(error: PlaybackException) {
                _isAudioPlaying.value = false
                audioProgressJob?.cancel()
                _audioErrorMessage.value = "Unsupported or unreadable audio (${error.errorCodeName})"
            }
        })

        audioPlayer = player
        return player
    }

    private fun startAudioPlayback(file: FileItem) {
        val queueCandidates = (_currentFiles.value + _recentFiles.value)
            .filter { !it.isDirectory && it.category == FileCategory.AUDIO }
            .distinctBy { it.path }

        audioQueue = if (queueCandidates.any { it.path == file.path }) {
            queueCandidates
        } else {
            listOf(file) + queueCandidates
        }
        audioQueueIndex = audioQueue.indexOfFirst { it.path == file.path }.coerceAtLeast(0)
        loadAndPlayAudioAtIndex(audioQueueIndex)
    }

    private fun loadAndPlayAudioAtIndex(index: Int) {
        val item = audioQueue.getOrNull(index) ?: return
        audioQueueIndex = index
        _hasPreviousAudio.value = index > 0
        _hasNextAudio.value = index < audioQueue.lastIndex

        _activeAudio.value = item
        _audioTitle.value = item.name
        _audioArtist.value = null
        _audioPositionMs.value = 0L
        _audioDurationMs.value = 0L
        _audioErrorMessage.value = null

        val uri = if (item.path.startsWith("content://") || item.path.startsWith("file://")) {
            Uri.parse(item.path)
        } else {
            Uri.fromFile(File(item.path))
        }

        // Load ID3 metadata off the main thread
        viewModelScope.launch {
            val metadata = withContext(Dispatchers.IO) {
                extractAudioMetadata(uri, item)
            }
            if (_activeAudio.value?.path == item.path) {
                _audioTitle.value = metadata.first ?: item.name
                _audioArtist.value = metadata.second
            }
        }

        try {
            val player = getOrCreateAudioPlayer()
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()
            player.play()
        } catch (e: Exception) {
            _isAudioPlaying.value = false
            _audioErrorMessage.value = "Cannot play audio file"
        }
    }

    private fun extractAudioMetadata(uri: Uri, item: FileItem): Pair<String?, String?> {
        val retriever = MediaMetadataRetriever()
        return try {
            if (item.isContentUri) {
                retriever.setDataSource(getApplication(), uri)
            } else {
                retriever.setDataSource(item.path)
            }
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            title to artist
        } catch (_: Exception) {
            null to null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun startAudioProgressTicker() {
        audioProgressJob?.cancel()
        audioProgressJob = viewModelScope.launch {
            while (isActive) {
                val player = audioPlayer
                if (player != null) {
                    _audioPositionMs.value = player.currentPosition.coerceAtLeast(0L)
                    val dur = player.duration.coerceAtLeast(0L)
                    if (dur > 0L) _audioDurationMs.value = dur
                }
                delay(250L)
            }
        }
    }

    fun toggleAudioPlayback() {
        val player = audioPlayer ?: return
        if (player.playbackState == Player.STATE_ENDED) {
            player.seekTo(0L)
            player.play()
        } else if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekAudioTo(positionMs: Long) {
        val player = audioPlayer ?: return
        val clamped = positionMs.coerceIn(0L, _audioDurationMs.value.coerceAtLeast(0L))
        player.seekTo(clamped)
        _audioPositionMs.value = clamped
    }

    fun playNextAudio() {
        if (audioQueueIndex + 1 in audioQueue.indices) {
            loadAndPlayAudioAtIndex(audioQueueIndex + 1)
        }
    }

    fun playPreviousAudio() {
        if (audioQueueIndex - 1 in audioQueue.indices) {
            loadAndPlayAudioAtIndex(audioQueueIndex - 1)
        }
    }

    fun closeAudioPlayer() {
        audioProgressJob?.cancel()
        audioPlayer?.stop()
        audioPlayer?.clearMediaItems()
        _activeAudio.value = null
        _audioTitle.value = null
        _audioArtist.value = null
        _isAudioPlaying.value = false
        _audioPositionMs.value = 0L
        _audioDurationMs.value = 0L
        _audioErrorMessage.value = null
        repository.clearVaultPreviewCache()
    }

    // CRUD operations
    fun createFolder(name: String) {
        viewModelScope.launch {
            val success = repository.createFolder(_currentPath.value, name)
            if (success) {
                showMessage("Folder '$name' created")
                loadCurrentDirectory(_currentPath.value)
                loadStorageStats()
            } else {
                showMessage("Failed to create folder")
            }
        }
    }

    fun createTextDocument(name: String, content: String) {
        viewModelScope.launch {
            val fullName = if (name.contains(".")) name else "$name.txt"
            val success = repository.createTextFile(_currentPath.value, fullName, content)
            if (success) {
                showMessage("Document '$fullName' created")
                loadCurrentDirectory(_currentPath.value)
                loadStorageStats()
            } else {
                showMessage("Failed to create document")
            }
        }
    }

    fun saveDocumentContent(content: String) {
        val file = _activeText.value ?: return
        if (file.isContentUri || file.isProtected) {
            showMessage("Read-only preview cannot overwrite source file")
            return
        }
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                try {
                    File(file.path).writeText(content)
                    true
                } catch (_: Exception) {
                    false
                }
            }
            showMessage(if (saved) "Document saved" else "Failed to save changes")
        }
    }

    fun renameFile(file: FileItem, newName: String) {
        viewModelScope.launch {
            val success = repository.renameItem(file.path, newName)
            if (success) {
                showMessage("Renamed to '$newName'")
                loadCurrentDirectory(_currentPath.value)
            } else {
                showMessage("Failed to rename")
            }
        }
    }

    fun deleteFilesToTrash(files: List<FileItem>) {
        viewModelScope.launch {
            var count = 0
            for (file in files) {
                if (repository.deleteToTrash(file)) count++
            }
            showMessage(if (count > 0) "Moved $count item(s) to Recycle Bin" else "Could not remove selected items")
            loadCurrentDirectory(_currentPath.value)
            loadRecentFiles()
            loadStorageStats()
        }
    }

    fun restoreTrashItem(trashEntity: TrashEntity) {
        viewModelScope.launch {
            if (repository.restoreFromTrash(trashEntity)) {
                showMessage("Restored '${trashEntity.fileName}'")
                loadCurrentDirectory(_currentPath.value)
                loadStorageStats()
            } else {
                showMessage("Unable to restore '${trashEntity.fileName}'")
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            if (repository.emptyTrash()) {
                showMessage("Recycle Bin emptied")
                loadStorageStats()
            }
        }
    }

    // Clipboard
    fun copyFiles(files: List<FileItem>) {
        _clipboard.value = ClipboardItem(ClipboardAction.COPY, files)
        showMessage("${files.size} item(s) copied to clipboard")
    }

    fun cutFiles(files: List<FileItem>) {
        _clipboard.value = ClipboardItem(ClipboardAction.CUT, files)
        showMessage("${files.size} item(s) ready to move")
    }

    fun pasteClipboard() {
        val clip = _clipboard.value ?: return
        viewModelScope.launch {
            val success = repository.executeClipboardPaste(clip, _currentPath.value)
            if (success) {
                showMessage(if (clip.action == ClipboardAction.CUT) "Files moved" else "Files copied")
                _clipboard.value = null
                loadCurrentDirectory(_currentPath.value)
                loadStorageStats()
            } else {
                showMessage("Failed to paste items (check destination permissions)")
            }
        }
    }

    fun clearClipboard() {
        _clipboard.value = null
    }

    fun toggleFavorite(file: FileItem) {
        viewModelScope.launch {
            repository.toggleFavorite(file.path)
            loadCurrentDirectory(_currentPath.value)
        }
    }

    // --- Safe Vault Persistent Auth & AES-256-GCM Operations ---

    fun setupVaultCredentials(pin: String, question: String, answer: String) {
        if (pin.length != 4) return
        securityManager.setupVaultCredentials(
            pin = pin,
            recoveryQuestion = question,
            recoveryAnswer = answer
        )
        sessionUnlockedPin = pin
        _isPinConfigured.value = true
        _isVaultPasswordEnabled.value = true
        _hasRecoveryConfigured.value = securityManager.hasRecoveryConfigured()
        _recoveryQuestion.value = securityManager.getRecoveryQuestion()
        _vaultFailedAttempts.value = 0
        _vaultLockoutSeconds.value = 0L
        _isVaultUnlocked.value = true
        showMessage("Safe Vault PIN & Recovery Security configured")
    }

    fun unlockVault(pin: String): Boolean {
        if (!_isVaultPasswordEnabled.value && _isPinConfigured.value) {
            _isVaultUnlocked.value = true
            return true
        }
        val remaining = securityManager.getLockoutRemainingSeconds()
        if (remaining > 0L) {
            _vaultLockoutSeconds.value = remaining
            startLockoutCountdown()
            return false
        }
        val verified = securityManager.verifyPin(pin)
        _vaultFailedAttempts.value = securityManager.getFailedAttempts()
        val newLockout = securityManager.getLockoutRemainingSeconds()
        _vaultLockoutSeconds.value = newLockout
        if (newLockout > 0L) startLockoutCountdown()

        if (verified) {
            sessionUnlockedPin = pin
            _isVaultUnlocked.value = true
        }
        return verified
    }

    fun verifyRecoveryAnswer(input: String): Boolean {
        return securityManager.verifyRecoveryAnswer(input)
    }

    fun setNewVaultPin(newPin: String) {
        if (newPin.length != 4) return
        securityManager.updatePin(newPin)
        sessionUnlockedPin = newPin
        _isPinConfigured.value = true
        _isVaultUnlocked.value = true
        _isVaultPasswordEnabled.value = true
        _vaultFailedAttempts.value = 0
        _vaultLockoutSeconds.value = 0L
        showMessage("Safe Vault PIN updated securely")
    }

    fun updateRecoverySecurity(question: String, answer: String) {
        if (answer.isBlank()) return
        securityManager.updateRecoveryQuestion(question, answer)
        _hasRecoveryConfigured.value = securityManager.hasRecoveryConfigured()
        _recoveryQuestion.value = securityManager.getRecoveryQuestion()
        showMessage("Recovery security question updated")
    }

    fun setVaultPasswordEnabled(enabled: Boolean) {
        securityManager.setPasswordProtectionEnabled(enabled)
        _isVaultPasswordEnabled.value = enabled
        if (!enabled) {
            _isVaultUnlocked.value = true
        }
        showMessage(if (enabled) "Safe Vault PIN lock enabled" else "Safe Vault PIN lock disabled")
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    private fun startLockoutCountdown() {
        lockoutTickerJob?.cancel()
        lockoutTickerJob = viewModelScope.launch {
            while (isActive) {
                val rem = securityManager.getLockoutRemainingSeconds()
                _vaultLockoutSeconds.value = rem
                if (rem <= 0L) break
                delay(1000L)
            }
        }
    }

    private fun getVaultCandidateKeys(): List<String> {
        val list = mutableListOf(securityManager.getOrCreateMasterKeySecret())
        sessionUnlockedPin?.let { list.add(it) }
        list.add("1234") // Legacy compatibility for vault files encrypted prior to keystore upgrade
        return list.distinct()
    }

    fun addToVault(file: FileItem) {
        viewModelScope.launch {
            val masterSecret = securityManager.getOrCreateMasterKeySecret()
            val success = repository.moveToVault(file, masterSecret)
            if (success) {
                showMessage("Encrypted with AES-256-GCM in Safe Vault")
                loadCurrentDirectory(_currentPath.value)
                loadStorageStats()
            } else {
                showMessage("Failed to lock item in Vault")
            }
        }
    }

    fun restoreFromVault(vaultEntity: VaultEntity) {
        viewModelScope.launch {
            val success = repository.restoreFromVault(vaultEntity, getVaultCandidateKeys(), _currentPath.value)
            if (success) {
                showMessage("Decrypted & restored '${vaultEntity.originalName}'")
                loadCurrentDirectory(_currentPath.value)
                loadStorageStats()
            } else {
                showMessage("Failed to decrypt Vault item")
            }
        }
    }

    fun previewVaultItem(vaultEntity: VaultEntity) {
        viewModelScope.launch {
            val tempItem = repository.decryptVaultItemToPreviewTemp(vaultEntity, getVaultCandidateKeys())
            if (tempItem != null) {
                openFile(tempItem)
            } else {
                showMessage("Unable to decrypt Vault file for preview")
            }
        }
    }

    // Storage Optimizer
    fun scanOptimizer(auto: Boolean = false) {
        viewModelScope.launch {
            _isScanning.value = true
            _junkResult.value = repository.scanJunkFiles()
            _duplicateGroups.value = repository.findDuplicateFiles()
            _largeFiles.value = repository.findLargeFiles()
            _isScanning.value = false
            if (!auto) {
                showMessage("Storage analysis complete")
            }
        }
    }

    fun cleanJunk() {
        val currentJunk = _junkResult.value ?: return
        viewModelScope.launch {
            val freed = repository.cleanJunkFiles(currentJunk)
            showMessage("Cleaned ${FileItem.formatFileSize(freed)} of residual junk")
            scanOptimizer(auto = true)
            loadStorageStats()
        }
    }

    fun deleteDuplicateFile(file: FileItem) {
        viewModelScope.launch {
            repository.deleteToTrash(file)
            showMessage("Duplicate moved to Recycle Bin")
            _duplicateGroups.value = repository.findDuplicateFiles()
            loadStorageStats()
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun toggleFtpServer() {
        if (isFtpRunning.value) {
            localWebServer.stopServer()
            showMessage("Wi-Fi File Server stopped")
        } else {
            val url = localWebServer.startServer()
            if (isFtpRunning.value) {
                showMessage("Wi-Fi Server active at $url")
            } else {
                showMessage(url)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        localWebServer.stopServer()
        audioProgressJob?.cancel()
        audioPlayer?.release()
        audioPlayer = null
        repository.clearVaultPreviewCache()
    }

    private fun showMessage(msg: String) {
        _userMessage.value = msg
    }
}
