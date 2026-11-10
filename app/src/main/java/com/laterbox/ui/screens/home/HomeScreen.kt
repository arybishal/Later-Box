package com.laterbox.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.ui.components.AddManualItemDialog
import com.laterbox.ui.components.AddToPlaylistDialog
import com.laterbox.ui.components.ContentActionsSheet
import com.laterbox.ui.components.CreatePlaylistDialog
import com.laterbox.ui.components.EditNotesDialog
import kotlinx.coroutines.launch

/**
 * Completely redesigned Home screen for Later Box.
 * World-class minimal aesthetic: Crisp White (#FFFFFF), Deep Black (#111111), and Soft Butter Yellow (#FFF1A6).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToFavorites: () -> Unit = onNavigateToLibrary,
    onOpenItem: (SavedItemEntity) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val recentItems by viewModel.recentItems.collectAsState()
    val continueItems by viewModel.continueItems.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val favoriteCount by viewModel.favoriteCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var selectedItemForActions by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForNotes by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForPlaylists by remember { mutableStateOf<SavedItemEntity?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp)
        ) {
            // 2.1 Header: Minimal wordmark with archive tag & settings entry
            item {
                HomeHeader(onNavigateToSettings = onNavigateToSettings)
            }

            // 2.2 Introductory Section: "Your saved world."
            item {
                Spacer(modifier = Modifier.height(4.dp))
                HomeIntroSection()
                Spacer(modifier = Modifier.height(18.dp))
            }

            // 2.3 Prominent Search Entry
            item {
                HomeSearchEntry(onClick = onNavigateToSearch)
                Spacer(modifier = Modifier.height(26.dp))
            }

            // 2.4 Continue Reading / Watching Section (compact, only shown if unfinished items exist)
            if (continueItems.isNotEmpty()) {
                item {
                    HomeSectionHeader(
                        title = "Jump back in",
                        badgeCount = continueItems.size
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 26.dp)
                    ) {
                        items(continueItems, key = { it.id }) { item ->
                            ContinueContentCard(
                                item = item,
                                onClick = { onOpenItem(item) }
                            )
                        }
                    }
                }
            }

            // 2.5 Collections Section: Favorites, Inbox, user playlists, and New Collection
            item {
                HomeSectionHeader(
                    title = "Collections",
                    actionLabel = "+ New",
                    onActionClick = { showCreatePlaylistDialog = true }
                )
                Spacer(modifier = Modifier.height(12.dp))

                val defaultInbox = playlists.find { it.isDefaultInbox }
                val customPlaylists = playlists.filter { !it.isDefaultInbox }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 28.dp)
                ) {
                    // Smart collection: Favorites
                    item {
                        CollectionShortcutTile(
                            name = "Favorites",
                            count = favoriteCount,
                            icon = Icons.Default.Favorite,
                            isHighlight = true,
                            onClick = onNavigateToFavorites
                        )
                    }

                    // Smart collection: Inbox
                    item {
                        CollectionShortcutTile(
                            name = "Inbox",
                            count = defaultInbox?.itemCount ?: 0,
                            icon = Icons.Default.BookmarkBorder,
                            onClick = {
                                if (defaultInbox != null) onNavigateToPlaylist(defaultInbox.id)
                                else onNavigateToLibrary()
                            }
                        )
                    }

                    // User Playlists
                    items(customPlaylists, key = { it.id }) { playlist ->
                        CollectionShortcutTile(
                            name = playlist.name,
                            count = playlist.itemCount,
                            icon = Icons.Default.Folder,
                            onClick = { onNavigateToPlaylist(playlist.id) }
                        )
                    }

                    // Add new collection shortcut
                    item {
                        HomeNewPlaylistTile(onClick = { showCreatePlaylistDialog = true })
                    }
                }
            }

            // 2.6 Recently Saved Section
            item {
                HomeSectionHeader(
                    title = "Recently saved",
                    badgeCount = if (recentItems.isNotEmpty()) recentItems.size else null,
                    actionLabel = "View all →",
                    onActionClick = onNavigateToLibrary
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Compact feed items or refined empty state
            if (recentItems.isEmpty()) {
                item {
                    HomeEmptyState(onSaveClick = { showAddDialog = true })
                }
            } else {
                items(recentItems, key = { it.id }) { item ->
                    RecentSavedItemCard(
                        item = item,
                        onClick = { onOpenItem(item) },
                        onFavoriteToggle = { viewModel.toggleFavorite(item) },
                        onAddToPlaylist = { selectedItemForPlaylists = item },
                        onShare = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, item.url.ifEmpty { item.title })
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                        },
                        onMenuClick = { selectedItemForActions = item },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    // Modal dialogs and action sheets (preserving existing business logic)
    if (showAddDialog) {
        AddManualItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { url, title, notes ->
                viewModel.saveManualItem(url, title, notes)
                showAddDialog = false
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

    selectedItemForActions?.let { item ->
        ContentActionsSheet(
            item = item,
            sheetState = bottomSheetState,
            onDismiss = { selectedItemForActions = null },
            onOpenInApp = {
                onOpenItem(item)
                selectedItemForActions = null
            },
            onAddToPlaylist = {
                selectedItemForPlaylists = item
                selectedItemForActions = null
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(item)
                selectedItemForActions = null
            },
            onToggleArchive = {
                viewModel.toggleArchive(item)
                selectedItemForActions = null
            },
            onEditNotes = {
                selectedItemForNotes = item
                selectedItemForActions = null
            },
            onMoveToTrash = {
                viewModel.moveToTrash(item.id)
                selectedItemForActions = null
            }
        )
    }

    selectedItemForNotes?.let { item ->
        EditNotesDialog(
            initialNotes = item.notes,
            onDismiss = { selectedItemForNotes = null },
            onSave = { newNotes ->
                viewModel.updateNotes(item.id, newNotes)
                selectedItemForNotes = null
            }
        )
    }

    selectedItemForPlaylists?.let { item ->
        val currentPlaylistEntities by viewModel.getPlaylistsForItem(item.id)
            .collectAsState(initial = emptyList())
        AddToPlaylistDialog(
            allPlaylists = playlists.map { it.toPlaylistEntity() },
            initiallySelectedIds = currentPlaylistEntities.map { it.id },
            onDismiss = { selectedItemForPlaylists = null },
            onSavePlaylists = { selectedIds ->
                viewModel.updatePlaylistsForItem(item.id, selectedIds)
                selectedItemForPlaylists = null
            },
            onCreateNewPlaylist = {
                showCreatePlaylistDialog = true
            }
        )
    }
}
