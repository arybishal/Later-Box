package com.laterbox.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.preferences.UserPreferencesRepository
import com.laterbox.data.model.ContentType
import com.laterbox.data.model.SortOrder
import com.laterbox.data.model.ViewMode
import com.laterbox.data.repository.ContentRepository
import com.laterbox.data.repository.PlaylistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryStatusFilter(val label: String) {
    ALL("All"),
    SNAP("Snap"),
    FAVORITES("Favorites"),
    ARCHIVE("Archive")
}

class LibraryViewModel(
    private val contentRepository: ContentRepository = LaterBoxApp.instance.contentRepository,
    private val playlistRepository: PlaylistRepository = LaterBoxApp.instance.playlistRepository,
    private val preferencesRepository: UserPreferencesRepository = LaterBoxApp.instance.preferencesRepository
) : ViewModel() {

    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    val sortOrder: StateFlow<SortOrder> = preferencesRepository.sortOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOrder.NEWEST)

    private val _selectedType = MutableStateFlow<ContentType?>(null)
    val selectedType: StateFlow<ContentType?> = _selectedType.asStateFlow()

    private val _statusFilter = MutableStateFlow(LibraryStatusFilter.ALL)
    val statusFilter: StateFlow<LibraryStatusFilter> = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.viewMode.collect { savedMode ->
                _viewMode.value = savedMode
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<SavedItemEntity>> = combine(
        sortOrder,
        _statusFilter,
        _selectedType,
        _searchQuery
    ) { sort, status, type, query ->
        Params(sort, status, type, query)
    }.flatMapLatest { params ->
        val baseFlow = when (params.status) {
            LibraryStatusFilter.ALL -> {
                if (params.query.isNotBlank()) contentRepository.searchItems(params.query)
                else contentRepository.getItems(params.sort)
            }
            LibraryStatusFilter.SNAP -> contentRepository.getSnapItems()
            LibraryStatusFilter.FAVORITES -> contentRepository.getFavoriteItems()
            LibraryStatusFilter.ARCHIVE -> contentRepository.getArchivedItems()
        }

        baseFlow.combine(MutableStateFlow(params)) { list, p ->
            list.filter { item ->
                (p.type == null || item.contentType == p.type) &&
                (p.query.isBlank() || item.title.contains(p.query, ignoreCase = true) ||
                 item.url.contains(p.query, ignoreCase = true) ||
                 (item.sourceDomain?.contains(p.query, ignoreCase = true) == true) ||
                 (item.notes?.contains(p.query, ignoreCase = true) == true))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class Params(
        val sort: SortOrder,
        val status: LibraryStatusFilter,
        val type: ContentType?,
        val query: String
    )

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
        viewModelScope.launch {
            preferencesRepository.setViewMode(mode)
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            preferencesRepository.setSortOrder(order)
        }
    }

    fun setTypeFilter(type: ContentType?) {
        _selectedType.value = type
    }

    fun setStatusFilter(status: LibraryStatusFilter) {
        _statusFilter.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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

    fun updateNotes(itemId: Long, notes: String?) {
        viewModelScope.launch {
            contentRepository.updateNotes(itemId, notes)
        }
    }

    val allPlaylists = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updatePlaylistsForItem(itemId: Long, playlistIds: List<Long>) {
        viewModelScope.launch {
            playlistRepository.updatePlaylistsForItem(itemId, playlistIds)
        }
    }

    fun createPlaylist(name: String, description: String?, iconName: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name, description, iconName)
        }
    }
}
