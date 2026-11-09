package com.laterbox.ui.screens.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.ui.components.AddManualItemDialog
import com.laterbox.ui.components.AddToPlaylistDialog
import com.laterbox.ui.components.ContentActionsSheet
import com.laterbox.ui.components.ContentCardGrid
import com.laterbox.ui.components.ContentCardList
import com.laterbox.ui.components.CreatePlaylistDialog
import com.laterbox.ui.components.EditNotesDialog
import com.laterbox.ui.components.EmptyStateView
import com.laterbox.ui.components.LaterBoxHeader
import com.laterbox.ui.components.LaterBoxSearchBar
import com.laterbox.ui.components.PlaylistCard
import com.laterbox.ui.theme.LaterBoxThemeTokens
import java.util.Calendar

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onOpenItem: (SavedItemEntity) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val extended = LaterBoxThemeTokens.extended
    val recentItems by viewModel.recentItems.collectAsState()
    val continueItems by viewModel.continueItems.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var selectedItemForActions by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForNotes by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForPlaylists by remember { mutableStateOf<SavedItemEntity?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            LaterBoxHeader(
                greeting = getGreeting(),
                subtitle = "Later Box • Personal Vault",
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = extended.amberAccent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Save Content",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Global Search Field
            item {
                LaterBoxSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Search Active View
            if (searchQuery.isNotBlank()) {
                item {
                    Text(
                        text = "Search Results (${searchResults.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                if (searchResults.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.BookmarkBorder,
                            title = "No results found",
                            description = "No saved content matches \"$searchQuery\"."
                        )
                    }
                } else {
                    items(searchResults) { item ->
                        ContentCardList(
                            item = item,
                            onClick = { onOpenItem(item) },
                            onFavoriteToggle = { viewModel.toggleFavorite(item) },
                            onMenuClick = { selectedItemForActions = item },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                // Continue Watching / Reading Section (if available)
                if (continueItems.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleOutline,
                                contentDescription = null,
                                tint = extended.amberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 18.dp)
                        ) {
                            items(continueItems) { item ->
                                ContentCardGrid(
                                    item = item,
                                    onClick = { onOpenItem(item) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(item) },
                                    onMenuClick = { selectedItemForActions = item },
                                    modifier = Modifier.width(220.dp)
                                )
                            }
                        }
                    }
                }

                // Your Playlists Section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Playlists",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "+ New",
                            style = MaterialTheme.typography.labelLarge,
                            color = extended.amberAccent,
                            modifier = Modifier
                                .clickable { showCreatePlaylistDialog = true }
                                .padding(4.dp)
                        )
                    }
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 22.dp)
                    ) {
                        // Quick Add Playlist Card
                        item {
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, extended.border, RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { showCreatePlaylistDialog = true }
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = extended.amberAccent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "New Playlist",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = extended.amberAccent
                                    )
                                }
                            }
                        }

                        items(playlists) { playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                onClick = { onNavigateToPlaylist(playlist.id) },
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(130.dp)
                            )
                        }
                    }
                }

                // Recent Saves Section
                item {
                    Text(
                        text = "Recent Saves",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                if (recentItems.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.BookmarkBorder,
                            title = "Your box is empty",
                            description = "Share content from any app (YouTube, Instagram, Browser) to save it for later.",
                            actionLabel = "Add Link Manually",
                            onActionClick = { showAddDialog = true }
                        )
                    }
                } else {
                    items(recentItems) { item ->
                        ContentCardList(
                            item = item,
                            onClick = { onOpenItem(item) },
                            onFavoriteToggle = { viewModel.toggleFavorite(item) },
                            onMenuClick = { selectedItemForActions = item },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
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
            onAddToPlaylist = { selectedItemForPlaylists = item },
            onToggleFavorite = { viewModel.toggleFavorite(item) },
            onToggleArchive = { viewModel.toggleArchive(item) },
            onEditNotes = { selectedItemForNotes = item },
            onMoveToTrash = { viewModel.moveToTrash(item.id) }
        )
    }

    // Manual Link / Note Dialog
    if (showAddDialog) {
        AddManualItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { url, title, notes ->
                viewModel.saveManualItem(url, title, notes)
                showAddDialog = false
            }
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
        AddToPlaylistDialog(
            allPlaylists = playlists.map { p ->
                com.laterbox.data.local.entity.PlaylistEntity(
                    id = p.id,
                    name = p.name,
                    description = p.description,
                    iconName = p.iconName,
                    isDefaultInbox = p.isDefaultInbox
                )
            },
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
}
