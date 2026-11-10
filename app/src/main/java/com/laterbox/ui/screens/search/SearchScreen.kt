package com.laterbox.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.LaterBoxApp
import com.laterbox.data.ai.SemanticSearchEngine
import com.laterbox.data.ai.network.GeminiApiClient
import com.laterbox.data.ai.security.SecureApiKeyStorage
import com.laterbox.data.local.entity.ReminderEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.SortOrder
import com.laterbox.data.model.ViewMode
import com.laterbox.data.repository.PlaylistRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import com.laterbox.ui.components.AddManualItemDialog
import com.laterbox.ui.components.AddToPlaylistDialog
import com.laterbox.ui.components.CompactSocialFeedCard
import com.laterbox.ui.components.ContentActionsSheet
import com.laterbox.ui.components.ContentCardGrid
import com.laterbox.ui.components.ContentCardList
import com.laterbox.ui.components.ContextualEmptyState
import com.laterbox.ui.components.CreatePlaylistDialog
import com.laterbox.ui.components.EditNotesDialog
import com.laterbox.ui.components.EmptyDashedCard
import com.laterbox.ui.components.GridListTogglePill
import com.laterbox.ui.components.LaterBoxSearchBar
import com.laterbox.ui.components.LaterBoxSnackbarController
import com.laterbox.ui.components.SortDropdownSelector
import com.laterbox.ui.components.ZeroSearchResultsState
import com.laterbox.ui.theme.LaterBoxThemeTokens
import com.laterbox.ui.theme.SerifHeadlineFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onOpenItem: (SavedItemEntity) -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val prefs = LaterBoxApp.instance.preferencesRepository
    val contentRepo = LaterBoxApp.instance.contentRepository
    val playlistRepo = LaterBoxApp.instance.playlistRepository
    val scope = rememberCoroutineScope()

    val viewMode by prefs.viewMode.collectAsState(initial = ViewMode.GRID)
    val sortOrder by prefs.sortOrder.collectAsState(initial = SortOrder.NEWEST)

    val database = LaterBoxApp.instance.database
    val reminderDao = database.reminderDao()
    val aiAnalysisDao = database.aiAnalysisDao()
    val secureStorage = remember { SecureApiKeyStorage.getInstance(context) }
    val semanticEngine = remember { SemanticSearchEngine(GeminiApiClient(), aiAnalysisDao) }

    var searchQuery by remember { mutableStateOf("") }
    val tokenSearchResults by contentRepo.searchItems(searchQuery).collectAsState(initial = emptyList())
    val matchingReminders by reminderDao.searchReminders(searchQuery).collectAsState(initial = emptyList())

    var semanticMatchedItems by remember { mutableStateOf<List<SavedItemEntity>>(emptyList()) }
    var isSemanticSearching by remember { mutableStateOf(false) }

    // Debounced semantic search using Gemini embeddings
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length >= 3 && secureStorage.hasApiKey()) {
            delay(400) // Debounce typing
            isSemanticSearching = true
            try {
                val apiKey = secureStorage.getApiKey() ?: ""
                val matches = semanticEngine.search(q, apiKey)
                val extraItems = mutableListOf<SavedItemEntity>()
                for (match in matches) {
                    val item = contentRepo.getItemById(match.itemId)
                    if (item != null && !item.isDeleted && !item.isArchived) {
                        extraItems.add(item)
                    }
                }
                semanticMatchedItems = extraItems
            } catch (_: Exception) {
                semanticMatchedItems = emptyList()
            } finally {
                isSemanticSearching = false
            }
        } else {
            semanticMatchedItems = emptyList()
            isSemanticSearching = false
        }
    }

    // Unified results: token matches first, then any additional semantic matches
    val unifiedResults = remember(tokenSearchResults, semanticMatchedItems) {
        val tokenIds = tokenSearchResults.map { it.id }.toSet()
        val uniqueSemantic = semanticMatchedItems.filter { it.id !in tokenIds }
        tokenSearchResults + uniqueSemantic
    }

    // Apply sort order
    val sortedResults = remember(unifiedResults, sortOrder) {
        when (sortOrder) {
            SortOrder.NEWEST -> unifiedResults.sortedByDescending { it.savedAt }
            SortOrder.OLDEST -> unifiedResults.sortedBy { it.savedAt }
            SortOrder.TITLE_AZ -> unifiedResults.sortedBy { it.title.lowercase() }
            SortOrder.LAST_OPENED -> unifiedResults.sortedByDescending { it.lastOpenedAt ?: it.savedAt }
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var selectedItemForActions by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForNotes by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForPlaylists by remember { mutableStateOf<SavedItemEntity?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = extended.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            // Header: "Search" + "X items" (Left) & [ Grid | List ] (Right)
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
                        text = "Search",
                        fontFamily = SerifHeadlineFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = extended.primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${sortedResults.size} items",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = extended.secondaryText
                        )
                        if (isSemanticSearching) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = extended.primaryBlack
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI search...",
                                style = MaterialTheme.typography.labelSmall,
                                color = extended.textSecondary
                            )
                        }
                    }
                }

                GridListTogglePill(
                    currentMode = viewMode,
                    onModeChanged = { newMode ->
                        scope.launch { prefs.setViewMode(newMode) }
                    }
                )
            }

            // Search Bar: "Search titles, notes, links"
            LaterBoxSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search titles, notes, links"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sort Selector: "Sort" + [ Newest first  v ]
            SortDropdownSelector(
                currentSort = sortOrder,
                onSortChanged = { newSort ->
                    scope.launch { prefs.setSortOrder(newSort) }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Matching Events & Reminders Section
            if (searchQuery.isNotBlank() && matchingReminders.isNotEmpty()) {
                Text(
                    text = "Matching Events (${matchingReminders.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = extended.primaryText,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(matchingReminders, key = { it.id }) { reminder ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = extended.cardBackground),
                            border = BorderStroke(1.dp, extended.border),
                            modifier = Modifier.width(220.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (reminder.eventType) {
                                            "BIRTHDAY" -> Icons.Default.Cake
                                            "ANNIVERSARY" -> Icons.Default.Favorite
                                            else -> Icons.Default.Event
                                        },
                                        contentDescription = null,
                                        tint = extended.primaryBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = reminder.eventTitle ?: "Event",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = extended.primaryBlack,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                reminder.personName?.let { person ->
                                    Text(
                                        text = person,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = extended.textSecondary,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(Date(reminder.dueAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extended.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Main Content: Empty State Card vs Zero Results vs Grid / List
            if (sortedResults.isEmpty()) {
                if (searchQuery.isNotBlank()) {
                    ZeroSearchResultsState(
                        query = searchQuery,
                        hasActiveFilters = false,
                        onClearSearch = { searchQuery = "" }
                    )
                } else {
                    ContextualEmptyState(
                        icon = Icons.Default.Search,
                        title = "Search everything you saved",
                        description = "Type words from a title, web address, or your personal notes to find saved content.",
                        primaryActionLabel = "Save something",
                        onPrimaryAction = { showAddDialog = true }
                    )
                }
            } else {
                if (viewMode == ViewMode.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(sortedResults, key = { it.id }) { item ->
                            ContentCardGrid(
                                item = item,
                                onClick = { onOpenItem(item) },
                                onFavoriteToggle = {
                                    scope.launch {
                                        contentRepo.toggleFavorite(item.id, item.isFavorite)
                                        LaterBoxSnackbarController.showMessage(
                                            if (!item.isFavorite) "Added to Favorites" else "Removed from Favorites"
                                        )
                                    }
                                },
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
                        items(sortedResults, key = { it.id }) { item ->
                            CompactSocialFeedCard(
                                item = item,
                                onClick = { onOpenItem(item) },
                                onFavoriteToggle = {
                                    scope.launch {
                                        contentRepo.toggleFavorite(item.id, item.isFavorite)
                                        LaterBoxSnackbarController.showMessage(
                                            if (!item.isFavorite) "Added to Favorites" else "Removed from Favorites"
                                        )
                                    }
                                },
                                onAddToPlaylist = { selectedItemForPlaylists = item },
                                onShare = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, item.url.ifEmpty { item.title })
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
            onToggleFavorite = {
                scope.launch {
                    contentRepo.toggleFavorite(item.id, item.isFavorite)
                    LaterBoxSnackbarController.showMessage(
                        if (!item.isFavorite) "Added to Favorites" else "Removed from Favorites"
                    )
                }
            },
            onToggleArchive = {
                scope.launch {
                    contentRepo.toggleArchive(item.id, item.isArchived)
                    LaterBoxSnackbarController.showMessage(
                        if (!item.isArchived) "Item archived" else "Item unarchived"
                    )
                }
            },
            onEditNotes = { selectedItemForNotes = item },
            onMoveToTrash = {
                scope.launch {
                    contentRepo.softDelete(item.id)
                    LaterBoxSnackbarController.showMessage(
                        message = "Moved to Trash",
                        actionLabel = "Undo",
                        onAction = {
                            scope.launch {
                                contentRepo.restoreFromTrash(item.id)
                            }
                        }
                    )
                }
            }
        )
    }

    // Manual Save Dialog
    if (showAddDialog) {
        AddManualItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { url, title, notes ->
                scope.launch {
                    contentRepo.saveContent(
                        url = url,
                        title = title,
                        notes = notes
                    )
                    LaterBoxSnackbarController.showMessage("Saved to Later Box")
                }
                showAddDialog = false
            }
        )
    }

    // Edit Notes Dialog
    selectedItemForNotes?.let { item ->
        EditNotesDialog(
            initialNotes = item.notes,
            onDismiss = { selectedItemForNotes = null },
            onSave = { updatedNotes ->
                scope.launch {
                    contentRepo.updateNotes(item.id, updatedNotes)
                }
                selectedItemForNotes = null
            }
        )
    }

    // Add to Playlist Dialog
    selectedItemForPlaylists?.let { item ->
        SearchPlaylistDialogHost(
            item = item,
            playlistRepo = playlistRepo,
            onDismiss = { selectedItemForPlaylists = null },
            onCreateNewPlaylist = { showCreatePlaylistDialog = true }
        )
    }

    val allSearchPlaylists by playlistRepo.getAllPlaylists().collectAsState(initial = emptyList())

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            existingNames = allSearchPlaylists.map { it.name },
            onConfirm = { name, desc, icon ->
                scope.launch {
                    playlistRepo.createPlaylist(name, desc, icon)
                    LaterBoxSnackbarController.showMessage("Created collection \"$name\"")
                }
                showCreatePlaylistDialog = false
            }
        )
    }
}

@Composable
private fun SearchPlaylistDialogHost(
    item: SavedItemEntity,
    playlistRepo: PlaylistRepository,
    onDismiss: () -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val allPlaylists by playlistRepo.getAllPlaylists().collectAsState(initial = emptyList())
    val currentPlaylists by playlistRepo.getPlaylistsForItem(item.id).collectAsState(initial = emptyList())

    AddToPlaylistDialog(
        allPlaylists = allPlaylists,
        initiallySelectedIds = currentPlaylists.map { it.id },
        onDismiss = onDismiss,
        onSavePlaylists = { pIds ->
            scope.launch {
                playlistRepo.updatePlaylistsForItem(item.id, pIds)
            }
            onDismiss()
        },
        onCreateNewPlaylist = onCreateNewPlaylist
    )
}
