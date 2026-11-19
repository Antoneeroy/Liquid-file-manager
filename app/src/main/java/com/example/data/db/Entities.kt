package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trash_items")
data class TrashEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalPath: String,
    val fileName: String,
    val size: Long,
    val deletedAt: Long = System.currentTimeMillis(),
    val isDirectory: Boolean = false
)

@Entity(tableName = "vault_items")
data class VaultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalName: String,
    val encryptedFileName: String,
    val originalSize: Long,
    val mimeType: String,
    val vaultedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_items")
data class FavoriteEntity(
    @PrimaryKey val path: String,
    val addedAt: Long = System.currentTimeMillis()
)
