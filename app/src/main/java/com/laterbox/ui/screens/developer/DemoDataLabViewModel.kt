package com.laterbox.ui.screens.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laterbox.LaterBoxApp
import com.laterbox.data.demo.DatabaseIntegrityStatus
import com.laterbox.data.demo.DemoDataGenerator
import com.laterbox.data.demo.DemoDatabaseCounts
import com.laterbox.data.local.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class PendingConfirmation {
    data class Generate(
        val title: String,
        val targetCount: Int,
        val description: String,
        val action: () -> Unit
    ) : PendingConfirmation()

    data class Remove(
        val title: String,
        val countToRemove: Int,
        val description: String,
        val action: () -> Unit
    ) : PendingConfirmation()
}

data class DemoDataLabUiState(
    val counts: DemoDatabaseCounts = DemoDatabaseCounts(),
    val integrity: DatabaseIntegrityStatus = DatabaseIntegrityStatus(),
    val lastGeneratedDate: String? = null,
    val lastDatasetType: String? = null,
    val isOperating: Boolean = false,
    val operationProgress: Float = 0f,
    val operationStatus: String = "",
    val feedbackMessage: String? = null,
    val useFixedSeed: Boolean = true,
    val pendingConfirmation: PendingConfirmation? = null
)

class DemoDataLabViewModel(
    private val demoDataGenerator: DemoDataGenerator = LaterBoxApp.instance.demoDataGenerator,
    private val preferencesRepository: UserPreferencesRepository = LaterBoxApp.instance.preferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemoDataLabUiState())
    val uiState: StateFlow<DemoDataLabUiState> = _uiState.asStateFlow()

    init {
        // Collect live demo record counts
        viewModelScope.launch {
            demoDataGenerator.demoCounts.collectLatest { counts ->
                _uiState.update { it.copy(counts = counts) }
            }
        }

        // Collect last generation timestamp
        viewModelScope.launch {
            preferencesRepository.lastDemoGenerationTimestamp.collectLatest { ts ->
                val formatted = ts?.let {
                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(it))
                }
                _uiState.update { it.copy(lastGeneratedDate = formatted) }
            }
        }

        // Collect last dataset type
        viewModelScope.launch {
            preferencesRepository.lastDemoDatasetType.collectLatest { type ->
                _uiState.update { it.copy(lastDatasetType = type) }
            }
        }

        checkDatabaseIntegrity()
    }

    fun setUseFixedSeed(fixed: Boolean) {
        _uiState.update { it.copy(useFixedSeed = fixed) }
    }

    fun dismissConfirmation() {
        _uiState.update { it.copy(pendingConfirmation = null) }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    fun checkDatabaseIntegrity() {
        viewModelScope.launch {
            val integrity = demoDataGenerator.checkDatabaseIntegrity()
            _uiState.update { it.copy(integrity = integrity) }
        }
    }

    fun requestGenerateComplete() {
        _uiState.update { current ->
            current.copy(
                pendingConfirmation = PendingConfirmation.Generate(
                    title = "Generate Complete Dataset",
                    targetCount = 200,
                    description = "This operation will generate exactly 200 synthetic records (100 general memories + 100 rich notes). Any existing demo records will be replaced cleanly without affecting real user content.",
                    action = { executeGenerateComplete() }
                )
            )
        }
    }

    fun requestGenerateGeneral() {
        _uiState.update { current ->
            current.copy(
                pendingConfirmation = PendingConfirmation.Generate(
                    title = "Generate General Dataset",
                    targetCount = 100,
                    description = "This operation will generate exactly 100 general synthetic memories across articles, links, photos, voice notes, tasks, and annual events.",
                    action = { executeGenerateGeneral() }
                )
            )
        }
    }

    fun requestGenerateRichNotes() {
        _uiState.update { current ->
            current.copy(
                pendingConfirmation = PendingConfirmation.Generate(
                    title = "Generate Rich Notes Dataset",
                    targetCount = 100,
                    description = "This operation will generate exactly 100 structured rich notes with varied headers, mixed inline spans, bulleted & numbered lists, interactive checklists, inline images, and hyperlinks.",
                    action = { executeGenerateRichNotes() }
                )
            )
        }
    }

    fun requestGenerateStress(count: Int) {
        _uiState.update { current ->
            current.copy(
                pendingConfirmation = PendingConfirmation.Generate(
                    title = "Generate Stress Benchmark Dataset",
                    targetCount = count,
                    description = "This operation will batch-insert $count synthetic records into the database to benchmark query latency, scroll smoothness, and memory usage.",
                    action = { executeGenerateStress(count) }
                )
            )
        }
    }

    fun requestRemoveGeneral() {
        val count = _uiState.value.counts.generalDemoItems
        _uiState.update { current ->
            current.copy(
                pendingConfirmation = PendingConfirmation.Remove(
                    title = "Remove General Demo Records",
                    countToRemove = count,
                    description = "This will permanently remove all $count general synthetic demo records. Your real user memories and custom playlists are strictly preserved.",
                    action = { executeRemoveGeneral() }
                )
            )
        }
    }

    fun requestRemoveRichNotes() {
        val count = _uiState.value.counts.richNotesDemoItems
        _uiState.update { current ->
            current.copy(
                pendingConfirmation = PendingConfirmation.Remove(
                    title = "Remove Rich Demo Notes",
                    countToRemove = count,
                    description = "This will permanently remove all $count synthetic rich notes and clean up unreferenced demo image fixtures.",
                    action = { executeRemoveRichNotes() }
                )
            )
        }
    }

    fun requestRemoveStress() {
        val count = _uiState.value.counts.stressDemoItems
        _uiState.update { current ->
            current.copy(
                pendingConfirmation = PendingConfirmation.Remove(
                    title = "Remove Stress Benchmark Records",
                    countToRemove = count,
                    description = "This will permanently remove all $count stress test records from the database.",
                    action = { executeRemoveStress() }
                )
            )
        }
    }

    fun requestRemoveAll() {
        val count = _uiState.value.counts.totalDemoItems
        _uiState.update { current ->
            current.copy(
                pendingConfirmation = PendingConfirmation.Remove(
                    title = "Remove All Demo Records",
                    countToRemove = count,
                    description = "This will remove all $count demo records (General, Rich Notes, and Stress) and prune unused demo media files and demo playlists. Normal user content will never be touched.",
                    action = { executeRemoveAll() }
                )
            )
        }
    }

    // --- Execution Methods ---

    private fun executeGenerateComplete() {
        val seed = if (_uiState.value.useFixedSeed) 42L else System.currentTimeMillis()
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, operationProgress = 0f, operationStatus = "Starting complete generation…") }
            val result = demoDataGenerator.generateCompleteDataset(seed) { progress, status ->
                _uiState.update { it.copy(operationProgress = progress, operationStatus = status) }
            }
            _uiState.update {
                it.copy(
                    isOperating = false,
                    feedbackMessage = result.fold(
                        onSuccess = { s -> "Successfully generated exactly 200 records in ${s.durationMs}ms." },
                        onFailure = { err -> "Generation failed: ${err.message}" }
                    )
                )
            }
            checkDatabaseIntegrity()
        }
    }

    private fun executeGenerateGeneral() {
        val seed = if (_uiState.value.useFixedSeed) 42L else System.currentTimeMillis()
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, operationProgress = 0f, operationStatus = "Starting general generation…") }
            val result = demoDataGenerator.generateGeneralDataset(seed) { progress, status ->
                _uiState.update { it.copy(operationProgress = progress, operationStatus = status) }
            }
            _uiState.update {
                it.copy(
                    isOperating = false,
                    feedbackMessage = result.fold(
                        onSuccess = { s -> "Generated 100 general memories in ${s.durationMs}ms." },
                        onFailure = { err -> "Generation failed: ${err.message}" }
                    )
                )
            }
            checkDatabaseIntegrity()
        }
    }

    private fun executeGenerateRichNotes() {
        val seed = if (_uiState.value.useFixedSeed) 42L else System.currentTimeMillis()
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, operationProgress = 0f, operationStatus = "Starting rich notes generation…") }
            val result = demoDataGenerator.generateRichNotesDataset(seed) { progress, status ->
                _uiState.update { it.copy(operationProgress = progress, operationStatus = status) }
            }
            _uiState.update {
                it.copy(
                    isOperating = false,
                    feedbackMessage = result.fold(
                        onSuccess = { s -> "Generated 100 rich notes in ${s.durationMs}ms." },
                        onFailure = { err -> "Generation failed: ${err.message}" }
                    )
                )
            }
            checkDatabaseIntegrity()
        }
    }

    private fun executeGenerateStress(count: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, operationProgress = 0f, operationStatus = "Starting stress benchmark generation…") }
            val result = demoDataGenerator.generateStressDataset(count) { progress, status ->
                _uiState.update { it.copy(operationProgress = progress, operationStatus = status) }
            }
            _uiState.update {
                it.copy(
                    isOperating = false,
                    feedbackMessage = result.fold(
                        onSuccess = { s -> "Inserted ${s.stressCount} stress records in ${s.durationMs}ms." },
                        onFailure = { err -> "Stress generation failed: ${err.message}" }
                    )
                )
            }
            checkDatabaseIntegrity()
        }
    }

    private fun executeRemoveGeneral() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, operationProgress = 0.2f, operationStatus = "Removing general demo records…") }
            val result = demoDataGenerator.removeGeneralDemoData()
            _uiState.update {
                it.copy(
                    isOperating = false,
                    feedbackMessage = result.fold(
                        onSuccess = { count -> "Removed $count general demo records." },
                        onFailure = { err -> "Removal failed: ${err.message}" }
                    )
                )
            }
            checkDatabaseIntegrity()
        }
    }

    private fun executeRemoveRichNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, operationProgress = 0.2f, operationStatus = "Removing rich demo notes…") }
            val result = demoDataGenerator.removeRichNotesDemoData()
            _uiState.update {
                it.copy(
                    isOperating = false,
                    feedbackMessage = result.fold(
                        onSuccess = { count -> "Removed $count rich demo notes." },
                        onFailure = { err -> "Removal failed: ${err.message}" }
                    )
                )
            }
            checkDatabaseIntegrity()
        }
    }

    private fun executeRemoveStress() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, operationProgress = 0.2f, operationStatus = "Removing stress records…") }
            val result = demoDataGenerator.removeStressDemoData()
            _uiState.update {
                it.copy(
                    isOperating = false,
                    feedbackMessage = result.fold(
                        onSuccess = { count -> "Removed $count stress benchmark records." },
                        onFailure = { err -> "Removal failed: ${err.message}" }
                    )
                )
            }
            checkDatabaseIntegrity()
        }
    }

    private fun executeRemoveAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, operationProgress = 0.2f, operationStatus = "Removing all demo records…") }
            val result = demoDataGenerator.removeAllDemoData()
            _uiState.update {
                it.copy(
                    isOperating = false,
                    feedbackMessage = result.fold(
                        onSuccess = { count -> "Safely removed all $count demo records." },
                        onFailure = { err -> "Removal failed: ${err.message}" }
                    )
                )
            }
            checkDatabaseIntegrity()
        }
    }
}
