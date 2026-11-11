package com.laterbox.ui.screens.note

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laterbox.LaterBoxApp
import com.laterbox.data.model.ContentType
import com.laterbox.data.model.richnote.ListType
import com.laterbox.data.model.richnote.RichBlock
import com.laterbox.data.model.richnote.RichDocument
import com.laterbox.data.model.richnote.RichSpan
import com.laterbox.data.model.richnote.RichTextFormatter
import com.laterbox.data.model.richnote.SpanType
import com.laterbox.data.model.richnote.TextAlignment
import com.laterbox.data.model.richnote.TextStyleType
import com.laterbox.data.local.entity.SavedItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Autosave indicator states.
 */
enum class SaveStatus {
    SAVED,
    SAVING,
    LOCAL_SAVE,
    ERROR
}

/**
 * UI State for the Rich Notes Editor.
 */
data class RichNoteUiState(
    val itemId: Long = 0L,
    val document: RichDocument = RichDocument(),
    val activeBlockId: String? = null,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val activeFormatting: Set<SpanType> = emptySet(),
    val activeStyle: TextStyleType = TextStyleType.NORMAL,
    val activeAlignment: TextAlignment = TextAlignment.LEFT,
    val activeListType: ListType = ListType.NONE,
    val isReadingMode: Boolean = false,
    val saveStatus: SaveStatus = SaveStatus.SAVED,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isNewNote: Boolean = false,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val isImportingImage: Boolean = false,
    val errorMessage: String? = null
)

class RichNoteViewModel : ViewModel() {

    private val contentRepository = LaterBoxApp.instance.contentRepository
    private val mediaStorageManager = LaterBoxApp.instance.mediaStorageManager

    private val _uiState = MutableStateFlow(RichNoteUiState())
    val uiState: StateFlow<RichNoteUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<RichDocument>()
    private val redoStack = mutableListOf<RichDocument>()
    private val maxUndoHistory = 50

    private var autosaveJob: Job? = null
    private var lastSnapshotTime = 0L
    private val snapshotTypingDebounceMs = 800L

    fun initialize(itemId: Long, initialEdit: Boolean = true) {
        viewModelScope.launch {
            if (itemId > 0L) {
                val item = contentRepository.getItemById(itemId)
                if (item != null) {
                    val mediaDir = mediaStorageManager.getMediaDirectory()
                    val doc = RichDocument.fromJson(item.notes, mediaDir).copy(
                        title = item.title
                    )
                    _uiState.update {
                        it.copy(
                            itemId = itemId,
                            document = doc,
                            activeBlockId = doc.blocks.firstOrNull()?.id,
                            isReadingMode = !initialEdit,
                            isNewNote = false,
                            saveStatus = SaveStatus.SAVED,
                            wordCount = calculateWordCount(doc),
                            characterCount = calculateCharCount(doc)
                        )
                    }
                    return@launch
                }
            }

            // Create blank new note
            val newDoc = RichDocument(
                title = "",
                blocks = listOf(RichBlock.Text())
            )
            _uiState.update {
                it.copy(
                    itemId = 0L,
                    document = newDoc,
                    activeBlockId = newDoc.blocks.first().id,
                    isReadingMode = !initialEdit,
                    isNewNote = true,
                    saveStatus = SaveStatus.SAVED,
                    wordCount = 0,
                    characterCount = 0
                )
            }
        }
    }

    // =========================================================================
    // Document Editing Operations
    // =========================================================================

    fun updateTitle(newTitle: String) {
        pushUndoSnapshot(debounce = true)
        _uiState.update { state ->
            val updatedDoc = state.document.copy(title = newTitle)
            state.copy(
                document = updatedDoc,
                wordCount = calculateWordCount(updatedDoc),
                characterCount = calculateCharCount(updatedDoc)
            )
        }
        triggerAutosave()
    }

    fun updateBlockText(blockId: String, newText: String, cursorPosition: Int) {
        pushUndoSnapshot(debounce = true)
        _uiState.update { state ->
            val updatedBlocks = state.document.blocks.map { block ->
                if (block is RichBlock.Text && block.id == blockId) {
                    val adjustedSpans = RichTextFormatter.adjustSpansOnTextChange(
                        spans = block.spans,
                        oldText = block.text,
                        newText = newText,
                        cursorPos = cursorPosition
                    )
                    block.copy(text = newText, spans = adjustedSpans)
                } else {
                    block
                }
            }
            val updatedDoc = state.document.copy(blocks = updatedBlocks)
            state.copy(
                document = updatedDoc,
                activeBlockId = blockId,
                selectionStart = cursorPosition,
                selectionEnd = cursorPosition,
                wordCount = calculateWordCount(updatedDoc),
                characterCount = calculateCharCount(updatedDoc)
            )
        }
        triggerAutosave()
    }

