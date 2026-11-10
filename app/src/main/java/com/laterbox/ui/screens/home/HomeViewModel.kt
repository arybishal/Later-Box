package com.laterbox.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laterbox.LaterBoxApp
import com.laterbox.data.ai.model.GeminiModels
import com.laterbox.data.ai.network.GeminiApiClient
import com.laterbox.data.ai.network.GeminiApiResult
import com.laterbox.data.ai.security.SecureApiKeyStorage
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.relation.PlaylistWithCount
import com.laterbox.data.model.ContentType
import com.laterbox.data.model.SortOrder
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

    val allItems: StateFlow<List<SavedItemEntity>> = contentRepository.getItems(SortOrder.NEWEST)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentItems: StateFlow<List<SavedItemEntity>> = contentRepository.getRecentItems(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueItems: StateFlow<List<SavedItemEntity>> = contentRepository.getContinueItems(6)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistWithCount>> = playlistRepository.getAllPlaylistsWithCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteCount: StateFlow<Int> = contentRepository.getFavoriteCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCount: StateFlow<Int> = contentRepository.getTotalActiveCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isAiEnabled: StateFlow<Boolean> = LaterBoxApp.instance.aiSettingsRepository.isAiEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _isAiQuerying = MutableStateFlow(false)
    val isAiQuerying: StateFlow<Boolean> = _isAiQuerying.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

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

    fun clearAiResponse() {
        _aiResponse.value = null
        _aiError.value = null
        _isAiQuerying.value = false
    }

    fun askGemini(question: String) {
        val cleanQuestion = question.trim()
        if (cleanQuestion.isBlank()) return

        viewModelScope.launch {
            _isAiQuerying.value = true
            _aiError.value = null
            _aiResponse.value = null

            try {
                val apiKey = SecureApiKeyStorage.getInstance(LaterBoxApp.instance).getApiKey()
                if (apiKey.isNullOrBlank()) {
                    _aiError.value = "Gemini API key is not configured. Add your API key in Settings."
                    _isAiQuerying.value = false
                    return@launch
                }

                val modelId: String = LaterBoxApp.instance.aiSettingsRepository.getSelectedModelIdNow().ifBlank {
                    GeminiModels.DEFAULT_MODEL_ID
                }

                val currentItems = recentItems.value
                val contextBuilder = StringBuilder()
                contextBuilder.append("User's Saved Memories / Content Feed (sample of latest items):\n")
                if (currentItems.isNotEmpty()) {
                    currentItems.take(12).forEachIndexed { index, item ->
                        contextBuilder.append("${index + 1}. Title: \"${item.title}\" | Type: ${item.contentType.displayName}")
                        item.sourceDomain?.let { contextBuilder.append(" | Source: $it") }
                        item.description?.take(150)?.let { contextBuilder.append(" | Summary: $it") }
                        item.notes?.take(100)?.let { contextBuilder.append(" | Note: $it") }
                        contextBuilder.append("\n")
                    }
                } else {
                    contextBuilder.append("No saved items yet.\n")
                }

                val prompt = """
                    You are the Later Box personal AI assistant inside the Android app Later Box.
                    The user asks: "$cleanQuestion"

                    $contextBuilder

                    Provide a helpful, concise, well-structured, and friendly answer addressing their question directly in relation to their saved items or general content advice. Keep formatting clean and readable without markdown clutter.
                """.trimIndent()

                val client = GeminiApiClient()
                when (val result = client.generateContentText(apiKey, modelId, prompt)) {
                    is GeminiApiResult.Success -> {
                        _aiResponse.value = result.data
                    }
                    is GeminiApiResult.Error -> {
                        _aiError.value = result.message
                    }
                }
            } catch (e: Exception) {
                _aiError.value = "Failed to query Gemini: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isAiQuerying.value = false
            }
        }
    }
}
