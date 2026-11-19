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
    val isProtected: Boolean = false
) {
    val formattedSize: String
        get() = formatFileSize(size)

    val isHidden: Boolean
        get() = name.startsWith(".")

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }

    val relativeTime: String
        get() {
            val diff = System.currentTimeMillis() - lastModified
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
            val ext = file.extension.lowercase()
            return when (ext) {
                "jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic" -> FileCategory.IMAGES
                "mp4", "mkv", "webm", "avi", "mov", "3gp", "flv" -> FileCategory.VIDEOS
                "mp3", "wav", "m4a", "flac", "aac", "ogg", "wma" -> FileCategory.AUDIO
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv", "json", "xml", "log" -> FileCategory.DOCUMENTS
                "zip", "rar", "7z", "tar", "gz", "bz2" -> FileCategory.ARCHIVES
                "apk", "xapk", "apks" -> FileCategory.APKS
                else -> FileCategory.OTHER
            }
        }
    }
}

data class StorageStats(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val usedPercentage: Float,
    val categorySizes: Map<FileCategory, Long> = emptyMap(),
    val categoryCounts: Map<FileCategory, Int> = emptyMap()
) {
    val totalFormatted: String get() = FileItem.formatFileSize(totalBytes)
    val usedFormatted: String get() = FileItem.formatFileSize(usedBytes)
    val freeFormatted: String get() = FileItem.formatFileSize(freeBytes)
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
    val totalBytes: Long,
    val availBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Float
) {
    val totalFormatted: String get() = FileItem.formatFileSize(totalBytes)
    val usedFormatted: String get() = FileItem.formatFileSize(usedBytes)
    val availFormatted: String get() = FileItem.formatFileSize(availBytes)
}
