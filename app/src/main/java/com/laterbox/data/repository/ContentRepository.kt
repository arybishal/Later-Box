package com.laterbox.data.repository

import com.laterbox.data.local.dao.PlaylistDao
import com.laterbox.data.local.dao.SavedItemDao
import com.laterbox.data.local.entity.ItemPlaylistCrossRef
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.relation.SavedItemWithPlaylists
import com.laterbox.data.model.ContentType
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

    fun getFavoriteCount(): Flow<Int> = savedItemDao.getFavoriteCount()

    fun getTotalActiveCount(): Flow<Int> = savedItemDao.getTotalActiveCount()

    fun getArchivedItems(): Flow<List<SavedItemEntity>> = savedItemDao.getArchivedItems()

    fun getTrashItems(): Flow<List<SavedItemEntity>> = savedItemDao.getTrashItems()

    fun searchItems(query: String): Flow<List<SavedItemEntity>> = savedItemDao.searchItems(query)

    suspend fun getItemById(id: Long): SavedItemEntity? = savedItemDao.getItemById(id)

    suspend fun getItemByUrl(url: String): SavedItemEntity? = savedItemDao.getItemByUrl(url)

    fun getItemWithPlaylists(id: Long): Flow<SavedItemWithPlaylists?> = savedItemDao.getItemWithPlaylists(id)

    suspend fun saveContent(
        url: String,
        title: String? = null,
        notes: String? = null,
        playlistIds: List<Long> = emptyList()
    ): Long {
        val cleanUrl = url.trim()
        val cleanTitle = title?.trim()?.ifEmpty { null } ?: cleanUrl
        val item = SavedItemEntity(
            url = cleanUrl,
            title = cleanTitle,
            notes = notes?.trim()?.ifEmpty { null },
            contentType = ContentType.fromMimeOrUrl(null, cleanUrl)
        )
        return saveItem(item, playlistIds, fetchMetadataAsync = true)
    }

    suspend fun saveItem(
        item: SavedItemEntity,
        playlistIds: List<Long> = emptyList(),
        fetchMetadataAsync: Boolean = true
    ): Long {
        // Resolve target playlists (default to Inbox if none selected)
        val finalPlaylists = if (playlistIds.isEmpty()) {
            val inbox = playlistDao.getDefaultInbox()
            if (inbox != null) listOf(inbox.id) else emptyList()
        } else {
            playlistIds
        }

        // Duplicate URL Detection
        if (item.url.startsWith("http://") || item.url.startsWith("https://")) {
            val existing = savedItemDao.getItemByUrl(item.url)
            if (existing != null) {
                // Link existing item to newly selected playlists
                for (pId in finalPlaylists) {
                    playlistDao.insertCrossRef(ItemPlaylistCrossRef(contentId = existing.id, playlistId = pId))
                }
                if (!item.notes.isNullOrBlank()) {
                    val mergedNotes = if (existing.notes.isNullOrBlank()) item.notes else "${existing.notes}\n${item.notes}"
                    savedItemDao.updateNotes(existing.id, mergedNotes)
                }
                return existing.id
            }
        }

        val insertedId = savedItemDao.insertItem(item)

        for (pId in finalPlaylists) {
            playlistDao.insertCrossRef(ItemPlaylistCrossRef(contentId = insertedId, playlistId = pId))
        }

        if (fetchMetadataAsync && (item.url.startsWith("http://") || item.url.startsWith("https://"))) {
            externalScope.launch {
                try {
                    val meta = MetadataExtractor.extractMetadata(item.url)
                    val current = savedItemDao.getItemById(insertedId)
                    if (current != null) {
                        val isGenericTitle = current.title == current.url ||
                                current.title.isBlank() ||
                                current.title.equals(current.sourceDomain, ignoreCase = true) ||
                                current.title.equals(meta.sourceDomain, ignoreCase = true) ||
                                current.title.equals(meta.sourceApp, ignoreCase = true) ||
                                current.title.equals("Saved Link", ignoreCase = true)

                        val updated = current.copy(
                            title = if (isGenericTitle) meta.title else current.title,
                            description = current.description ?: meta.description,
                            thumbnailUrl = current.thumbnailUrl ?: meta.thumbnailUrl,
                            sourceDomain = current.sourceDomain ?: meta.sourceDomain,
                            sourceApp = current.sourceApp ?: meta.sourceApp,
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
        savedItemDao.emptyTrash()
    }

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        savedItemDao.setFavorite(id, !currentStatus)
    }

    suspend fun toggleArchive(id: Long, currentStatus: Boolean) {
        savedItemDao.setArchived(id, !currentStatus)
    }

    suspend fun updateReadProgress(id: Long, progress: Float) {
        savedItemDao.updateReadProgress(id, progress, lastOpened = System.currentTimeMillis())
    }

    suspend fun updateLastOpened(id: Long) {
        val current = savedItemDao.getItemById(id) ?: return
        savedItemDao.updateReadProgress(id, current.readProgress, lastOpened = System.currentTimeMillis())
    }

    suspend fun updateNotes(id: Long, notes: String?) {
        savedItemDao.updateNotes(id, notes)
    }
}
