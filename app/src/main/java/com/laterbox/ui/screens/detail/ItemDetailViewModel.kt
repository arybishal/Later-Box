package com.laterbox.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laterbox.LaterBoxApp
import com.laterbox.data.ai.AiSettingsRepository
import com.laterbox.data.ai.GeminiAnalysisService
import com.laterbox.data.ai.model.GeminiModels
import com.laterbox.data.ai.network.GeminiApiClient
import com.laterbox.data.ai.network.GeminiApiResult
import com.laterbox.data.ai.prompt.GeminiPromptBuilder
import com.laterbox.data.ai.security.SecureApiKeyStorage
import com.laterbox.data.local.dao.AiAnalysisDao
import com.laterbox.data.local.entity.AiAnalysisEntity
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
    private val tagRepository: TagRepository = LaterBoxApp.instance.tagRepository,
    private val geminiAnalysisService: GeminiAnalysisService = LaterBoxApp.instance.geminiAnalysisService,
    private val aiAnalysisDao: AiAnalysisDao = LaterBoxApp.instance.database.aiAnalysisDao(),
    private val aiSettingsRepository: AiSettingsRepository = LaterBoxApp.instance.aiSettingsRepository
) : ViewModel() {

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiActionError = MutableStateFlow<String?>(null)
    val aiActionError: StateFlow<String?> = _aiActionError.asStateFlow()

    private val _isAiQuerying = MutableStateFlow(false)
    val isAiQuerying: StateFlow<Boolean> = _isAiQuerying.asStateFlow()

    private val _aiQueryResponse = MutableStateFlow<String?>(null)
    val aiQueryResponse: StateFlow<String?> = _aiQueryResponse.asStateFlow()

    private val _aiQueryError = MutableStateFlow<String?>(null)
    val aiQueryError: StateFlow<String?> = _aiQueryError.asStateFlow()

    val aiAnalysis: StateFlow<AiAnalysisEntity?> = aiAnalysisDao.getAnalysisForItem(itemId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isAiEnabled: StateFlow<Boolean> = aiSettingsRepository.isAiEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasConsentedPrivacy: StateFlow<Boolean> = aiSettingsRepository.hasConsentedPrivacy
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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

    fun grantPrivacyConsent() {
        viewModelScope.launch {
            aiSettingsRepository.setConsentedPrivacy(true)
        }
    }

    fun clearAiError() {
        _aiActionError.value = null
    }

    fun analyzeWithAi(forceRegenerate: Boolean = false) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiActionError.value = null
            val result = geminiAnalysisService.analyzeItem(itemId, forceRegenerate = forceRegenerate)
            result.onFailure { error ->
                _aiActionError.value = error.message ?: "Failed to analyze content."
            }
            _isAiLoading.value = false
        }
    }

    fun deleteAiAnalysis() {
        viewModelScope.launch {
            geminiAnalysisService.deleteAnalysis(itemId)
        }
    }

    fun clearAiQueryResponse() {
        _aiQueryResponse.value = null
        _aiQueryError.value = null
        _isAiQuerying.value = false
    }

    fun askGeminiAboutItem(question: String) {
        val cleanQuestion = question.trim()
        if (cleanQuestion.isBlank()) return

        val currentItem = uiState.value.item ?: return

        viewModelScope.launch {
            _isAiQuerying.value = true
            _aiQueryError.value = null
            _aiQueryResponse.value = null

            try {
                val apiKey = SecureApiKeyStorage.getInstance(LaterBoxApp.instance).getApiKey()
                if (apiKey.isNullOrBlank()) {
                    _aiQueryError.value = "Gemini API key is not configured. Add your API key in Settings."
                    _isAiQuerying.value = false
                    return@launch
                }

                if (!isAiEnabled.value) {
                    _aiQueryError.value = "Gemini AI is disabled in Settings. Enable it to ask questions."
                    _isAiQuerying.value = false
                    return@launch
                }

                val modelId: String = aiSettingsRepository.getSelectedModelIdNow().ifBlank {
                    GeminiModels.DEFAULT_MODEL_ID
                }

                val prompt = GeminiPromptBuilder.buildItemQuestionPrompt(
                    item = currentItem,
                    aiAnalysis = aiAnalysis.value,
                    question = cleanQuestion
                )

                val client = GeminiApiClient()
                when (val result = client.generateContentText(apiKey, modelId, prompt)) {
                    is GeminiApiResult.Success -> {
                        _aiQueryResponse.value = result.data
                    }
                    is GeminiApiResult.Error -> {
                        _aiQueryError.value = result.message
                    }
                }
            } catch (e: Exception) {
                _aiQueryError.value = "Failed to query Gemini: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isAiQuerying.value = false
            }
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
