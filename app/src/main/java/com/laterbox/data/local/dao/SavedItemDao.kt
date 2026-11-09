package com.laterbox.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.relation.SavedItemWithPlaylists
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SavedItemEntity): Long

    @Update
    suspend fun updateItem(item: SavedItemEntity)

    @Delete
    suspend fun deleteItem(item: SavedItemEntity)

    @Query("SELECT * FROM saved_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): SavedItemEntity?

    @Query("SELECT * FROM saved_items WHERE url = :url AND isDeleted = 0 LIMIT 1")
    suspend fun getItemByUrl(url: String): SavedItemEntity?

    @Transaction
    @Query("SELECT * FROM saved_items WHERE id = :id LIMIT 1")
    fun getItemWithPlaylists(id: Long): Flow<SavedItemWithPlaylists?>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isArchived = 0 
        ORDER BY savedAt DESC
    """)
    fun getAllActiveItemsNewest(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isArchived = 0 
        ORDER BY savedAt ASC
    """)
    fun getAllActiveItemsOldest(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isArchived = 0 
        ORDER BY title COLLATE NOCASE ASC
    """)
    fun getAllActiveItemsTitle(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isArchived = 0 
        ORDER BY COALESCE(lastOpenedAt, savedAt) DESC
    """)
    fun getAllActiveItemsRecentlyOpened(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isArchived = 0 
        ORDER BY savedAt DESC 
        LIMIT :limit
    """)
    fun getRecentItems(limit: Int = 10): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isArchived = 0 
          AND (readProgress > 0.0 OR lastOpenedAt IS NOT NULL)
        ORDER BY COALESCE(lastOpenedAt, savedAt) DESC 
        LIMIT :limit
    """)
    fun getContinueItems(limit: Int = 6): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isFavorite = 1 
        ORDER BY savedAt DESC
    """)
    fun getFavoriteItems(): Flow<List<SavedItemEntity>>

    @Query("SELECT COUNT(*) FROM saved_items WHERE isDeleted = 0 AND isFavorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isArchived = 0 
          AND contentType IN ('ARTICLE', 'DOCUMENT') 
          AND readProgress > 0.0 AND readProgress < 1.0
        ORDER BY COALESCE(lastOpenedAt, savedAt) DESC
    """)
    fun getContinueReadingItems(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isArchived = 0 
          AND contentType = 'VIDEO' 
          AND (readProgress > 0.0 OR lastOpenedAt IS NOT NULL) 
          AND readProgress < 1.0
        ORDER BY COALESCE(lastOpenedAt, savedAt) DESC
    """)
    fun getContinueWatchingItems(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT s.* FROM saved_items s
        WHERE s.isDeleted = 0 AND s.isArchived = 0
          AND s.id NOT IN (
              SELECT c.contentId FROM item_playlist_cross_ref c
              INNER JOIN playlists p ON c.playlistId = p.id
              WHERE p.isDefaultInbox = 0
          )
        ORDER BY s.savedAt DESC
    """)
    fun getUnsortedItems(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isArchived = 0 
          AND localMediaPath IS NOT NULL AND localMediaPath != ''
        ORDER BY savedAt DESC
    """)
    fun getDownloadedItems(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isArchived = 0 
          AND lastOpenedAt IS NOT NULL
        ORDER BY lastOpenedAt DESC
    """)
    fun getRecentlyOpenedItems(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 AND isArchived = 1 
        ORDER BY savedAt DESC
    """)
    fun getArchivedItems(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 1 
        ORDER BY deletedAt DESC
    """)
    fun getTrashItems(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM saved_items 
        WHERE isDeleted = 0 
          AND (
            title LIKE '%' || :query || '%' 
            OR description LIKE '%' || :query || '%' 
            OR url LIKE '%' || :query || '%' 
            OR sourceDomain LIKE '%' || :query || '%'
            OR notes LIKE '%' || :query || '%'
          )
        ORDER BY savedAt DESC
    """)
    fun searchItems(query: String): Flow<List<SavedItemEntity>>

    @Query("UPDATE saved_items SET isDeleted = 1, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE saved_items SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("DELETE FROM saved_items WHERE id = :id")
    suspend fun permanentDelete(id: Long)

    @Query("DELETE FROM saved_items WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("UPDATE saved_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE saved_items SET isArchived = :isArchived WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean)

    @Query("UPDATE saved_items SET readProgress = :progress, lastOpenedAt = :lastOpened WHERE id = :id")
    suspend fun updateReadProgress(id: Long, progress: Float, lastOpened: Long = System.currentTimeMillis())

    @Query("UPDATE saved_items SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?)

    @Query("SELECT COUNT(*) FROM saved_items WHERE isDeleted = 0")
    fun getTotalActiveCount(): Flow<Int>

    @Query("SELECT * FROM saved_items")
    suspend fun getAllItemsSnapshot(): List<SavedItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SavedItemEntity>): List<Long>

    @Query("DELETE FROM saved_items")
    suspend fun clearAllItems()
}
