package com.laterbox.ui.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.ui.components.ConfirmationDialog
import com.laterbox.ui.components.ContentActionsSheet
import com.laterbox.ui.components.ContentCardList
import com.laterbox.ui.components.ContextualEmptyState
import com.laterbox.ui.components.CreatePlaylistDialog
import com.laterbox.ui.components.EmptyStateView
import com.laterbox.ui.components.LaterBoxHeader
import com.laterbox.ui.components.LaterBoxSearchBar
import com.laterbox.ui.components.LaterBoxSnackbarController
import com.laterbox.ui.components.ZeroSearchResultsState
import com.laterbox.ui.components.getIconForName
import com.laterbox.ui.theme.LaterBoxThemeTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onBack: () -> Unit,
    onOpenItem: (SavedItemEntity) -> Unit,
    viewModel: PlaylistDetailViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PlaylistDetailViewModel(playlistId) as T
        }
    })
) {
    val extended = LaterBoxThemeTokens.extended
    val playlist by viewModel.playlist.collectAsState()
    val items by viewModel.filteredItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val scope = rememberCoroutineScope()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedItemForActions by remember { mutableStateOf<SavedItemEntity?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            LaterBoxHeader(
                greeting = playlist?.name ?: "Playlist",
                subtitle = playlist?.description ?: "${items.size} items",
                showBack = true,
                onBackClick = onBack,
                actions = {
                    if (playlist?.isDefaultInbox == false) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Playlist",
                                tint = extended.primaryIcons
                            )
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Playlist",
                                tint = extended.warmError
                            )
                        }
                    }
                }
            )
        },
        containerColor = extended.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search inside playlist
            LaterBoxSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                placeholder = "Search in ${playlist?.name ?: "playlist"}…",
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp)
            )

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (searchQuery.isNotBlank()) {
                        ZeroSearchResultsState(
                            query = searchQuery,
                            onClearSearch = { viewModel.setSearchQuery("") }
                        )
                    } else {
                        ContextualEmptyState(
                            icon = Icons.Default.FolderOpen,
                            title = "This collection is empty",
                            description = "Add saved links, articles, or videos to this collection from your library or share menu.",
                            primaryActionLabel = "Back to Library",
                            onPrimaryAction = onBack
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items, key = { it.id }) { item ->
                        ContentCardList(
                            item = item,
                            onClick = { onOpenItem(item) },
                            onFavoriteToggle = {
                                viewModel.toggleFavorite(item)
                                LaterBoxSnackbarController.showMessage(
                                    if (!item.isFavorite) "Added to Favorites" else "Removed from Favorites"
                                )
                            },
                            onMenuClick = { selectedItemForActions = item }
                        )
                    }
                }
            }
        }
    }

    // Contextual Actions Bottom Sheet
    selectedItemForActions?.let { item ->
        ContentActionsSheet(
            item = item,
            sheetState = bottomSheetState,
            onDismiss = { selectedItemForActions = null },
            onOpenInApp = { onOpenItem(item) },
            onAddToPlaylist = {},
            onToggleFavorite = {
                viewModel.toggleFavorite(item)
                LaterBoxSnackbarController.showMessage(
                    if (!item.isFavorite) "Added to Favorites" else "Removed from Favorites"
                )
            },
            onToggleArchive = {},
            onEditNotes = {},
            onRemoveFromPlaylist = {
                viewModel.removeItemFromPlaylist(item.id)
                LaterBoxSnackbarController.showMessage("Removed from \"${playlist?.name}\"")
            },
            onMoveToTrash = {
                viewModel.moveToTrash(item.id)
                LaterBoxSnackbarController.showMessage(
                    message = "Moved to Trash",
                    actionLabel = "Undo",
                    onAction = {
                        scope.launch {
                            LaterBoxApp.instance.contentRepository.restoreFromTrash(item.id)
                        }
                    }
                )
            }
        )
    }

    // Edit Playlist Dialog
    if (showEditDialog && playlist != null) {
        val currentPlaylist = playlist!!
        CreatePlaylistDialog(
            onDismiss = { showEditDialog = false },
            initialName = currentPlaylist.name,
            initialDescription = currentPlaylist.description ?: "",
            initialIcon = currentPlaylist.iconName ?: "bookmark",
            dialogTitle = "Edit Collection",
            confirmButtonLabel = "Save Changes",
            onConfirm = { newName, newDesc, _ ->
                viewModel.renamePlaylist(newName, newDesc)
                LaterBoxSnackbarController.showMessage("Updated collection details")
                showEditDialog = false
            }
        )
    }

    // Delete Playlist Confirmation Dialog
    if (showDeleteConfirm) {
        val count = items.size
        ConfirmationDialog(
            title = "Delete Playlist and Its Items?",
            message = "Deleting this playlist will also move all $count saved items inside it to Trash. You can recover deleted items from Trash.",
            confirmLabel = "Delete Playlist & Items",
            isDestructive = true,
            onConfirm = {
                viewModel.deletePlaylist {
                    LaterBoxSnackbarController.showMessage("Deleted playlist and moved items to Trash")
                    onBack()
                }
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
