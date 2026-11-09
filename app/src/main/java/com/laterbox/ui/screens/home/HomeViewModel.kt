package com.laterbox.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.relation.PlaylistWithCount
import com.laterbox.data.model.ContentType
import com.laterbox.data.repository.ContentRepository
import com.laterbox.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val recentItems: List<SavedItemEntity> = emptyList(),
    val continueItems: List<SavedItemEntity> = emptyList(),
    val playlists: List<PlaylistWithCount> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<SavedItemEntity> = emptyList(),
    val isSearching: Boolean = false
)

class HomeViewModel(
    private val contentRepository: ContentRepository = LaterBoxApp.instance.contentRepository,
    private val playlistRepository: PlaylistRepository = LaterBoxApp.instance.playlistRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SavedItemEntity>>(emptyList())
    val searchResults: StateFlow<List<SavedItemEntity>> = _searchResults.asStateFlow()

    val recentItems: StateFlow<List<SavedItemEntity>> = contentRepository.getRecentItems(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueItems: StateFlow<List<SavedItemEntity>> = contentRepository.getContinueItems(6)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistWithCount>> = playlistRepository.getAllPlaylistsWithCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteCount: StateFlow<Int> = contentRepository.getFavoriteCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            viewModelScope.launch {
                contentRepository.searchItems(query).collect {
                    _searchResults.value = it
                }
            }
        }
    }

    fun toggleFavorite(item: SavedItemEntity) {
        viewModelScope.launch {
            contentRepository.toggleFavorite(item.id, item.isFavorite)
        }
    }

    fun toggleArchive(item: SavedItemEntity) {
        viewModelScope.launch {
            contentRepository.toggleArchive(item.id, item.isArchived)
        }
    }

    fun moveToTrash(itemId: Long) {
        viewModelScope.launch {
            contentRepository.softDelete(itemId)
        }
    }

    fun saveManualItem(urlOrText: String, title: String, notes: String?) {
        viewModelScope.launch {
            val type = ContentType.fromMimeOrUrl(null, urlOrText)
            val domain = try {
                if (urlOrText.startsWith("http")) java.net.URI(urlOrText).host?.removePrefix("www.") else null
            } catch (_: Exception) { null }

            val item = SavedItemEntity(
                url = urlOrText,
                title = title,
                sourceDomain = domain,
                contentType = type,
                notes = notes
            )
            contentRepository.saveItem(item)
        }
    }

    fun createPlaylist(name: String, description: String?, iconName: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name, description, iconName)
        }
    }

    fun updateNotes(itemId: Long, notes: String?) {
        viewModelScope.launch {
            contentRepository.updateNotes(itemId, notes)
        }
    }

    fun updatePlaylistsForItem(itemId: Long, playlistIds: List<Long>) {
        viewModelScope.launch {
            playlistRepository.updatePlaylistsForItem(itemId, playlistIds)
        }
    }

    fun getPlaylistsForItem(itemId: Long) = playlistRepository.getPlaylistsForItem(itemId)
}
