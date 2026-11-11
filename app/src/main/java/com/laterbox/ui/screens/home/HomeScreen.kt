package com.laterbox.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.laterbox.LaterBoxApp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.laterbox.ui.components.ZeroSearchResultsState
import com.laterbox.data.ai.security.SecureApiKeyStorage
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import com.laterbox.ui.components.AddManualItemDialog
import com.laterbox.ui.components.AddToPlaylistDialog
import com.laterbox.ui.components.ContentActionsSheet
import com.laterbox.ui.components.CreatePlaylistDialog
import com.laterbox.ui.components.EditNotesDialog
import com.laterbox.ui.components.LaterBoxSnackbarController
import com.laterbox.ui.theme.LaterBoxThemeTokens
import com.laterbox.ui.theme.SerifHeadlineFontFamily
import kotlinx.coroutines.launch

/**
 * Later Box v2.0.0 Home Screen
 * Redesigned aesthetic: Soft. Minimal. Premium. Spacious. Modern. Content-focused.
 * Features:
 * - Header with contextual tag, title, dynamic count, circular search and settings actions.
 * - Dynamic Activity summary card with real metrics.
 * - Horizontal scrolling Collections section with white 24dp rounded cards.
 * - Memories section with dynamic filter chips row.
 * - Left-text / right-thumbnail compact white rounded saved content cards.
 * - Floating pill-shaped "Ask questions" AI assistant connected to Gemini API.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToFavorites: () -> Unit = onNavigateToLibrary,
    onNavigateToEvents: () -> Unit = {},
    onOpenItem: (SavedItemEntity) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allItems by viewModel.allItems.collectAsState()
    val recentItems by viewModel.recentItems.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val favoriteCount by viewModel.favoriteCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val upcomingReminders by LaterBoxApp.instance.database.reminderDao().getAllUpcomingReminders().collectAsState(emptyList())

    val isAiEnabled by viewModel.isAiEnabled.collectAsState()
    val isAiQuerying by viewModel.isAiQuerying.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val aiError by viewModel.aiError.collectAsState()
    val hasApiKey = remember(context) { SecureApiKeyStorage.getInstance(context).hasApiKey() }

    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }
    var selectedItemForActions by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForNotes by remember { mutableStateOf<SavedItemEntity?>(null) }
    var selectedItemForPlaylists by remember { mutableStateOf<SavedItemEntity?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val aiSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Compute dynamic filter counts from all items
    val snapCount = remember(allItems) { allItems.count { it.isSnap } }
    val articlesCount = remember(allItems) { allItems.count { it.contentType == ContentType.ARTICLE } }
    val videosCount = remember(allItems) { allItems.count { it.contentType == ContentType.VIDEO } }
    val imagesCount = remember(allItems) { allItems.count { it.contentType == ContentType.IMAGE } }
    val notesCount = remember(allItems) { allItems.count { it.contentType == ContentType.NOTE } }

    // Filter feed items according to the active chip
    val feedItems = remember(allItems, selectedFilter) {
        when (selectedFilter) {
            "All" -> allItems.take(25)
            "Snap ⚡", "Snap" -> allItems.filter { it.isSnap }
            "Favorites" -> allItems.filter { it.isFavorite }
            "Articles" -> allItems.filter { it.contentType == ContentType.ARTICLE }
            "Videos" -> allItems.filter { it.contentType == ContentType.VIDEO }
            "Images" -> allItems.filter { it.contentType == ContentType.IMAGE }
            "Notes" -> allItems.filter { it.contentType == ContentType.NOTE }
            else -> allItems.take(25)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = extended.background
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp)
            ) {
                // 3.1 Header
                item {
                    HomeHeader(
                        totalCount = totalCount,
                        onNavigateToSearch = onNavigateToSearch,
                        onNavigateToSettings = onNavigateToSettings
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 3.2 Dynamic Activity & Welcome Area
                item {
                    HomeActivitySummaryCard(
                        totalCount = totalCount,
                        favoriteCount = favoriteCount,
                        collectionsCount = playlists.size,
                        latestItem = recentItems.firstOrNull(),
                        onOpenLatest = recentItems.firstOrNull()?.let { { onOpenItem(it) } }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 3.2.1 Upcoming Events Preview (if any)
                if (upcomingReminders.isNotEmpty()) {
                    item {
                        HomeUpcomingEventsCard(
                            upcomingReminders = upcomingReminders,
                            onNavigateToEvents = onNavigateToEvents
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // 3.3 Collections Section
                item {
                    HomeCollectionsSection(
                        totalCount = totalCount,
                        favoriteCount = favoriteCount,
                        playlists = playlists,
                        onNavigateToLibrary = onNavigateToLibrary,
                        onNavigateToFavorites = onNavigateToFavorites,
                        onNavigateToPlaylist = onNavigateToPlaylist,
                        onCreatePlaylist = { showCreatePlaylistDialog = true }
                    )
                    Spacer(modifier = Modifier.height(26.dp))
                }

                // 3.4 Memories Section Header & Filter Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Memories",
                            fontFamily = SerifHeadlineFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = extended.primaryText
                        )

                        Text(
                            text = "View all →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extended.interactiveAccent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onNavigateToLibrary() }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HomeFilterChipsRow(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it },
                        totalCount = totalCount,
                        favoriteCount = favoriteCount,
                        articlesCount = articlesCount,
                        videosCount = videosCount,
                        imagesCount = imagesCount,
                        notesCount = notesCount,
                        snapCount = snapCount
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 3.5 Saved Content Cards Feed
                if (feedItems.isEmpty()) {
                    item {
                        if (allItems.isEmpty()) {
                            HomeEmptyState(onSaveClick = { showAddDialog = true })
                        } else {
                            ZeroSearchResultsState(
                                query = "",
                                hasActiveFilters = true,
                                onClearSearch = { selectedFilter = "All" },
                                onResetFilters = { selectedFilter = "All" }
                            )
                        }
                    }
                } else {
                    items(feedItems, key = { it.id }) { item ->
                        MemorySavedContentCard(
                            item = item,
                            onClick = { onOpenItem(item) },
                            onFavoriteToggle = {
                                viewModel.toggleFavorite(item)
                                LaterBoxSnackbarController.showMessage(
                                    if (!item.isFavorite) "Added to Favorites" else "Removed from Favorites"
                                )
                            },
                            onMenuClick = { selectedItemForActions = item },
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // 3.6 Floating AI Assistant: Pill-shaped elevated action floating above content
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            FloatingAiAssistantPill(
                onClick = { showAiSheet = true }
            )
        }
    }

    // Modal Gemini AI Assistant Sheet
    if (showAiSheet) {
        AskGeminiAssistantSheet(
            sheetState = aiSheetState,
            isAiEnabled = isAiEnabled,
            hasApiKey = hasApiKey,
            isAiQuerying = isAiQuerying,
            aiResponse = aiResponse,
            aiError = aiError,
            onAskQuestion = { question ->
                viewModel.askGemini(question)
            },
            onNavigateToSettings = {
                showAiSheet = false
                onNavigateToSettings()
            },
            onDismiss = {
                showAiSheet = false
                viewModel.clearAiResponse()
            }
        )
    }

    // Modal dialogs and action sheets (preserving existing business logic)
    if (showAddDialog) {
        AddManualItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { url, title, notes ->
                viewModel.saveManualItem(url, title, notes)
                LaterBoxSnackbarController.showMessage("Saved to Later Box")
                showAddDialog = false
            }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            existingNames = playlists.map { it.name },
            onConfirm = { name, desc, icon ->
                viewModel.createPlaylist(name, desc, icon)
                LaterBoxSnackbarController.showMessage("Created collection \"$name\"")
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
                LaterBoxSnackbarController.showMessage(
                    if (!item.isFavorite) "Added to Favorites" else "Removed from Favorites"
                )
                selectedItemForActions = null
            },
            onToggleArchive = {
                viewModel.toggleArchive(item)
                LaterBoxSnackbarController.showMessage(
                    if (!item.isArchived) "Item archived" else "Item unarchived"
                )
                selectedItemForActions = null
            },
            onEditNotes = {
                selectedItemForNotes = item
                selectedItemForActions = null
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
