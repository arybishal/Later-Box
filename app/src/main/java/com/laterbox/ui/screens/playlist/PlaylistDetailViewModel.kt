package com.laterbox.ui.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.repository.ContentRepository
import com.laterbox.data.repository.PlaylistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    private val playlistId: Long,
    private val playlistRepository: PlaylistRepository = LaterBoxApp.instance.playlistRepository,
    private val contentRepository: ContentRepository = LaterBoxApp.instance.contentRepository
) : ViewModel() {

    private val _playlist = MutableStateFlow<PlaylistEntity?>(null)
    val playlist: StateFlow<PlaylistEntity?> = _playlist.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            _playlist.value = playlistRepository.getPlaylistById(playlistId)
        }
    }

    private val rawItems: StateFlow<List<SavedItemEntity>> = playlistRepository.getItemsForPlaylist(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredItems: StateFlow<List<SavedItemEntity>> = combine(rawItems, _searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.url.contains(query, ignoreCase = true) ||
            (it.sourceDomain?.contains(query, ignoreCase = true) == true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun removeItemFromPlaylist(contentId: Long) {
        viewModelScope.launch {
            playlistRepository.removeItemFromPlaylist(contentId, playlistId)
        }
    }

    fun renamePlaylist(newName: String, newDesc: String?) {
        viewModelScope.launch {
            val current = _playlist.value ?: return@launch
            val updated = current.copy(name = newName.trim(), description = newDesc?.trim())
            playlistRepository.updatePlaylist(updated)
            _playlist.value = updated
        }
    }

    fun deletePlaylist(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val current = _playlist.value ?: return@launch
            playlistRepository.deletePlaylist(current)
            onDeleted()
        }
    }

    fun toggleFavorite(item: SavedItemEntity) {
        viewModelScope.launch {
            contentRepository.toggleFavorite(item.id, item.isFavorite)
        }
    }

    fun moveToTrash(itemId: Long) {
        viewModelScope.launch {
            contentRepository.softDelete(itemId)
        }
    }
}
