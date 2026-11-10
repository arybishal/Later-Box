package com.laterbox.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.entity.TagEntity
import com.laterbox.data.local.relation.PlaylistWithCount
import com.laterbox.data.repository.ContentRepository
import com.laterbox.data.repository.PlaylistRepository
import com.laterbox.data.repository.TagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ItemDetailUiState(
    val item: SavedItemEntity? = null,
    val playlists: List<PlaylistEntity> = emptyList(),
    val tags: List<TagEntity> = emptyList(),
    val allPlaylists: List<PlaylistWithCount> = emptyList(),
    val isLoading: Boolean = true,
    val isNotFound: Boolean = false,
    val isDeleted: Boolean = false
)

class ItemDetailViewModel(
    val itemId: Long,
    private val contentRepository: ContentRepository = LaterBoxApp.instance.contentRepository,
    private val playlistRepository: PlaylistRepository = LaterBoxApp.instance.playlistRepository,
    private val tagRepository: TagRepository = LaterBoxApp.instance.tagRepository
) : ViewModel() {

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    val allPlaylists: StateFlow<List<PlaylistWithCount>> = playlistRepository.getAllPlaylistsWithCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<ItemDetailUiState> = combine(
        contentRepository.getItemWithPlaylists(itemId),
        tagRepository.getTagsForItem(itemId),
        allPlaylists
    ) { itemWithPlaylists, tags, allPlaylistsList ->
        if (itemWithPlaylists == null) {
            ItemDetailUiState(
                isLoading = false,
                isNotFound = true,
                allPlaylists = allPlaylistsList
            )
        } else {
            ItemDetailUiState(
                item = itemWithPlaylists.item,
                playlists = itemWithPlaylists.playlists,
                tags = tags,
                allPlaylists = allPlaylistsList,
                isLoading = false,
                isNotFound = false,
                isDeleted = itemWithPlaylists.item.isDeleted
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ItemDetailUiState(isLoading = true))

    init {
        // Automatically record last opened timestamp when viewing detail
        recordLastOpened()
    }

    fun recordLastOpened() {
        viewModelScope.launch {
            contentRepository.updateLastOpened(itemId)
        }
    }

    fun toggleFavorite() {
        val currentItem = uiState.value.item ?: return
        viewModelScope.launch {
            contentRepository.toggleFavorite(currentItem.id, currentItem.isFavorite)
        }
    }

    fun toggleArchive() {
        val currentItem = uiState.value.item ?: return
        viewModelScope.launch {
            contentRepository.toggleArchive(currentItem.id, currentItem.isArchived)
        }
    }

    fun updateNotes(notes: String?) {
        viewModelScope.launch {
            contentRepository.updateNotes(itemId, notes?.trim()?.ifEmpty { null })
        }
    }

    fun updatePlaylists(selectedPlaylistIds: List<Long>) {
        viewModelScope.launch {
            playlistRepository.updatePlaylistsForItem(itemId, selectedPlaylistIds)
        }
    }

    fun addTag(rawTag: String) {
        val clean = rawTag.trim().removePrefix("#")
        if (clean.isBlank()) return
        viewModelScope.launch {
            val tagId = tagRepository.createOrGetTag(clean)
            tagRepository.addTagToItem(itemId, tagId)
        }
    }

    fun removeTag(tagId: Long) {
        viewModelScope.launch {
            tagRepository.removeTagFromItem(itemId, tagId)
        }
    }

    fun moveToTrash(onCompleted: () -> Unit) {
        viewModelScope.launch {
            contentRepository.softDelete(itemId)
            onCompleted()
        }
    }

    fun restoreFromTrash() {
        viewModelScope.launch {
            contentRepository.restoreFromTrash(itemId)
        }
    }
}

class ItemDetailViewModelFactory(
    private val itemId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemDetailViewModel::class.java)) {
            return ItemDetailViewModel(itemId = itemId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
