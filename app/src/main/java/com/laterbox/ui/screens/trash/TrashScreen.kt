package com.laterbox.ui.screens.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.ui.components.ConfirmationDialog
import com.laterbox.ui.components.ContentActionsSheet
import com.laterbox.ui.components.ContentCardList
import com.laterbox.ui.components.ContextualEmptyState
import com.laterbox.ui.components.EmptyStateView
import com.laterbox.ui.components.LaterBoxHeader
import com.laterbox.ui.components.LaterBoxSnackbarController
import com.laterbox.ui.theme.LaterBoxThemeTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onBack: () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    val scope = rememberCoroutineScope()
    val trashItems by LaterBoxApp.instance.contentRepository.getTrashItems().collectAsState(initial = emptyList())
    var selectedItemForActions by remember { mutableStateOf<SavedItemEntity?>(null) }
    var showEmptyTrashConfirm by remember { mutableStateOf(false) }
    var itemForPermanentDelete by remember { mutableStateOf<SavedItemEntity?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            LaterBoxHeader(
                greeting = "Recently Deleted",
                subtitle = "${trashItems.size} items in trash",
                showBack = true,
                onBackClick = onBack,
                actions = {
                    if (trashItems.isNotEmpty()) {
                        IconButton(onClick = { showEmptyTrashConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Empty Trash",
                                tint = extended.warmError
                            )
                        }
                    }
                }
            )
        },
        containerColor = extended.background
    ) { paddingValues ->
        if (trashItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                ContextualEmptyState(
                    icon = Icons.Default.DeleteForever,
                    title = "Trash is empty",
                    description = "Items you delete will be kept here before permanent deletion.",
                    primaryActionLabel = "Back to Settings",
                    onPrimaryAction = onBack
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 88.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(trashItems, key = { it.id }) { item ->
                    ContentCardList(
                        item = item,
                        onClick = { selectedItemForActions = item },
                        onFavoriteToggle = {},
                        onMenuClick = { selectedItemForActions = item },
                        showFavorite = false
                    )
                }
            }
        }
    }

    // Trashed Actions Bottom Sheet
    selectedItemForActions?.let { item ->
        ContentActionsSheet(
            item = item,
            sheetState = bottomSheetState,
            onDismiss = { selectedItemForActions = null },
            onOpenInApp = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onToggleArchive = {},
            onEditNotes = {},
            onMoveToTrash = {},
            onRestore = {
                scope.launch {
                    LaterBoxApp.instance.contentRepository.restoreFromTrash(item.id)
                    LaterBoxSnackbarController.showMessage("Restored \"${item.title}\"")
                }
                selectedItemForActions = null
            },
            onPermanentDelete = {
                itemForPermanentDelete = item
                selectedItemForActions = null
            }
        )
    }

    // Confirm Empty Trash Dialog
    if (showEmptyTrashConfirm) {
        val totalToDelete = trashItems.size
        ConfirmationDialog(
            title = "Empty Trash",
            message = "Permanently delete all $totalToDelete items? This cannot be undone.",
            confirmLabel = "Empty All",
            isDestructive = true,
            onConfirm = {
                scope.launch {
                    LaterBoxApp.instance.contentRepository.emptyTrash()
                    LaterBoxSnackbarController.showMessage("Emptied trash ($totalToDelete items permanently deleted)")
                }
                showEmptyTrashConfirm = false
            },
            onDismiss = { showEmptyTrashConfirm = false }
        )
    }

    // Confirm Single Item Permanent Delete Dialog
    itemForPermanentDelete?.let { item ->
        ConfirmationDialog(
            title = "Delete Permanently",
            message = "Are you sure you want to permanently delete \"${item.title}\"? This action cannot be reversed.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                scope.launch {
                    LaterBoxApp.instance.contentRepository.permanentDelete(item.id)
                    LaterBoxSnackbarController.showMessage("Permanently deleted \"${item.title}\"")
                }
                itemForPermanentDelete = null
            },
            onDismiss = { itemForPermanentDelete = null }
        )
    }
}
