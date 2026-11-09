package com.laterbox.data.repository

import com.laterbox.data.local.dao.PlaylistDao
import com.laterbox.data.local.entity.ItemPlaylistCrossRef
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.relation.PlaylistWithCount
import com.laterbox.data.local.relation.PlaylistWithItems
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(
    private val playlistDao: PlaylistDao
) {

    fun getAllPlaylistsWithCount(): Flow<List<PlaylistWithCount>> = playlistDao.getAllPlaylistsWithCount()

    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun getPlaylistById(id: Long): PlaylistEntity? = playlistDao.getPlaylistById(id)

    suspend fun getDefaultInbox(): PlaylistEntity? = playlistDao.getDefaultInbox()

    fun getPlaylistWithItems(id: Long): Flow<PlaylistWithItems?> = playlistDao.getPlaylistWithItems(id)

    fun getItemsForPlaylist(playlistId: Long): Flow<List<SavedItemEntity>> = playlistDao.getItemsForPlaylist(playlistId)

    fun getPlaylistsForItem(contentId: Long): Flow<List<PlaylistEntity>> = playlistDao.getPlaylistsForItem(contentId)

    suspend fun createPlaylist(name: String, description: String? = null, iconName: String = "bookmark"): Long {
        val playlist = PlaylistEntity(
            name = name.trim(),
            description = description?.trim(),
            iconName = iconName,
            isDefaultInbox = false
        )
        return playlistDao.insertPlaylist(playlist)
    }

    suspend fun updatePlaylist(playlist: PlaylistEntity) {
        playlistDao.updatePlaylist(playlist)
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) {
        // Only non-inbox playlists can be deleted
        if (!playlist.isDefaultInbox) {
            playlistDao.deletePlaylist(playlist)
        }
    }

    suspend fun addItemToPlaylists(contentId: Long, playlistIds: List<Long>) {
        for (pId in playlistIds) {
            playlistDao.insertCrossRef(ItemPlaylistCrossRef(contentId = contentId, playlistId = pId))
        }
    }

    suspend fun removeItemFromPlaylist(contentId: Long, playlistId: Long) {
        playlistDao.deleteCrossRef(contentId, playlistId)
    }

    suspend fun updatePlaylistsForItem(contentId: Long, playlistIds: List<Long>) {
        playlistDao.deleteAllCrossRefsForContent(contentId)
        addItemToPlaylists(contentId, playlistIds)
    }
}
