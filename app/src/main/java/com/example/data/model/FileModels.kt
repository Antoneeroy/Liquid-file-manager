package com.example.data.model

import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FileCategory(val label: String) {
    IMAGES("Images"),
    VIDEOS("Videos"),
    AUDIO("Audio"),
    DOCUMENTS("Documents"),
    ARCHIVES("Archives"),
    APKS("APKs"),
    DOWNLOADS("Downloads"),
    OTHER("Files"),
    FOLDER("Folders")
}

data class FileItem(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val category: FileCategory,
    val extension: String,
    val itemCount: Int = 0,
    val isFavorite: Boolean = false,
    val isProtected: Boolean = false,
    val mimeType: String? = null
) {
    val isContentUri: Boolean
        get() = path.startsWith("content://")

    val isPdf: Boolean
        get() = extension.equals("pdf", ignoreCase = true) || mimeType == "application/pdf"

    val formattedSize: String
        get() = if (size < 0) "Unavailable" else formatFileSize(size)

    val isHidden: Boolean
        get() = name.startsWith(".")

    val formattedDate: String
        get() {
            if (lastModified <= 0L) return "Unknown"
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }

    val relativeTime: String
        get() {
            if (lastModified <= 0L) return "Unknown"
            val diff = System.currentTimeMillis() - lastModified
            if (diff < 0) return formattedDate
            val hours = diff / (1000 * 60 * 60)
            val days = hours / 24
            return when {
                hours < 1 -> "Just now"
                hours < 24 -> "${hours}h ago"
                days < 7 -> "${days}d ago"
                else -> formattedDate
            }
        }

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, 4)
            val format = DecimalFormat("#,##0.#")
            return "${format.format(bytes / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
        }

        fun getCategoryForFile(file: File): FileCategory {
            if (file.isDirectory) return FileCategory.FOLDER
            return getCategoryForExtension(file.extension.lowercase())
        }

        fun getCategoryForExtension(ext: String, mimeType: String? = null): FileCategory {
            val cleanExt = ext.lowercase()
            if (mimeType != null) {
                when {
                    mimeType.startsWith("image/") -> return FileCategory.IMAGES
                    mimeType.startsWith("video/") -> return FileCategory.VIDEOS
                    mimeType.startsWith("audio/") -> return FileCategory.AUDIO
                    mimeType == "application/pdf" || mimeType.startsWith("text/") -> return FileCategory.DOCUMENTS
                    mimeType == "application/vnd.android.package-archive" -> return FileCategory.APKS
                    mimeType == "application/zip" || mimeType.contains("archive") || mimeType.contains("compressed") -> return FileCategory.ARCHIVES
                }
            }
            return when (cleanExt) {
                "jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic", "heif" -> FileCategory.IMAGES
                "mp4", "mkv", "webm", "avi", "mov", "3gp", "flv", "m4v", "ts" -> FileCategory.VIDEOS
                "mp3", "wav", "m4a", "flac", "aac", "ogg", "wma", "opus", "amr" -> FileCategory.AUDIO
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv", "json", "xml", "log", "kt", "java", "py", "html", "css", "js", "ini", "conf" -> FileCategory.DOCUMENTS
                "zip", "rar", "7z", "tar", "gz", "bz2", "xz" -> FileCategory.ARCHIVES
                "apk", "xapk", "apks" -> FileCategory.APKS
                else -> FileCategory.OTHER
            }
        }
    }
}

data class ExternalVolumeStats(
    val label: String,
    val path: String,
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val usedPercentage: Float
) {
    val totalFormatted: String get() = FileItem.formatFileSize(totalBytes)
    val usedFormatted: String get() = FileItem.formatFileSize(usedBytes)
    val freeFormatted: String get() = FileItem.formatFileSize(freeBytes)
}

data class StorageStats(
    val isAvailable: Boolean = false,
    val hasAllFilesPermission: Boolean = false,
    val totalBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedPercentage: Float = 0f,
    val categorySizes: Map<FileCategory, Long> = emptyMap(),
    val categoryCounts: Map<FileCategory, Int> = emptyMap(),
    val totalRootItems: Int = 0,
    val sdCardStats: ExternalVolumeStats? = null
) {
    val totalFormatted: String get() = if (isAvailable && totalBytes > 0) FileItem.formatFileSize(totalBytes) else "Unavailable"
    val usedFormatted: String get() = if (isAvailable && totalBytes > 0) FileItem.formatFileSize(usedBytes) else "Unavailable"
    val freeFormatted: String get() = if (isAvailable && totalBytes > 0) FileItem.formatFileSize(freeBytes) else "Unavailable"
}

enum class ClipboardAction {
    COPY, CUT
}

data class ClipboardItem(
    val action: ClipboardAction,
    val files: List<FileItem>
)

enum class ViewMode {
    GRID, LIST
}

enum class SortBy {
    NAME, DATE, SIZE, TYPE
}

enum class SortOrder {
    ASCENDING, DESCENDING
}

data class RamStats(
    val isAvailable: Boolean = false,
    val totalBytes: Long = 0L,
    val availBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercentage: Float = 0f
) {
    val totalFormatted: String get() = if (isAvailable && totalBytes > 0) FileItem.formatFileSize(totalBytes) else "Unavailable"
    val usedFormatted: String get() = if (isAvailable && totalBytes > 0) FileItem.formatFileSize(usedBytes) else "Unavailable"
    val availFormatted: String get() = if (isAvailable && totalBytes > 0) FileItem.formatFileSize(availBytes) else "Unavailable"
}
