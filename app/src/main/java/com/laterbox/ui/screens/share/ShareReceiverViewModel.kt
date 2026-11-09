package com.laterbox.ui.screens.share

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import com.laterbox.data.network.MetadataExtractor
import com.laterbox.data.repository.ContentRepository
import com.laterbox.data.repository.PlaylistRepository
import com.laterbox.data.storage.MediaStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SharedContentItem(
    val rawText: String? = null,
    val extractedUrl: String? = null,
    val mediaUri: Uri? = null,
    val mimeType: String? = null,
    val title: String = "",
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val domain: String? = null,
    val contentType: ContentType = ContentType.LINK,
    val isFetchingMetadata: Boolean = false
)

class ShareReceiverViewModel(
    private val contentRepository: ContentRepository = LaterBoxApp.instance.contentRepository,
    private val playlistRepository: PlaylistRepository = LaterBoxApp.instance.playlistRepository,
    private val mediaStorageManager: MediaStorageManager = LaterBoxApp.instance.mediaStorageManager
) : ViewModel() {

    private val _items = MutableStateFlow<List<SharedContentItem>>(emptyList())
    val items: StateFlow<List<SharedContentItem>> = _items.asStateFlow()

    private val _selectedPlaylistIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedPlaylistIds: StateFlow<Set<Long>> = _selectedPlaylistIds.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    val playlists: StateFlow<List<PlaylistEntity>> = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun processSharedIntent(
        text: String?,
        streamUris: List<Uri>,
        mimeType: String?
    ) {
        val parsedItems = mutableListOf<SharedContentItem>()

        if (streamUris.isNotEmpty()) {
            for (uri in streamUris) {
                val detectedType = ContentType.fromMimeOrUrl(mimeType, uri.toString())
                parsedItems.add(
                    SharedContentItem(
                        mediaUri = uri,
                        mimeType = mimeType,
                        title = uri.lastPathSegment ?: "Shared Media",
                        contentType = detectedType
                    )
                )
            }
        } else if (!text.isNullOrBlank()) {
            val url = MetadataExtractor.extractUrlFromText(text)
            if (url != null) {
                val domain = MetadataExtractor.extractDomain(url)
                val detectedType = ContentType.fromMimeOrUrl(mimeType, url)
                parsedItems.add(
                    SharedContentItem(
                        rawText = text,
                        extractedUrl = url,
                        mimeType = mimeType,
                        title = domain.replaceFirstChar { it.uppercase() },
                        domain = domain,
                        contentType = detectedType,
                        isFetchingMetadata = true
                    )
                )
            } else {
                parsedItems.add(
                    SharedContentItem(
                        rawText = text,
                        mimeType = mimeType,
                        title = text.take(50).trim(),
                        contentType = ContentType.NOTE
                    )
                )
            }
        }

        _items.value = parsedItems

        // Extract metadata in parallel for URLs
        parsedItems.forEachIndexed { index, item ->
            val u = item.extractedUrl
            if (u != null) {
                viewModelScope.launch {
                    try {
                        val meta = MetadataExtractor.extractMetadata(u)
                        val updatedList = _items.value.toMutableList()
                        if (index < updatedList.size) {
                            updatedList[index] = updatedList[index].copy(
                                title = meta.title,
                                description = meta.description,
                                thumbnailUrl = meta.thumbnailUrl,
                                domain = meta.sourceDomain,
                                contentType = meta.contentType,
                                isFetchingMetadata = false
                            )
                            _items.value = updatedList
                        }
                    } catch (_: Exception) {
                        val updatedList = _items.value.toMutableList()
                        if (index < updatedList.size) {
                            updatedList[index] = updatedList[index].copy(isFetchingMetadata = false)
                            _items.value = updatedList
                        }
                    }
                }
            }
        }
    }

    fun togglePlaylist(playlistId: Long) {
        val current = _selectedPlaylistIds.value.toMutableSet()
        if (current.contains(playlistId)) current.remove(playlistId)
        else current.add(playlistId)
        _selectedPlaylistIds.value = current
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }

    fun createPlaylist(name: String, description: String?, iconName: String) {
        viewModelScope.launch {
            val id = playlistRepository.createPlaylist(name, description, iconName)
            val current = _selectedPlaylistIds.value.toMutableSet()
            current.add(id)
            _selectedPlaylistIds.value = current
        }
    }

    fun saveDirectlyToInbox(onComplete: () -> Unit) {
        saveInternal(playlistIds = emptyList(), onComplete = onComplete)
    }

    fun saveToSelectedPlaylists(onComplete: () -> Unit) {
        saveInternal(playlistIds = _selectedPlaylistIds.value.toList(), onComplete = onComplete)
    }

    private fun saveInternal(playlistIds: List<Long>, onComplete: () -> Unit) {
        viewModelScope.launch {
            val userNotes = _notes.value.trim().ifEmpty { null }

            for (item in _items.value) {
                var localPath: String? = null
                var mediaSize = 0L

                // If media stream was received, persist to app internal storage
                if (item.mediaUri != null) {
                    try {
                        val saved = mediaStorageManager.saveSharedUriToInternalStorage(item.mediaUri, item.mimeType)
                        localPath = saved.first
                        mediaSize = saved.second
                    } catch (_: Exception) {}
                }

                val finalUrl = item.extractedUrl ?: item.mediaUri?.toString() ?: item.rawText ?: ""
                val savedItem = SavedItemEntity(
                    url = finalUrl,
                    title = item.title.ifBlank { finalUrl },
                    description = item.description,
                    sourceDomain = item.domain,
                    thumbnailUrl = item.thumbnailUrl,
                    localMediaPath = localPath,
                    contentType = item.contentType,
                    mimeType = item.mimeType,
                    notes = userNotes,
                    mediaSize = mediaSize
                )

                contentRepository.saveItem(
                    item = savedItem,
                    playlistIds = playlistIds,
                    fetchMetadataAsync = item.extractedUrl != null && item.title.isBlank()
                )
            }

            _isSaved.value = true
            onComplete()
        }
    }
}
