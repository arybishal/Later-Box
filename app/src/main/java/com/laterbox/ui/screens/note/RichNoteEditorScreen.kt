package com.laterbox.ui.screens.note

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.laterbox.LaterBoxApp
import com.laterbox.data.model.richnote.RichBlock
import com.laterbox.data.model.richnote.SpanType
import com.laterbox.ui.components.ConfirmationDialog
import com.laterbox.ui.components.LaterBoxSnackbarController
import com.laterbox.ui.screens.note.components.LinkInputDialog
import com.laterbox.ui.screens.note.components.RichImageBlockItem
import com.laterbox.ui.screens.note.components.RichNoteFormattingToolbar
import com.laterbox.ui.screens.note.components.RichNoteReadingView
import com.laterbox.ui.screens.note.components.RichNoteTopBar
import com.laterbox.ui.screens.note.components.RichTextBlockItem
import com.laterbox.ui.theme.LaterBoxThemeTokens
import kotlinx.coroutines.launch

@Composable
fun RichNoteEditorScreen(
    itemId: Long,
    initialEdit: Boolean = true,
    onBack: () -> Unit,
    viewModel: RichNoteViewModel = viewModel()
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    var showLinkDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Android Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri)
            viewModel.insertImage(uri, mimeType)
        }
    }

    LaunchedEffect(itemId, initialEdit) {
        viewModel.initialize(itemId, initialEdit)
    }

    // Flush any pending changes when back button is pressed
    BackHandler {
        viewModel.saveImmediately()
        onBack()
    }

    Scaffold(
        topBar = {
            RichNoteTopBar(
                isReadingMode = uiState.isReadingMode,
                saveStatus = uiState.saveStatus,
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                wordCount = uiState.wordCount,
                characterCount = uiState.characterCount,
                onBack = {
                    viewModel.saveImmediately()
                    onBack()
                },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onToggleReadingMode = { viewModel.toggleReadingMode() },
                onRetrySave = { viewModel.saveImmediately() },
                onClearFormatting = { viewModel.clearFormatting() },
                onCopyPlainText = {
                    val plainText = uiState.document.toPlainText()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Note Content", plainText))
                    Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                onDeleteNote = { showDeleteConfirmDialog = true }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !uiState.isReadingMode,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                RichNoteFormattingToolbar(
                    activeFormatting = uiState.activeFormatting,
                    activeStyle = uiState.activeStyle,
                    activeAlignment = uiState.activeAlignment,
                    activeListType = uiState.activeListType,
                    onToggleFormat = { spanType -> viewModel.applyFormatting(spanType) },
                    onSelectStyle = { styleType -> viewModel.applyStyle(styleType) },
                    onSelectAlignment = { alignment -> viewModel.applyAlignment(alignment) },
                    onSelectListType = { listType -> viewModel.applyListType(listType) },
                    onInsertImage = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onInsertLink = { showLinkDialog = true },
                    modifier = Modifier.imePadding()
                )
            }
        },
        floatingActionButton = {
            if (uiState.isReadingMode) {
                FloatingActionButton(
                    onClick = { viewModel.toggleReadingMode() },
                    shape = CircleShape,
                    containerColor = extended.buttonPrimary,
                    contentColor = extended.buttonPrimaryText,
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Note",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        containerColor = extended.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isReadingMode) {
                // Reading Mode
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        RichNoteReadingView(
                            document = uiState.document,
                            onToggleChecklist = { blockId -> viewModel.toggleChecklist(blockId) }
                        )
                    }
                }
            } else {
                // Editing Mode
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp)
                ) {
                    // Document Title
                    item(key = "note_title") {
                        Spacer(modifier = Modifier.height(12.dp))
                        BasicTextField(
                            value = uiState.document.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = extended.primaryBlack,
                                lineHeight = 30.sp
                            ),
                            cursorBrush = SolidColor(extended.primaryBlack),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            decorationBox = { innerTextField ->
                                if (uiState.document.title.isEmpty()) {
                                    Text(
                                        text = "Title",
                                        style = TextStyle(
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = extended.tertiaryText,
                                            lineHeight = 30.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Content Blocks
                    itemsIndexed(
                        items = uiState.document.blocks,
                        key = { _, block -> block.id }
                    ) { _, block ->
                        when (block) {
                            is RichBlock.Text -> {
                                RichTextBlockItem(
                                    block = block,
                                    isFocused = uiState.activeBlockId == block.id,
                                    onTextChange = { newText, cursorPosition ->
                                        viewModel.updateBlockText(block.id, newText, cursorPosition)
                                    },
                                    onSelectionChange = { start, end ->
                                        viewModel.updateSelection(block.id, start, end)
                                    },
                                    onSplitBlock = { cursorPosition ->
                                        viewModel.splitBlock(block.id, cursorPosition)
                                    },
                                    onMergeWithPrevious = {
                                        viewModel.mergeWithPreviousBlock(block.id)
                                    },
                                    onToggleChecklist = {
                                        viewModel.toggleChecklist(block.id)
                                    }
                                )
                            }
                            is RichBlock.Image -> {
                                RichImageBlockItem(
                                    block = block,
                                    isReadingMode = false,
                                    onCaptionChange = { caption ->
                                        viewModel.updateImageCaption(block.id, caption)
                                    },
                                    onRemove = {
                                        viewModel.removeBlock(block.id)
                                    }
                                )
                            }
                        }
                    }

                    // Bottom padding so toolbar and keyboard don't obscure content
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }

    // Link Input Dialog
    if (showLinkDialog) {
        val activeBlock = uiState.document.blocks.find { it.id == uiState.activeBlockId } as? RichBlock.Text
        val selStart = minOf(uiState.selectionStart, uiState.selectionEnd)
        val selEnd = maxOf(uiState.selectionStart, uiState.selectionEnd)
        val selectedText = if (activeBlock != null && selStart < selEnd && selEnd <= activeBlock.text.length) {
            activeBlock.text.substring(selStart, selEnd)
        } else ""

        val existingLinkSpan = activeBlock?.spans?.find {
            it.type == SpanType.LINK && it.start <= selStart && it.end >= selEnd
        }

        LinkInputDialog(
            initialText = selectedText,
            initialUrl = existingLinkSpan?.linkUrl ?: "",
            onDismiss = { showLinkDialog = false },
            onConfirm = { url, _ ->
                viewModel.applyFormatting(SpanType.LINK, url)
                showLinkDialog = false
            },
            onRemove = if (existingLinkSpan != null) {
                {
                    viewModel.applyFormatting(SpanType.LINK, null)
                    showLinkDialog = false
                }
            } else null
        )
    }

    // Delete Note Confirmation Dialog
    if (showDeleteConfirmDialog) {
        ConfirmationDialog(
            title = "Delete Note",
            message = "Are you sure you want to move this note to Trash? You can restore it later from Trash & Retention.",
            confirmLabel = "Move to Trash",
            onConfirm = {
                showDeleteConfirmDialog = false
                scope.launch {
                    if (uiState.itemId > 0L) {
                        LaterBoxApp.instance.contentRepository.softDelete(uiState.itemId)
                        LaterBoxSnackbarController.showMessage("Note moved to Trash")
                    }
                    onBack()
                }
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }
}
