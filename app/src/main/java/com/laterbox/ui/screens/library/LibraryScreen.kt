package com.laterbox.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import com.laterbox.data.model.SortOrder
import com.laterbox.data.model.ViewMode
import com.laterbox.ui.components.AddToPlaylistDialog
import com.laterbox.ui.components.ContentActionsSheet
import com.laterbox.ui.components.ContentCardGrid
import com.laterbox.ui.components.ContentCardList
import com.laterbox.ui.components.CreatePlaylistDialog
import com.laterbox.ui.components.EditNotesDialog
import com.laterbox.ui.components.EmptyStateView
import com.laterbox.ui.components.FilterChip
import com.laterbox.ui.components.LaterBoxHeader
import com.laterbox.ui.components.LaterBoxSearchBar
import com.laterbox.ui.components.ViewModeSwitcher
import com.laterbox.ui.theme.LaterBoxThemeTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenItem: (SavedItemEntity) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val extended = LaterBoxThemeTokens.extended
    val items by viewModel.items.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var selectedItemForActions by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForNotes by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForPlaylists by remember { mutableStateOf<SavedItemEntity?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            LaterBoxHeader(
                greeting = "Library",
                subtitle = "${items.size} ${if (items.size == 1) "save" else "saves"}",
                actions = {
                    // Sort order button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = extended.amberAccent
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            SortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = order.label,
                                            fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sortOrder == order) extended.amberAccent else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOrder(order)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Grid / List view mode switcher
                    ViewModeSwitcher(
                        currentMode = viewMode,
                        onModeChanged = { viewModel.setViewMode(it) }
                    )
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
            // Search Input
            LaterBoxSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                selectedType = selectedType,
                onTypeSelect = { viewModel.setTypeFilter(it) },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Status Filter Chips (All, Favorites, Archive)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LibraryStatusFilter.values()) { filter ->
                    val isSelected = statusFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) extended.amberAccentSubtle else MaterialTheme.colorScheme.surface)
                            .border(1.dp, if (isSelected) extended.amberAccent else extended.border, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setStatusFilter(filter) }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) extended.amberAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content Area: Grid or List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Default.BookmarkBorder,
                        title = when (statusFilter) {
                            LibraryStatusFilter.FAVORITES -> "No favorites yet"
                            LibraryStatusFilter.ARCHIVE -> "Archive is empty"
                            LibraryStatusFilter.ALL -> if (searchQuery.isNotBlank()) "No matching saves" else "No saved content"
                        },
                        description = when (statusFilter) {
                            LibraryStatusFilter.FAVORITES -> "Mark saves as favorites to quickly find them here."
                            LibraryStatusFilter.ARCHIVE -> "Archived items will appear here."
                            LibraryStatusFilter.ALL -> if (searchQuery.isNotBlank()) "Try refining your search terms." else "Share articles, videos, and links to Later Box."
                        }
                    )
                }
            } else {
                if (viewMode == ViewMode.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 88.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items, key = { it.id }) { item ->
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
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 88.dp, top = 8.dp),
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
        val allPlaylists by viewModel.allPlaylists.collectAsState()
        AddToPlaylistDialog(
            allPlaylists = allPlaylists,
            initiallySelectedIds = emptyList(),
            onDismiss = { selectedItemForPlaylists = null },
            onSavePlaylists = { pIds ->
                viewModel.updatePlaylistsForItem(item.id, pIds)
                selectedItemForPlaylists = null
            },
            onCreateNewPlaylist = {
                showCreatePlaylistDialog = true
            }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name, desc, icon ->
                viewModel.createPlaylist(name, desc, icon)
                showCreatePlaylistDialog = false
            }
        )
    }
}
