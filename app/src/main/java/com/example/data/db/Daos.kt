package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashDao {
    @Query("SELECT * FROM trash_items ORDER BY deletedAt DESC")
    fun getAllTrash(): Flow<List<TrashEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TrashEntity): Long

    @Delete
    suspend fun delete(item: TrashEntity)

    @Query("DELETE FROM trash_items")
    suspend fun clearAll()
}

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items ORDER BY vaultedAt DESC")
    fun getAllVaultItems(): Flow<List<VaultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VaultEntity): Long

    @Delete
    suspend fun delete(item: VaultEntity)

    @Query("SELECT COUNT(*) FROM vault_items")
    fun getVaultCount(): Flow<Int>
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_items")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT path FROM favorite_items")
    suspend fun getAllFavoritePaths(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FavoriteEntity)

    @Query("DELETE FROM favorite_items WHERE path = :path")
    suspend fun remove(path: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_items WHERE path = :path)")
    suspend fun isFavorite(path: String): Boolean
}
