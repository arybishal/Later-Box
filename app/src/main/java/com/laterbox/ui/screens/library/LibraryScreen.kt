package com.laterbox.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import com.laterbox.data.model.ViewMode
import com.laterbox.ui.components.AddManualItemDialog
import com.laterbox.ui.components.AddToPlaylistDialog
import com.laterbox.ui.components.CompactSocialFeedCard
import com.laterbox.ui.components.ContentActionsSheet
import com.laterbox.ui.components.ContentCardGrid
import com.laterbox.ui.components.ContentCardList
import com.laterbox.ui.components.CreatePlaylistDialog
import com.laterbox.ui.components.EditNotesDialog
import com.laterbox.ui.components.EmptyDashedCard
import com.laterbox.ui.components.GridListTogglePill
import com.laterbox.ui.components.LaterBoxSearchBar
import com.laterbox.ui.components.PillFilterRow
import com.laterbox.ui.components.SortDropdownSelector
import com.laterbox.ui.theme.LaterBoxThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenItem: (SavedItemEntity) -> Unit,
    initialCategory: String = "All",
    viewModel: LibraryViewModel = viewModel()
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val items by viewModel.items.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var selectedItemForActions by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForNotes by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForPlaylists by remember { mutableStateOf<SavedItemEntity?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Map UI category string to ViewModel type filter
    val filteredItems = items.filter { item ->
        when (selectedCategory) {
            "All" -> true
            "Favorites" -> item.isFavorite
            "Inbox" -> true
            "Videos" -> item.contentType == ContentType.VIDEO
            "Articles" -> item.contentType == ContentType.ARTICLE
            "Images" -> item.contentType == ContentType.IMAGE
            "Documents" -> item.contentType == ContentType.DOCUMENT
            "Audio" -> item.contentType == ContentType.AUDIO
            "Notes" -> item.contentType == ContentType.NOTE
            else -> true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            // Header: "Library" + "X items" (Left) & [ Grid | List ] (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Library",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${filteredItems.size} items",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = extended.textSecondary
                    )
                }

                GridListTogglePill(
                    currentMode = viewMode,
                    onModeChanged = { viewModel.setViewMode(it) }
                )
            }

            // Search Bar: "Search titles, notes, links"
            LaterBoxSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                placeholder = "Search titles, notes, links"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Pills Row: All, Inbox, Videos, Articles, Images...
            PillFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sort Selector: "Sort" + [ Newest first  v ]
            SortDropdownSelector(
                currentSort = sortOrder,
                onSortChanged = { viewModel.setSortOrder(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Content: Empty State Card vs Grid / List
            if (filteredItems.isEmpty()) {
                EmptyDashedCard(
                    title = "Your library is empty",
                    description = "Anything you save shows up here, in a grid or a list.",
                    buttonLabel = "Save something",
                    onButtonClick = { showAddDialog = true }
                )
            } else {
                if (viewMode == ViewMode.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            ContentCardGrid(
                                item = item,
                                onClick = { onOpenItem(item) },
                                onFavoriteToggle = { viewModel.toggleFavorite(item) },
                                onMenuClick = { selectedItemForActions = item }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            CompactSocialFeedCard(
                                item = item,
                                onClick = { onOpenItem(item) },
                                onFavoriteToggle = { viewModel.toggleFavorite(item) },
                                onAddToPlaylist = { selectedItemForPlaylists = item },
                                onShare = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, item.url ?: item.title)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                                },
                                onMenuClick = { selectedItemForActions = item }
                            )
                        }
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
            onAddToPlaylist = { selectedItemForPlaylists = item },
            onToggleFavorite = { viewModel.toggleFavorite(item) },
            onToggleArchive = { viewModel.toggleArchive(item) },
            onEditNotes = { selectedItemForNotes = item },
            onMoveToTrash = { viewModel.moveToTrash(item.id) }
        )
    }

    // Edit Notes Dialog
    selectedItemForNotes?.let { item ->
        EditNotesDialog(
            initialNotes = item.notes,
            onDismiss = { selectedItemForNotes = null },
            onSave = { updatedNotes ->
                viewModel.updateNotes(item.id, updatedNotes)
                selectedItemForNotes = null
            }
        )
    }

    // Add to Playlist Dialog
    selectedItemForPlaylists?.let { item ->
        LibraryPlaylistDialogHost(
            item = item,
            viewModel = viewModel,
            onDismiss = { selectedItemForPlaylists = null },
            onCreateNewPlaylist = { showCreatePlaylistDialog = true }
        )
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name, desc, icon ->
                viewModel.createPlaylist(name, desc, icon)
                showCreatePlaylistDialog = false
            }
        )
    }

    // Add Manual Item Dialog
    if (showAddDialog) {
        AddManualItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { url, title, notes ->
                scope.launch {
                    LaterBoxApp.instance.contentRepository.saveContent(
                        url = url,
                        title = title,
                        notes = notes
                    )
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun LibraryPlaylistDialogHost(
    item: SavedItemEntity,
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val currentPlaylists by LaterBoxApp.instance.playlistRepository.getPlaylistsForItem(item.id).collectAsState(initial = emptyList())
    AddToPlaylistDialog(
        allPlaylists = allPlaylists,
        initiallySelectedIds = currentPlaylists.map { it.id },
        onDismiss = onDismiss,
        onSavePlaylists = { pIds ->
            viewModel.updatePlaylistsForItem(item.id, pIds)
            onDismiss()
        },
        onCreateNewPlaylist = onCreateNewPlaylist
    )
}