    fun updateSelection(blockId: String, start: Int, end: Int) {
        _uiState.update { state ->
            val activeBlock = state.document.blocks.find { it.id == blockId } as? RichBlock.Text
            val activeSpans = if (activeBlock != null && start < end) {
                activeBlock.spans.filter { it.start <= start && it.end >= end }.map { it.type }.toSet()
            } else emptySet()

            state.copy(
                activeBlockId = blockId,
                selectionStart = start,
                selectionEnd = end,
                activeFormatting = activeSpans,
                activeStyle = activeBlock?.style ?: TextStyleType.NORMAL,
                activeAlignment = activeBlock?.alignment ?: TextAlignment.LEFT,
                activeListType = activeBlock?.listType ?: ListType.NONE
            )
        }
    }

    /**
     * Splits a text block at the cursor when Enter is pressed.
     * Manages list continuation and list exit cleanly.
     */
    fun splitBlock(blockId: String, cursorPosition: Int) {
        pushUndoSnapshot(debounce = false)
        _uiState.update { state ->
            val blocks = state.document.blocks
            val targetIndex = blocks.indexOfFirst { it.id == blockId }
            if (targetIndex == -1) return@update state

            val currentBlock = blocks[targetIndex]
            if (currentBlock !is RichBlock.Text) return@update state

            val currentText = currentBlock.text
            val clampedPos = cursorPosition.coerceIn(0, currentText.length)

            // Special case: pressing Enter on an empty list item exits the list
            if (currentText.isBlank() && currentBlock.listType != ListType.NONE) {
                val updatedBlock = currentBlock.copy(
                    listType = ListType.NONE,
                    style = TextStyleType.NORMAL
                )
                val newBlocks = blocks.toMutableList()
                newBlocks[targetIndex] = updatedBlock
                return@update state.copy(
                    document = state.document.copy(blocks = newBlocks),
                    activeListType = ListType.NONE
                )
            }

            // Normal split
            val firstPart = currentText.substring(0, clampedPos)
            val secondPart = currentText.substring(clampedPos)

            val firstSpans = currentBlock.spans.filter { it.end <= clampedPos }
            val secondSpans = currentBlock.spans
                .filter { it.start >= clampedPos }
                .map { it.copy(start = it.start - clampedPos, end = it.end - clampedPos) }

            val updatedCurrent = currentBlock.copy(text = firstPart, spans = firstSpans)
            val nextListIndex = if (currentBlock.listType == ListType.NUMBERED) currentBlock.listIndex + 1 else 1

            val newBlock = RichBlock.Text(
                id = UUID.randomUUID().toString(),
                text = secondPart,
                spans = secondSpans,
                style = if (currentBlock.style == TextStyleType.TITLE || currentBlock.style == TextStyleType.HEADING_1) TextStyleType.NORMAL else currentBlock.style,
                alignment = currentBlock.alignment,
                listType = currentBlock.listType,
                listIndex = nextListIndex,
                isChecked = false
            )

            val newBlocks = blocks.toMutableList()
            newBlocks[targetIndex] = updatedCurrent
            newBlocks.add(targetIndex + 1, newBlock)

            // Re-index subsequent numbered list items
            reindexNumberedLists(newBlocks, targetIndex + 2)

            val updatedDoc = state.document.copy(blocks = newBlocks)
            state.copy(
                document = updatedDoc,
                activeBlockId = newBlock.id,
                selectionStart = 0,
                selectionEnd = 0,
                activeListType = newBlock.listType
            )
        }
        triggerAutosave()
    }

