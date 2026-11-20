package com.example.data.repository

import android.Manifest
import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.ContextCompat
import com.example.data.crypto.VaultCrypto
import com.example.data.db.AppDatabase
import com.example.data.db.FavoriteEntity
import com.example.data.db.TrashDao
import com.example.data.db.TrashEntity
import com.example.data.db.VaultDao
import com.example.data.db.VaultEntity
import com.example.data.model.ClipboardAction
import com.example.data.model.ClipboardItem
import com.example.data.model.ExternalVolumeStats
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.data.model.RamStats
import com.example.data.model.StorageStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

class FileManagerRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    val trashDao: TrashDao = db.trashDao()
    val vaultDao: VaultDao = db.vaultDao()
    private val favoriteDao = db.favoriteDao()

    private val trashDir: File
        get() = File(context.filesDir, "trash").apply { if (!exists()) mkdirs() }

    private val vaultDir: File
        get() = File(context.filesDir, "vault").apply { if (!exists()) mkdirs() }

    private val vaultPreviewDir: File
        get() = File(context.cacheDir, "vault_preview").apply { if (!exists()) mkdirs() }

    fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Environment.isExternalStorageManager()
            } catch (_: Throwable) {
                false
            }
        } else {
            val read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            val write = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            read && write
        }
    }

    fun hasMediaPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED ||
                hasAllFilesAccess()
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    val rootPath: String
        get() {
            val ext = try {
                Environment.getExternalStorageDirectory()
            } catch (_: Throwable) {
                null
            }
            if (ext != null && ext.exists() && ext.canRead() && ext.listFiles() != null) {
                return ext.absolutePath
            }
            val scopedExt = try {
                context.getExternalFilesDir(null)
            } catch (_: Throwable) {
                null
            }
            return (scopedExt ?: context.filesDir).apply { if (!exists()) mkdirs() }.absolutePath
        }

    fun getAccessibleSdCardRoot(): String? {
        return detectRemovableVolumeStats()?.path
    }

    /**
     * Checks that target file is not inside private app security/database directories.
     */
    private fun isUserManagedPathSafe(file: File): Boolean {
        return try {
            val canonical = file.canonicalPath
            val privateData = context.filesDir.parentFile?.canonicalPath
            if (privateData != null) {
                val forbidden = listOf(
                    vaultDir.canonicalPath,
                    trashDir.canonicalPath,
                    File(privateData, "databases").canonicalPath,
                    File(privateData, "shared_prefs").canonicalPath
                )
                if (forbidden.any { canonical == it || canonical.startsWith(it + File.separator) }) {
                    return false
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun sanitizeName(rawName: String): String? {
        val trimmed = rawName.trim()
        if (trimmed.isEmpty() || trimmed == "." || trimmed == "..") return null
        if (trimmed.contains('/') || trimmed.contains('\\') || trimmed.contains('\u0000')) return null
        return trimmed
    }

    private fun uniqueTargetFile(dir: File, desiredName: String): File {
        var candidate = File(dir, desiredName)
        if (!candidate.exists()) return candidate
        val dotIdx = desiredName.lastIndexOf('.')
        val base = if (dotIdx > 0) desiredName.substring(0, dotIdx) else desiredName
        val ext = if (dotIdx > 0) desiredName.substring(dotIdx) else ""
        var counter = 1
        while (candidate.exists() && counter < 1000) {
            candidate = File(dir, "$base ($counter)$ext")
            counter++
        }
        return candidate
    }

    private fun detectRemovableVolumeStats(): ExternalVolumeStats? {
        return try {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
            if (storageManager != null) {
                for (vol in storageManager.storageVolumes) {
                    if (vol.isRemovable && vol.state == Environment.MEDIA_MOUNTED) {
                        val volDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            vol.directory
                        } else {
                            // On Android 8-10, derive root from secondary getExternalFilesDirs entry if available
                            val dirs = context.getExternalFilesDirs(null)
                            val primary = context.getExternalFilesDir(null)?.canonicalPath
                            val secondary = dirs.firstOrNull { it != null && it.canonicalPath != primary }
                            secondary?.parentFile?.parentFile?.parentFile?.parentFile?.takeIf { it.exists() && it.canRead() }
                        }
                        if (volDir != null && volDir.exists() && volDir.canRead()) {
                            val stat = StatFs(volDir.path)
                            val total = stat.totalBytes
                            val free = stat.availableBytes
                            if (total > 0L) {
                                val used = (total - free).coerceAtLeast(0L)
                                val pct = (used.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                                val label = vol.getDescription(context)?.takeIf { it.isNotBlank() } ?: "SD Card"
                                return ExternalVolumeStats(
                                    label = label,
                                    path = volDir.absolutePath,
                                    totalBytes = total,
                                    usedBytes = used,
                                    freeBytes = free,
                                    usedPercentage = pct
                                )
                            }
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getStorageStats(includeCategoryBreakdown: Boolean = true): StorageStats = withContext(Dispatchers.IO) {
        try {
            val allFilesGranted = hasAllFilesAccess()
            var totalBytes = 0L
            var freeBytes = 0L

            // 1. Primary Internal Storage via official StorageStatsManager (API 26+)
            try {
                val statsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
                if (statsManager != null) {
                    totalBytes = statsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
                    freeBytes = statsManager.getFreeBytes(StorageManager.UUID_DEFAULT)
                }
            } catch (e: Exception) {
                // Fallback to StatFs below
            }

            // 2. Fallback to StatFs on primary storage directory
            if (totalBytes <= 0L) {
                val ext = try {
                    Environment.getExternalStorageDirectory()
                } catch (_: Throwable) {
                    null
                }
                val targetDir = when {
                    ext != null && ext.exists() -> ext
                    else -> Environment.getDataDirectory()
                }
                if (targetDir != null && targetDir.exists()) {
                    val stat = StatFs(targetDir.path)
                    totalBytes = stat.totalBytes
                    freeBytes = stat.availableBytes
                }
            }

            if (totalBytes <= 0L) {
                return@withContext StorageStats(isAvailable = false, hasAllFilesPermission = allFilesGranted)
            }

            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
            val usedPercentage = (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)

            val categorySizes = mutableMapOf<FileCategory, Long>()
            val categoryCounts = mutableMapOf<FileCategory, Int>()
            var rootItemCount = 0

            val rootDir = File(rootPath)
            if (rootDir.exists() && rootDir.canRead()) {
                rootItemCount = rootDir.listFiles()?.count { !it.name.startsWith(".") } ?: 0
            }

            if (includeCategoryBreakdown) {
                val visited = mutableSetOf<String>()
                if (rootDir.exists() && rootDir.canRead()) {
                    val fsFiles = listAllFilesRecursively(rootDir, maxDepth = 4, maxFiles = 6000)
                    for (file in fsFiles) {
                        currentCoroutineContext().ensureActive()
                        if (!file.isDirectory) {
                            val key = "${file.name}_${file.length()}"
                            visited.add(key)
                            val cat = FileItem.getCategoryForFile(file)
                            categorySizes[cat] = (categorySizes[cat] ?: 0L) + file.length()
                            categoryCounts[cat] = (categoryCounts[cat] ?: 0) + 1
                        }
                    }
                }

                // Merge Downloads count specifically if Downloads folder is accessible
                val dlDir = File(rootDir, Environment.DIRECTORY_DOWNLOADS)
                if (dlDir.exists() && dlDir.canRead()) {
                    val dlFiles = listAllFilesRecursively(dlDir, maxDepth = 3, maxFiles = 2000).filter { !it.isDirectory }
                    categorySizes[FileCategory.DOWNLOADS] = dlFiles.sumOf { it.length() }
                    categoryCounts[FileCategory.DOWNLOADS] = dlFiles.size
                }

                // Merge MediaStore sizes/counts for media categories when permitted (captures MediaStore items not visible via File API)
                if (hasMediaPermissions()) {
                    for (mediaCat in listOf(FileCategory.IMAGES, FileCategory.VIDEOS, FileCategory.AUDIO)) {
                        val msItems = queryMediaStore(mediaCat, limit = 2000)
                        for (item in msItems) {
                            val key = "${item.name}_${item.size}"
                            if (visited.add(key)) {
                                categorySizes[mediaCat] = (categorySizes[mediaCat] ?: 0L) + item.size
                                categoryCounts[mediaCat] = (categoryCounts[mediaCat] ?: 0) + 1
                            }
                        }
                    }
                }
            }

            val sdCard = detectRemovableVolumeStats()

            StorageStats(
                isAvailable = true,
                hasAllFilesPermission = allFilesGranted,
                totalBytes = totalBytes,
                usedBytes = usedBytes,
                freeBytes = freeBytes,
                usedPercentage = usedPercentage,
                categorySizes = categorySizes,
                categoryCounts = categoryCounts,
                totalRootItems = rootItemCount,
                sdCardStats = sdCard
            )
        } catch (e: Exception) {
            StorageStats(isAvailable = false, hasAllFilesPermission = hasAllFilesAccess())
        }
    }

    suspend fun getFilesInDirectory(directoryPath: String, showHidden: Boolean = false): List<FileItem> = withContext(Dispatchers.IO) {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory || !dir.canRead() || !isUserManagedPathSafe(dir)) {
            return@withContext emptyList()
        }

        val rawFiles = dir.listFiles() ?: return@withContext emptyList()
        val files = if (showHidden) rawFiles.toList() else rawFiles.filter { !it.name.startsWith(".") }

        // Single batch query instead of N+1 SQLite calls
        val favoritePaths = favoriteDao.getAllFavoritePaths().toHashSet()

        val result = ArrayList<FileItem>(files.size)
        for (file in files) {
            currentCoroutineContext().ensureActive()
            if (!isUserManagedPathSafe(file)) continue
            val isDir = file.isDirectory
            // Avoid deep recursive folder traversal on listing for 60fps smooth loading
            val itemCount = if (isDir) (file.list()?.size ?: 0) else 0
            val size = if (isDir) 0L else file.length()
            val cat = FileItem.getCategoryForFile(file)
            val isFav = favoritePaths.contains(file.absolutePath)

            result.add(
                FileItem(
                    path = file.absolutePath,
                    name = file.name,
                    size = size,
                    lastModified = file.lastModified(),
                    isDirectory = isDir,
                    category = cat,
                    extension = file.extension.lowercase(),
                    itemCount = itemCount,
                    isFavorite = isFav
                )
            )
        }
        result.sortedWith(
            compareBy<FileItem> { !it.isDirectory }
                .thenBy { it.name.lowercase() }
        )
    }

    suspend fun searchFiles(
        query: String,
        showHidden: Boolean = false,
        limit: Int = 250,
        searchRoot: String = rootPath
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return@withContext emptyList()

        val root = File(searchRoot)
        val favoritePaths = favoriteDao.getAllFavoritePaths().toHashSet()
        val seen = mutableSetOf<String>()
        val result = mutableListOf<FileItem>()

        if (root.exists() && root.canRead()) {
            val scanned = listAllFilesRecursively(root, maxDepth = 5, maxFiles = 6000)
            for (file in scanned) {
                currentCoroutineContext().ensureActive()
                if (!showHidden && file.name.startsWith(".")) continue
                if (!isUserManagedPathSafe(file)) continue
                if (file.name.lowercase().contains(needle)) {
                    if (seen.add(file.absolutePath)) {
                        val isDir = file.isDirectory
                        result.add(
                            FileItem(
                                path = file.absolutePath,
                                name = file.name,
                                size = if (isDir) 0L else file.length(),
                                lastModified = file.lastModified(),
                                isDirectory = isDir,
                                category = FileItem.getCategoryForFile(file),
                                extension = file.extension.lowercase(),
                                itemCount = if (isDir) (file.list()?.size ?: 0) else 0,
                                isFavorite = favoritePaths.contains(file.absolutePath)
                            )
                        )
                        if (result.size >= limit) break
                    }
                }
            }
        }

        if (result.size < limit && hasMediaPermissions()) {
            for (cat in listOf(FileCategory.IMAGES, FileCategory.VIDEOS, FileCategory.AUDIO)) {
                val msItems = queryMediaStore(cat, limit = 500)
                for (item in msItems) {
                    if (item.name.lowercase().contains(needle) && seen.add(item.path)) {
                        result.add(item)
                        if (result.size >= limit) break
                    }
                }
                if (result.size >= limit) break
            }
        }

        result.sortedWith(
            compareBy<FileItem> { !it.isDirectory }
                .thenBy { it.name.lowercase() }
        )
    }

    suspend fun queryMediaStore(category: FileCategory, limit: Int = 500): List<FileItem> = withContext(Dispatchers.IO) {
        if (!hasMediaPermissions()) return@withContext emptyList()
        val result = mutableListOf<FileItem>()
        try {
            val (uri, projection, sortOrder) = when (category) {
                FileCategory.IMAGES -> Triple(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.DATE_MODIFIED,
                        MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media.DATA
                    ),
                    "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
                )
                FileCategory.VIDEOS -> Triple(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DATE_MODIFIED,
                        MediaStore.Video.Media.MIME_TYPE,
                        MediaStore.Video.Media.DATA
                    ),
                    "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
                )
                FileCategory.AUDIO -> Triple(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATE_MODIFIED,
                        MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.DATA
                    ),
                    "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
                )
                else -> return@withContext emptyList()
            }

            context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                val dataCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)

                var count = 0
                while (cursor.moveToNext() && count < limit) {
                    currentCoroutineContext().ensureActive()
                    val id = if (idCol != -1) cursor.getLong(idCol) else -1L
                    val name = if (nameCol != -1) cursor.getString(nameCol) ?: "file" else "file"
                    val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                    val dateModified = if (dateCol != -1) cursor.getLong(dateCol) * 1000L else System.currentTimeMillis()
                    val mime = if (mimeCol != -1) cursor.getString(mimeCol) else null
                    val dataPath = if (dataCol != -1) cursor.getString(dataCol) else null
                    val contentUri = if (id != -1L) ContentUris.withAppendedId(uri, id).toString() else ""
                    // Prefer readable filesystem path when valid, otherwise content:// URI
                    val finalPath = if (!dataPath.isNullOrBlank() && File(dataPath).let { it.exists() && it.canRead() }) {
                        dataPath
                    } else {
                        contentUri
                    }

                    if (finalPath.isNotBlank()) {
                        val ext = name.substringAfterLast('.', "").lowercase()
                        result.add(
                            FileItem(
                                path = finalPath,
                                name = name,
                                size = size,
                                lastModified = dateModified,
                                isDirectory = false,
                                category = category,
                                extension = ext,
                                mimeType = mime
                            )
                        )
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            // Safe fallback on denied/restricted MediaStore queries
        }
        result
    }

    suspend fun resolveContentUriToFileItem(uri: Uri): FileItem? = withContext(Dispatchers.IO) {
        try {
            val cr = context.contentResolver
            val mimeType = cr.getType(uri)
            var displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected_File"
            var size = 0L

            cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx != -1) {
                        cursor.getString(nameIdx)?.takeIf { it.isNotBlank() }?.let { displayName = it }
                    }
                    if (sizeIdx != -1 && !cursor.isNull(sizeIdx)) {
                        size = cursor.getLong(sizeIdx)
                    }
                }
            }

            val ext = displayName.substringAfterLast('.', "").lowercase()
            val cat = FileItem.getCategoryForExtension(ext, mimeType)

            FileItem(
                path = uri.toString(),
                name = displayName,
                size = size,
                lastModified = System.currentTimeMillis(),
                isDirectory = false,
                category = cat,
                extension = ext,
                mimeType = mimeType
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRecentFiles(limit: Int = 15): List<FileItem> = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        val fileSys = if (root.exists() && root.canRead()) {
            listAllFilesRecursively(root, maxDepth = 4, maxFiles = 3000)
                .filter { !it.isDirectory && !it.name.startsWith(".") }
                .map { file ->
                    FileItem(
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        isDirectory = false,
                        category = FileItem.getCategoryForFile(file),
                        extension = file.extension.lowercase()
                    )
                }
        } else {
            emptyList()
        }

        val mediaImages = queryMediaStore(FileCategory.IMAGES, limit = 10)
        val mediaAudio = queryMediaStore(FileCategory.AUDIO, limit = 10)
        val mediaVideos = queryMediaStore(FileCategory.VIDEOS, limit = 10)

        val seen = mutableSetOf<String>()
        val combined = mutableListOf<FileItem>()
        for (item in (fileSys + mediaImages + mediaAudio + mediaVideos)) {
            val key = "${item.name}_${item.size}"
            if (seen.add(key) && seen.add(item.path)) {
                combined.add(item)
            }
        }
        combined.sortedByDescending { it.lastModified }.take(limit)
    }

    suspend fun getFilesByCategory(category: FileCategory): List<FileItem> = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        val fileSysItems = if (category == FileCategory.DOWNLOADS) {
            val dlDir = File(root, Environment.DIRECTORY_DOWNLOADS)
            if (dlDir.exists() && dlDir.canRead()) {
                listAllFilesRecursively(dlDir, maxDepth = 4, maxFiles = 3000)
                    .filter { !it.isDirectory && !it.name.startsWith(".") }
                    .map { file ->
                        FileItem(
                            path = file.absolutePath,
                            name = file.name,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            isDirectory = false,
                            category = FileItem.getCategoryForFile(file),
                            extension = file.extension.lowercase()
                        )
                    }
            } else {
                emptyList()
            }
        } else if (root.exists() && root.canRead()) {
            listAllFilesRecursively(root, maxDepth = 5, maxFiles = 6000)
                .filter { !it.isDirectory && !it.name.startsWith(".") }
                .filter { FileItem.getCategoryForFile(it) == category }
                .map { file ->
                    FileItem(
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        isDirectory = false,
                        category = category,
                        extension = file.extension.lowercase()
                    )
                }
        } else {
            emptyList()
        }

        val mediaStoreItems = if (category == FileCategory.IMAGES || category == FileCategory.VIDEOS || category == FileCategory.AUDIO) {
            queryMediaStore(category, limit = 1000)
        } else {
            emptyList()
        }

        val seen = mutableSetOf<String>()
        val combined = mutableListOf<FileItem>()
        for (item in (fileSysItems + mediaStoreItems)) {
            val key = "${item.name}_${item.size}"
            if (seen.add(key) && seen.add(item.path)) {
                combined.add(item)
            }
        }
        combined.sortedByDescending { it.lastModified }
    }

    suspend fun createFolder(parentPath: String, folderName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val safeName = sanitizeName(folderName) ?: return@withContext false
            val parent = File(parentPath)
            if (!parent.exists() || !parent.isDirectory || !isUserManagedPathSafe(parent)) return@withContext false
            val newDir = File(parent, safeName)
            if (!isUserManagedPathSafe(newDir)) return@withContext false
            newDir.mkdirs()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createTextFile(parentPath: String, fileName: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val safeName = sanitizeName(fileName) ?: return@withContext false
            val parent = File(parentPath)
            if (!parent.exists() || !parent.isDirectory || !isUserManagedPathSafe(parent)) return@withContext false
            val newFile = uniqueTargetFile(parent, safeName)
            if (!isUserManagedPathSafe(newFile)) return@withContext false
            newFile.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun renameItem(oldPath: String, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val safeName = sanitizeName(newName) ?: return@withContext false
            val oldFile = File(oldPath)
            if (!oldFile.exists() || !isUserManagedPathSafe(oldFile)) return@withContext false
            val parent = oldFile.parentFile ?: return@withContext false
            val newFile = File(parent, safeName)
            if (newFile.exists() || !isUserManagedPathSafe(newFile)) return@withContext false
            oldFile.renameTo(newFile)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleHideFile(file: FileItem): Boolean = withContext(Dispatchers.IO) {
        try {
            if (file.isContentUri) return@withContext false
            val oldFile = File(file.path)
            if (!oldFile.exists() || !isUserManagedPathSafe(oldFile)) return@withContext false
            val newName = if (file.name.startsWith(".")) {
                file.name.removePrefix(".")
            } else {
                ".${file.name}"
            }
            val safeName = sanitizeName(newName) ?: return@withContext false
            val newFile = File(oldFile.parentFile, safeName)
            if (newFile.exists() || !isUserManagedPathSafe(newFile)) return@withContext false
            oldFile.renameTo(newFile)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteToTrash(item: FileItem): Boolean = withContext(Dispatchers.IO) {
        try {
            if (item.isContentUri) {
                val uri = Uri.parse(item.path)
                val rows = context.contentResolver.delete(uri, null, null)
                return@withContext rows > 0
            }

            val src = File(item.path)
            if (!src.exists() || !isUserManagedPathSafe(src)) return@withContext false

            val trashId = System.currentTimeMillis()
            val safeBase = item.name.replace('/', '_').replace('\\', '_')
            val target = File(trashDir, "${trashId}_$safeBase")

            var moved = src.renameTo(target)
            if (!moved) {
                // Cross-filesystem fallback: copy safely then delete source
                val copied = if (src.isDirectory) {
                    src.copyRecursively(target, overwrite = false)
                } else {
                    src.copyTo(target, overwrite = false)
                    target.exists() && target.length() == src.length()
                }
                if (copied) {
                    moved = src.deleteRecursively()
                    if (!moved) {
                        target.deleteRecursively()
                    }
                } else {
                    target.deleteRecursively()
                }
            }

            if (moved) {
                trashDao.insert(
                    TrashEntity(
                        id = trashId,
                        originalPath = item.path,
                        fileName = item.name,
                        size = item.size,
                        isDirectory = item.isDirectory
                    )
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun restoreFromTrash(trashEntity: TrashEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val trashFile = File(trashDir, "${trashEntity.id}_${trashEntity.fileName}")
            if (!trashFile.exists()) {
                trashDao.delete(trashEntity)
                return@withContext false
            }

            val intended = File(trashEntity.originalPath)
            val parent = intended.parentFile?.takeIf { isUserManagedPathSafe(it) } ?: File(rootPath)
            if (!parent.exists()) parent.mkdirs()
            val targetFile = uniqueTargetFile(parent, intended.name)
            if (!isUserManagedPathSafe(targetFile)) return@withContext false

            var restored = trashFile.renameTo(targetFile)
            if (!restored) {
                val copied = if (trashFile.isDirectory) {
                    trashFile.copyRecursively(targetFile, overwrite = false)
                } else {
                    trashFile.copyTo(targetFile, overwrite = false)
                    targetFile.exists() && targetFile.length() == trashFile.length()
                }
                if (copied) {
                    restored = trashFile.deleteRecursively()
                } else {
                    targetFile.deleteRecursively()
                }
            }

            if (restored) {
                trashDao.delete(trashEntity)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun emptyTrash(): Boolean = withContext(Dispatchers.IO) {
        try {
            trashDir.listFiles()?.forEach { it.deleteRecursively() }
            trashDao.clearAll()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun executeClipboardPaste(clipboard: ClipboardItem, destinationPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val destDir = File(destinationPath)
            if (!destDir.exists() || !destDir.isDirectory || !destDir.canWrite() || !isUserManagedPathSafe(destDir)) {
                return@withContext false
            }

            val destCanonical = destDir.canonicalPath
            var anySuccess = false

            for (fileItem in clipboard.files) {
                currentCoroutineContext().ensureActive()
                if (fileItem.isContentUri) continue
                val src = File(fileItem.path)
                if (!src.exists() || !isUserManagedPathSafe(src)) continue

                val srcCanonical = src.canonicalPath
                // Prevent infinite recursion: cannot copy/move a directory into itself or its descendants
                if (src.isDirectory && (destCanonical == srcCanonical || destCanonical.startsWith(srcCanonical + File.separator))) {
                    continue
                }

                val dest = uniqueTargetFile(destDir, src.name)
                if (!isUserManagedPathSafe(dest)) continue

                if (clipboard.action == ClipboardAction.CUT) {
                    var moved = src.renameTo(dest)
                    if (!moved) {
                        val copied = if (src.isDirectory) {
                            src.copyRecursively(dest, overwrite = false)
                        } else {
                            src.copyTo(dest, overwrite = false)
                            dest.exists() && dest.length() == src.length()
                        }
                        if (copied) {
                            moved = src.deleteRecursively()
                        }
                    }
                    if (moved) anySuccess = true
                } else {
                    val copied = if (src.isDirectory) {
                        src.copyRecursively(dest, overwrite = false)
                    } else {
                        src.copyTo(dest, overwrite = false)
                        dest.exists()
                    }
                    if (copied) anySuccess = true
                }
            }
            anySuccess
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleFavorite(path: String) = withContext(Dispatchers.IO) {
        if (favoriteDao.isFavorite(path)) {
            favoriteDao.remove(path)
        } else {
            favoriteDao.insert(FavoriteEntity(path))
        }
    }

    // Safe Vault operations
    suspend fun moveToVault(item: FileItem, masterKeySecret: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (item.isDirectory || item.isContentUri) return@withContext false
            val src = File(item.path)
            if (!src.exists() || !src.canRead() || !isUserManagedPathSafe(src)) return@withContext false

            val safeName = item.name.replace('/', '_').replace('\\', '_')
            val encName = "${System.currentTimeMillis()}_${safeName}.enc"
            val dest = File(vaultDir, encName)

            val encrypted = VaultCrypto.encryptFile(src, dest, masterKeySecret)
            if (encrypted) {
                val deletedSource = src.delete()
                vaultDao.insert(
                    VaultEntity(
                        originalName = item.name,
                        encryptedFileName = encName,
                        originalSize = item.size,
                        mimeType = item.category.label
                    )
                )
                deletedSource || dest.exists()
            } else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Attempts decryption with primary master key first, then falls back to candidate legacy PINs
     * so encrypted files from prior app versions are never stranded or unreadable.
     */
    private fun decryptWithKeyChain(encFile: File, destFile: File, keys: List<String>): Boolean {
        for (key in keys.distinct()) {
            if (key.isNotEmpty() && VaultCrypto.decryptFile(encFile, destFile, key)) {
                return true
            }
        }
        return false
    }

    suspend fun restoreFromVault(vaultEntity: VaultEntity, candidateKeys: List<String>, restoreDir: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val encFile = File(vaultDir, vaultEntity.encryptedFileName)
            if (!encFile.exists()) return@withContext false

            val destParent = File(restoreDir).takeIf { it.exists() && it.canWrite() && isUserManagedPathSafe(it) }
                ?: File(rootPath)
            val safeOrig = sanitizeName(vaultEntity.originalName) ?: "Restored_${System.currentTimeMillis()}"
            val destFile = uniqueTargetFile(destParent, safeOrig)
            if (!isUserManagedPathSafe(destFile)) return@withContext false

            val decrypted = decryptWithKeyChain(encFile, destFile, candidateKeys)
            if (decrypted) {
                encFile.delete()
                vaultDao.delete(vaultEntity)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun decryptVaultItemToPreviewTemp(vaultEntity: VaultEntity, candidateKeys: List<String>): FileItem? = withContext(Dispatchers.IO) {
        try {
            val encFile = File(vaultDir, vaultEntity.encryptedFileName)
            if (!encFile.exists()) return@withContext null

            val safeOrig = sanitizeName(vaultEntity.originalName) ?: "vault_item"
            val tempFile = File(vaultPreviewDir, safeOrig)
            if (decryptWithKeyChain(encFile, tempFile, candidateKeys)) {
                val ext = tempFile.extension.lowercase()
                FileItem(
                    path = tempFile.absolutePath,
                    name = vaultEntity.originalName,
                    size = tempFile.length(),
                    lastModified = vaultEntity.vaultedAt,
                    isDirectory = false,
                    category = FileItem.getCategoryForExtension(ext),
                    extension = ext,
                    isProtected = true
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun clearVaultPreviewCache() {
        try {
            vaultPreviewDir.listFiles()?.forEach { it.deleteRecursively() }
        } catch (e: Exception) {
            // ignore
        }
    }

    // Storage Optimizer
    data class JunkScanResult(
        val totalJunkBytes: Long,
        val tempFiles: List<File>,
        val emptyFolders: List<File>,
        val cacheFiles: List<File>
    )

    suspend fun scanJunkFiles(): JunkScanResult = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        val temp = mutableListOf<File>()
        val emptyDirs = mutableListOf<File>()
        val caches = mutableListOf<File>()
        var junkBytes = 0L

        fun scan(file: File, depth: Int) {
            if (depth > 4) return
            if (!isUserManagedPathSafe(file)) return
            if (file.isDirectory) {
                val children = file.listFiles()
                if (children != null && children.isEmpty() && file.absolutePath != root.absolutePath && file.name != "Android") {
                    emptyDirs.add(file)
                } else {
                    children?.forEach {
                        if (it.name != "Android" && !it.name.startsWith(".")) {
                            scan(it, depth + 1)
                        }
                    }
                }
            } else {
                val name = file.name.lowercase()
                if (name.endsWith(".tmp") || name.endsWith(".log") || name.startsWith("cache_")) {
                    temp.add(file)
                    junkBytes += file.length()
                }
            }
        }

        if (root.exists() && root.canRead()) {
            scan(root, 0)
        }

        context.cacheDir.listFiles()?.forEach {
            if (it.name != "vault_preview") {
                caches.add(it)
                junkBytes += getDirectoryOrFileSize(it)
            }
        }

        JunkScanResult(
            totalJunkBytes = junkBytes,
            tempFiles = temp,
            emptyFolders = emptyDirs,
            cacheFiles = caches
        )
    }

    suspend fun cleanJunkFiles(result: JunkScanResult): Long = withContext(Dispatchers.IO) {
        var freed = 0L
        result.tempFiles.forEach {
            if (isUserManagedPathSafe(it) && it.exists()) {
                val len = it.length()
                if (it.delete()) freed += len
            }
        }
        result.emptyFolders.forEach {
            if (isUserManagedPathSafe(it) && it.exists() && (it.listFiles()?.isEmpty() == true)) {
                it.delete()
            }
        }
        result.cacheFiles.forEach {
            val len = getDirectoryOrFileSize(it)
            if (it.deleteRecursively()) freed += len
        }
        freed
    }

    // Duplicate files scanner
    data class DuplicateGroup(
        val hash: String,
        val size: Long,
        val files: List<FileItem>
    )

    suspend fun findDuplicateFiles(): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        if (!root.exists() || !root.canRead()) return@withContext emptyList()
        val allFiles = listAllFilesRecursively(root, maxDepth = 4, maxFiles = 5000)
            .filter { !it.isDirectory && it.length() > 1024 }

        val sizeGroups = allFiles.groupBy { it.length() }.filter { it.value.size > 1 }
        val duplicates = mutableListOf<DuplicateGroup>()

        for ((size, files) in sizeGroups) {
            currentCoroutineContext().ensureActive()
            val hashGroups = files.groupBy { computeFileHash(it) }.filter { it.value.size > 1 }
            for ((hash, dupeFiles) in hashGroups) {
                duplicates.add(
                    DuplicateGroup(
                        hash = hash,
                        size = size,
                        files = dupeFiles.map { file ->
                            FileItem(
                                path = file.absolutePath,
                                name = file.name,
                                size = file.length(),
                                lastModified = file.lastModified(),
                                isDirectory = false,
                                category = FileItem.getCategoryForFile(file),
                                extension = file.extension.lowercase()
                            )
                        }
                    )
                )
            }
        }
        duplicates
    }

    suspend fun findLargeFiles(minSizeBytes: Long = 20_000_000L): List<FileItem> = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        if (!root.exists() || !root.canRead()) return@withContext emptyList()
        listAllFilesRecursively(root, maxDepth = 4, maxFiles = 5000)
            .filter { !it.isDirectory && it.length() >= minSizeBytes }
            .sortedByDescending { it.length() }
            .take(100)
            .map { file ->
                FileItem(
                    path = file.absolutePath,
                    name = file.name,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    isDirectory = false,
                    category = FileItem.getCategoryForFile(file),
                    extension = file.extension.lowercase()
                )
            }
    }

    private fun computeFileHash(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var read: Int
                var total = 0
                while (fis.read(buffer).also { read = it } != -1 && total < 131072) {
                    digest.update(buffer, 0, read)
                    total += read
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "${file.name}_${file.length()}"
        }
    }

    private fun getDirectoryOrFileSize(file: File): Long {
        if (!file.exists()) return 0L
        if (!file.isDirectory) return file.length()
        var total = 0L
        file.listFiles()?.forEach { child ->
            total += getDirectoryOrFileSize(child)
        }
        return total
    }

    private fun listAllFilesRecursively(dir: File, maxDepth: Int, maxFiles: Int = 5000): List<File> {
        val result = ArrayList<File>(256)
        fun walk(current: File, depth: Int) {
            if (depth > maxDepth || result.size >= maxFiles) return
            val children = current.listFiles() ?: return
            for (child in children) {
                if (result.size >= maxFiles) break
                if (child.name == "Android" && depth == 0) continue
                result.add(child)
                if (child.isDirectory && !child.name.startsWith(".")) {
                    walk(child, depth + 1)
                }
            }
        }
        if (dir.exists() && dir.canRead()) {
            walk(dir, 0)
        }
        return result
    }

    fun getRamStats(): RamStats {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                ?: return RamStats(isAvailable = false)
            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)
            val total = memInfo.totalMem
            val avail = memInfo.availMem
            if (total <= 0L) return RamStats(isAvailable = false)
            val used = (total - avail).coerceAtLeast(0L)
            val percent = (used.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            RamStats(
                isAvailable = true,
                totalBytes = total,
                availBytes = avail,
                usedBytes = used,
                usedPercentage = percent
            )
        } catch (e: Exception) {
            RamStats(isAvailable = false)
        }
    }

    suspend fun clearAppCache(): Long = withContext(Dispatchers.IO) {
        var freed = 0L
        try {
            context.cacheDir.listFiles()?.forEach {
                val len = getDirectoryOrFileSize(it)
                if (it.deleteRecursively()) freed += len
            }
        } catch (e: Exception) {
            // ignore
        }
        freed
    }
}
