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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.ui.components.ConfirmationDialog
import com.laterbox.ui.components.ContentActionsSheet
import com.laterbox.ui.components.ContentCardList
import com.laterbox.ui.components.EmptyStateView
import com.laterbox.ui.components.LaterBoxHeader
import com.laterbox.ui.components.LaterBoxSearchBar
import com.laterbox.ui.components.getIconForName
import com.laterbox.ui.theme.LaterBoxThemeTokens

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

    var showDeleteConfirm by remember { mutableStateOf(false) }
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
        containerColor = MaterialTheme.colorScheme.background
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
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Default.BookmarkBorder,
                        title = "No items in this playlist",
                        description = "Add saved content to this playlist from your library or share menu."
                    )
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
                            onFavoriteToggle = { viewModel.toggleFavorite(item) },
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
            onToggleFavorite = { viewModel.toggleFavorite(item) },
            onToggleArchive = {},
            onEditNotes = {},
            onRemoveFromPlaylist = {
                viewModel.removeItemFromPlaylist(item.id)
            },
            onMoveToTrash = {
                viewModel.moveToTrash(item.id)
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
                viewModel.deletePlaylist { onBack() }
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