    /**
     * Merges current block with previous block on Backspace at start.
     */
    fun mergeWithPreviousBlock(blockId: String) {
        pushUndoSnapshot(debounce = false)
        _uiState.update { state ->
            val blocks = state.document.blocks
            val targetIndex = blocks.indexOfFirst { it.id == blockId }
            if (targetIndex <= 0) return@update state

            val currentBlock = blocks[targetIndex]
            val prevBlock = blocks[targetIndex - 1]

            if (currentBlock is RichBlock.Text && prevBlock is RichBlock.Text) {
                val mergedText = prevBlock.text + currentBlock.text
                val offset = prevBlock.text.length
                val shiftedCurrentSpans = currentBlock.spans.map {
                    it.copy(start = it.start + offset, end = it.end + offset)
                }
                val mergedSpans = prevBlock.spans + shiftedCurrentSpans

                val mergedBlock = prevBlock.copy(
                    text = mergedText,
                    spans = mergedSpans
                )

                val newBlocks = blocks.toMutableList()
                newBlocks.removeAt(targetIndex)
                newBlocks[targetIndex - 1] = mergedBlock

                reindexNumberedLists(newBlocks, targetIndex)

                val updatedDoc = state.document.copy(blocks = newBlocks)
                state.copy(
                    document = updatedDoc,
                    activeBlockId = mergedBlock.id,
                    selectionStart = offset,
                    selectionEnd = offset
                )
            } else if (currentBlock is RichBlock.Text && currentBlock.text.isEmpty()) {
                val newBlocks = blocks.toMutableList()
                newBlocks.removeAt(targetIndex)
                val updatedDoc = state.document.copy(blocks = newBlocks)
                state.copy(
                    document = updatedDoc,
                    activeBlockId = prevBlock.id
                )
            } else {
                state
            }
        }
        triggerAutosave()
    }

    /**
     * Toggles the completion state of a checklist item.
     * Accessible in both reading and editing modes.
     */
    fun toggleChecklist(blockId: String) {
        pushUndoSnapshot(debounce = false)
        _uiState.update { state ->
            val updatedBlocks = state.document.blocks.map { block ->
                if (block is RichBlock.Text && block.id == blockId && block.listType == ListType.CHECKLIST) {
                    block.copy(isChecked = !block.isChecked)
                } else {
                    block
                }
            }
            state.copy(document = state.document.copy(blocks = updatedBlocks))
        }
        triggerAutosave()
    }

    // =========================================================================
    // Formatting & Styling Actions
    // =========================================================================

    fun applyFormatting(spanType: SpanType, linkUrl: String? = null) {
        val state = _uiState.value
        val blockId = state.activeBlockId ?: return
        val start = minOf(state.selectionStart, state.selectionEnd)
        val end = maxOf(state.selectionStart, state.selectionEnd)

        if (start >= end) return // Formatting applies to selected range

        pushUndoSnapshot(debounce = false)
        _uiState.update { current ->
            val updatedBlocks = current.document.blocks.map { block ->
                if (block is RichBlock.Text && block.id == blockId) {
                    val newSpans = RichTextFormatter.toggleSpan(
                        spans = block.spans,
                        type = spanType,
                        start = start,
                        end = end,
                        linkUrl = linkUrl
                    )
                    block.copy(spans = newSpans)
                } else {
                    block
                }
            }
            val activeSpans = current.activeFormatting.toMutableSet()
            if (activeSpans.contains(spanType)) activeSpans.remove(spanType) else activeSpans.add(spanType)

            current.copy(
                document = current.document.copy(blocks = updatedBlocks),
                activeFormatting = activeSpans
            )
        }
        triggerAutosave()
    }

    fun applyStyle(styleType: TextStyleType) {
        val blockId = _uiState.value.activeBlockId ?: return
        pushUndoSnapshot(debounce = false)

        _uiState.update { current ->
            val updatedBlocks = current.document.blocks.map { block ->
                if (block is RichBlock.Text && block.id == blockId) {
                    block.copy(style = styleType)
                } else {
                    block
                }
            }
            current.copy(
                document = current.document.copy(blocks = updatedBlocks),
                activeStyle = styleType
            )
        }
        triggerAutosave()
    }

    fun applyAlignment(alignment: TextAlignment) {
        val blockId = _uiState.value.activeBlockId ?: return
        pushUndoSnapshot(debounce = false)

        _uiState.update { current ->
            val updatedBlocks = current.document.blocks.map { block ->
                if (block is RichBlock.Text && block.id == blockId) {
                    block.copy(alignment = alignment)
                } else {
                    block
                }
            }
            current.copy(
                document = current.document.copy(blocks = updatedBlocks),
                activeAlignment = alignment
            )
        }
        triggerAutosave()
    }

