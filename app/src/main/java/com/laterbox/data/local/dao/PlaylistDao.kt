package com.laterbox.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.laterbox.data.local.entity.ItemPlaylistCrossRef
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.data.local.relation.PlaylistWithCount
import com.laterbox.data.local.relation.PlaylistWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE isDefaultInbox = 1 LIMIT 1")
    suspend fun getDefaultInbox(): PlaylistEntity?

    @Query("SELECT * FROM playlists ORDER BY isDefaultInbox DESC, name COLLATE NOCASE ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("""
        SELECT p.id, p.name, p.description, p.iconName, p.coverUrl, p.createdAt, p.isDefaultInbox,
               COUNT(s.id) AS itemCount
        FROM playlists p
        LEFT JOIN item_playlist_cross_ref c ON p.id = c.playlistId
        LEFT JOIN saved_items s ON c.contentId = s.id AND s.isDeleted = 0
        GROUP BY p.id
        ORDER BY p.isDefaultInbox DESC, p.name COLLATE NOCASE ASC
    """)
    fun getAllPlaylistsWithCount(): Flow<List<PlaylistWithCount>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    fun getPlaylistWithItems(id: Long): Flow<PlaylistWithItems?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: ItemPlaylistCrossRef)

    @Query("DELETE FROM item_playlist_cross_ref WHERE contentId = :contentId AND playlistId = :playlistId")
    suspend fun deleteCrossRef(contentId: Long, playlistId: Long)

    @Query("DELETE FROM item_playlist_cross_ref WHERE contentId = :contentId")
    suspend fun deleteAllCrossRefsForContent(contentId: Long)

    @Query("""
        SELECT p.* FROM playlists p
        INNER JOIN item_playlist_cross_ref c ON p.id = c.playlistId
        WHERE c.contentId = :contentId
        ORDER BY p.name ASC
    """)
    fun getPlaylistsForItem(contentId: Long): Flow<List<PlaylistEntity>>

    @Query("""
        SELECT s.* FROM saved_items s
        INNER JOIN item_playlist_cross_ref c ON s.id = c.contentId
        WHERE c.playlistId = :playlistId AND s.isDeleted = 0
        ORDER BY c.addedAt DESC
    """)
    fun getItemsForPlaylist(playlistId: Long): Flow<List<com.laterbox.data.local.entity.SavedItemEntity>>
}
