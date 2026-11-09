package com.laterbox.data.repository

import com.laterbox.data.local.dao.PlaylistDao
import com.laterbox.data.local.dao.SavedItemDao
import com.laterbox.data.local.entity.ItemPlaylistCrossRef
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.relation.SavedItemWithPlaylists
import com.laterbox.data.model.SortOrder
import com.laterbox.data.network.MetadataExtractor
import com.laterbox.data.storage.MediaStorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ContentRepository(
    private val savedItemDao: SavedItemDao,
    private val playlistDao: PlaylistDao,
    private val mediaStorageManager: MediaStorageManager,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    fun getItems(sortOrder: SortOrder): Flow<List<SavedItemEntity>> {
        return when (sortOrder) {
            SortOrder.NEWEST -> savedItemDao.getAllActiveItemsNewest()
            SortOrder.OLDEST -> savedItemDao.getAllActiveItemsOldest()
            SortOrder.TITLE_AZ -> savedItemDao.getAllActiveItemsTitle()
            SortOrder.LAST_OPENED -> savedItemDao.getAllActiveItemsRecentlyOpened()
        }
    }

    fun getRecentItems(limit: Int = 10): Flow<List<SavedItemEntity>> = savedItemDao.getRecentItems(limit)

    fun getContinueItems(limit: Int = 6): Flow<List<SavedItemEntity>> = savedItemDao.getContinueItems(limit)

    fun getFavoriteItems(): Flow<List<SavedItemEntity>> = savedItemDao.getFavoriteItems()

    fun getArchivedItems(): Flow<List<SavedItemEntity>> = savedItemDao.getArchivedItems()

    fun getTrashItems(): Flow<List<SavedItemEntity>> = savedItemDao.getTrashItems()

    fun searchItems(query: String): Flow<List<SavedItemEntity>> = savedItemDao.searchItems(query)

    suspend fun getItemById(id: Long): SavedItemEntity? = savedItemDao.getItemById(id)

    suspend fun getItemByUrl(url: String): SavedItemEntity? = savedItemDao.getItemByUrl(url)

    fun getItemWithPlaylists(id: Long): Flow<SavedItemWithPlaylists?> = savedItemDao.getItemWithPlaylists(id)

    suspend fun saveItem(
        item: SavedItemEntity,
        playlistIds: List<Long> = emptyList(),
        fetchMetadataAsync: Boolean = true
    ): Long {
        val insertedId = savedItemDao.insertItem(item)

        // If no playlists selected, assign to Inbox
        val finalPlaylists = if (playlistIds.isEmpty()) {
            val inbox = playlistDao.getDefaultInbox()
            if (inbox != null) listOf(inbox.id) else emptyList()
        } else {
            playlistIds
        }

        for (pId in finalPlaylists) {
            playlistDao.insertCrossRef(ItemPlaylistCrossRef(contentId = insertedId, playlistId = pId))
        }

        if (fetchMetadataAsync && (item.url.startsWith("http://") || item.url.startsWith("https://"))) {
            externalScope.launch {
                try {
                    val meta = MetadataExtractor.extractMetadata(item.url)
                    val current = savedItemDao.getItemById(insertedId)
                    if (current != null) {
                        val updated = current.copy(
                            title = if (current.title == current.url || current.title.isBlank()) meta.title else current.title,
                            description = current.description ?: meta.description,
                            thumbnailUrl = current.thumbnailUrl ?: meta.thumbnailUrl,
                            sourceDomain = current.sourceDomain ?: meta.sourceDomain,
                            contentType = if (current.contentType == com.laterbox.data.model.ContentType.LINK) meta.contentType else current.contentType
                        )
                        savedItemDao.updateItem(updated)
                    }
                } catch (_: Exception) {}
            }
        }

        return insertedId
    }

    suspend fun updateItem(item: SavedItemEntity) {
        savedItemDao.updateItem(item)
    }

    suspend fun softDelete(id: Long) {
        savedItemDao.softDelete(id)
    }

    suspend fun restoreFromTrash(id: Long) {
        savedItemDao.restoreFromTrash(id)
    }

    suspend fun permanentDelete(id: Long) {
        val item = savedItemDao.getItemById(id)
        if (item != null && item.localMediaPath != null) {
            mediaStorageManager.deleteLocalMedia(item.localMediaPath)
        }
        savedItemDao.permanentDelete(id)
    }

    suspend fun emptyTrash() {
        val trashed = savedItemDao.getItemById(-1) // triggers cleanup logic if needed
        savedItemDao.emptyTrash()
    }

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        savedItemDao.setFavorite(id, !currentStatus)
    }

    suspend fun toggleArchive(id: Long, currentStatus: Boolean) {
        savedItemDao.setArchived(id, !currentStatus)
    }

    suspend fun updateReadProgress(id: Long, progress: Float) {
        savedItemDao.updateReadProgress(id, progress)
    }

    suspend fun updateNotes(id: Long, notes: String?) {
        savedItemDao.updateNotes(id, notes)
    }
}