    fun applyListType(listType: ListType) {
        val blockId = _uiState.value.activeBlockId ?: return
        pushUndoSnapshot(debounce = false)

        _uiState.update { current ->
            val updatedBlocks = current.document.blocks.toMutableList()
            val index = updatedBlocks.indexOfFirst { it.id == blockId }
            if (index == -1) return@update current

            val currentBlock = updatedBlocks[index] as? RichBlock.Text ?: return@update current
            val toggledType = if (currentBlock.listType == listType) ListType.NONE else listType

            updatedBlocks[index] = currentBlock.copy(
                listType = toggledType,
                isChecked = false
            )
            reindexNumberedLists(updatedBlocks, index)

            current.copy(
                document = current.document.copy(blocks = updatedBlocks),
                activeListType = toggledType
            )
        }
        triggerAutosave()
    }

    fun clearFormatting() {
        val state = _uiState.value
        val blockId = state.activeBlockId ?: return
        val start = minOf(state.selectionStart, state.selectionEnd)
        val end = maxOf(state.selectionStart, state.selectionEnd)

        pushUndoSnapshot(debounce = false)
        _uiState.update { current ->
            val updatedBlocks = current.document.blocks.map { block ->
                if (block is RichBlock.Text && block.id == blockId) {
                    val cleared = if (start < end) {
                        RichTextFormatter.clearFormatting(block.spans, start, end)
                    } else {
                        emptyList()
                    }
                    block.copy(
                        spans = cleared,
                        style = TextStyleType.NORMAL,
                        alignment = TextAlignment.LEFT,
                        listType = ListType.NONE
                    )
                } else {
                    block
                }
            }
            current.copy(
                document = current.document.copy(blocks = updatedBlocks),
                activeFormatting = emptySet(),
                activeStyle = TextStyleType.NORMAL,
                activeAlignment = TextAlignment.LEFT,
                activeListType = ListType.NONE
            )
        }
        triggerAutosave()
    }

    // =========================================================================
    // Image Handling
    // =========================================================================

    fun insertImage(uri: Uri, mimeType: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImportingImage = true) }
            try {
                val (localPath, _) = mediaStorageManager.saveSharedUriToInternalStorage(uri, mimeType)
                val fileName = File(localPath).name

                pushUndoSnapshot(debounce = false)

                _uiState.update { current ->
                    val blocks = current.document.blocks.toMutableList()
                    val activeId = current.activeBlockId
                    val insertIndex = if (activeId != null) {
                        blocks.indexOfFirst { it.id == activeId }.let { if (it != -1) it + 1 else blocks.size }
                    } else {
                        blocks.size
                    }

                    val imageBlock = RichBlock.Image(
                        id = UUID.randomUUID().toString(),
                        localPath = localPath,
                        fileName = fileName,
                        caption = ""
                    )
                    val trailingTextBlock = RichBlock.Text()

                    blocks.add(insertIndex, imageBlock)
                    blocks.add(insertIndex + 1, trailingTextBlock)

                    current.copy(
                        document = current.document.copy(blocks = blocks),
                        activeBlockId = trailingTextBlock.id,
                        isImportingImage = false
                    )
                }
                triggerAutosave()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImportingImage = false,
                        errorMessage = "Failed to import image: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateImageCaption(blockId: String, caption: String) {
        pushUndoSnapshot(debounce = true)
        _uiState.update { current ->
            val updated = current.document.blocks.map { block ->
                if (block is RichBlock.Image && block.id == blockId) {
                    block.copy(caption = caption)
                } else {
                    block
                }
            }
            current.copy(document = current.document.copy(blocks = updated))
        }
        triggerAutosave()
    }

    fun removeBlock(blockId: String) {
        pushUndoSnapshot(debounce = false)
        _uiState.update { current ->
            val blocks = current.document.blocks.filterNot { it.id == blockId }.ifEmpty {
                listOf(RichBlock.Text())
            }
            current.copy(
                document = current.document.copy(blocks = blocks),
                activeBlockId = blocks.firstOrNull()?.id
            )
        }
        triggerAutosave()
    }

    // =========================================================================
    // Undo / Redo Architecture
    // =========================================================================

    private fun pushUndoSnapshot(debounce: Boolean) {
        val now = System.currentTimeMillis()
        if (debounce && (now - lastSnapshotTime) < snapshotTypingDebounceMs) {
            return
        }
        lastSnapshotTime = now
        val currentDoc = _uiState.value.document

        if (undoStack.isEmpty() || undoStack.last() != currentDoc) {
            undoStack.add(currentDoc)
            if (undoStack.size > maxUndoHistory) {
                undoStack.removeAt(0)
            }
            redoStack.clear()
            _uiState.update { it.copy(canUndo = true, canRedo = false) }
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val currentDoc = _uiState.value.document
        redoStack.add(currentDoc)

        val previousDoc = undoStack.removeAt(undoStack.lastIndex)
        _uiState.update {
            it.copy(
                document = previousDoc,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true,
                wordCount = calculateWordCount(previousDoc),
                characterCount = calculateCharCount(previousDoc)
            )
        }
        triggerAutosave()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentDoc = _uiState.value.document
        undoStack.add(currentDoc)

        val nextDoc = redoStack.removeAt(redoStack.lastIndex)
        _uiState.update {
            it.copy(
                document = nextDoc,
                canUndo = true,
                canRedo = redoStack.isNotEmpty(),
                wordCount = calculateWordCount(nextDoc),
                characterCount = calculateCharCount(nextDoc)
            )
        }
        triggerAutosave()
    }

    // =========================================================================
    // Mode Switching & Persistence
    // =========================================================================

    fun toggleReadingMode() {
        val newMode = !_uiState.value.isReadingMode
        _uiState.update { it.copy(isReadingMode = newMode) }
        if (newMode) {
            saveImmediately()
        }
    }

    private fun triggerAutosave() {
        _uiState.update { it.copy(saveStatus = SaveStatus.SAVING) }
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(750) // Debounce rapid edits
            performSave()
        }
    }

    fun saveImmediately() {
        autosaveJob?.cancel()
        viewModelScope.launch {
            performSave()
        }
    }

    private suspend fun performSave() = withContext(Dispatchers.IO) {
        val state = _uiState.value
        val doc = state.document

        // If completely empty new note, don't write blank records
        if (state.isNewNote && doc.title.isBlank() && doc.blocks.all { it is RichBlock.Text && it.text.isBlank() }) {
            _uiState.update { it.copy(saveStatus = SaveStatus.SAVED) }
            return@withContext
        }

        try {
            val titleToSave = doc.title.ifBlank {
                doc.blocks.filterIsInstance<RichBlock.Text>()
                    .firstOrNull { it.text.isNotBlank() }?.text?.take(40)
                    ?: "Untitled Note"
            }
            val plainTextDesc = doc.toPlainText().take(160)
            val jsonNotes = doc.toJson()

            if (state.itemId == 0L) {
                val newEntity = SavedItemEntity(
                    url = "local://note/${System.currentTimeMillis()}",
                    title = titleToSave,
                    description = plainTextDesc,
                    notes = jsonNotes,
                    contentType = ContentType.NOTE,
                    isSnap = false,
                    captureMethod = "RICH_EDITOR"
                )
                val newId = contentRepository.saveItem(newEntity)
                _uiState.update {
                    it.copy(
                        itemId = newId,
                        isNewNote = false,
                        saveStatus = SaveStatus.SAVED
                    )
                }
            } else {
                val existing = contentRepository.getItemById(state.itemId)
                if (existing != null) {
                    val updated = if (existing.contentType == ContentType.NOTE) {
                        existing.copy(
                            title = titleToSave,
                            description = plainTextDesc,
                            notes = jsonNotes
                        )
                    } else {
                        // Personal note attached to another item type
                        existing.copy(notes = jsonNotes)
                    }
                    contentRepository.updateItem(updated)
                }
                _uiState.update { it.copy(saveStatus = SaveStatus.SAVED) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(saveStatus = SaveStatus.ERROR) }
        }
    }

    private fun reindexNumberedLists(blocks: MutableList<RichBlock>, startIndex: Int) {
        var counter = 1
        for (i in startIndex until blocks.size) {
            val block = blocks[i]
            if (block is RichBlock.Text && block.listType == ListType.NUMBERED) {
                blocks[i] = block.copy(listIndex = counter++)
            } else if (block is RichBlock.Text && block.listType != ListType.NUMBERED) {
                break
            }
        }
    }

    private fun calculateWordCount(doc: RichDocument): Int {
        val text = doc.toPlainText()
        if (text.isBlank()) return 0
        return text.split(Regex("""\s+""")).filter { it.isNotBlank() }.size
    }

    private fun calculateCharCount(doc: RichDocument): Int {
        return doc.toPlainText().length
    }

    override fun onCleared() {
        super.onCleared()
        // Final synchronous flush if pending
        if (_uiState.value.saveStatus == SaveStatus.SAVING) {
            saveImmediately()
        }
    }
}
