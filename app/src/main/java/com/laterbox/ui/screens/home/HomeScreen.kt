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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.ui.components.AddManualItemDialog
import com.laterbox.ui.components.AddToPlaylistDialog
import com.laterbox.ui.components.CompactSocialFeedCard
import com.laterbox.ui.components.ContentActionsSheet
import com.laterbox.ui.components.ContinueWatchingCard
import com.laterbox.ui.components.CreatePlaylistDialog
import com.laterbox.ui.components.EditNotesDialog
import com.laterbox.ui.components.EmptyDashedCard
import com.laterbox.ui.components.FavoritesCollectionCard
import com.laterbox.ui.components.PlaylistCard
import com.laterbox.ui.theme.LaterBoxThemeTokens
import com.laterbox.ui.theme.SerifHeadlineFontFamily
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
    onNavigateToLibrary: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToFavorites: () -> Unit = onNavigateToLibrary,
    onOpenItem: (SavedItemEntity) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val recentItems by viewModel.recentItems.collectAsState()
    val continueItems by viewModel.continueItems.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val favoriteCount by viewModel.favoriteCount.collectAsState()

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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp)
        ) {
            // Header: "Save now. Enjoy later." + "Good evening" + Avatar Circle "L"
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Save now. Enjoy later.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = extended.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = getGreeting(),
                            fontFamily = SerifHeadlineFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            lineHeight = 38.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Avatar Circle "L" (Clickable to open Settings)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.dp, extended.border, CircleShape)
                            .background(extended.cardBackground)
                            .clickable { onNavigateToSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "L",
                            fontFamily = SerifHeadlineFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Search Bar: "Search your library"
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, extended.border, RoundedCornerShape(14.dp))
                        .background(extended.cardBackground)
                        .clickable { onNavigateToSearch() }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Search your library",
                        fontSize = 15.sp,
                        color = extended.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Section: Continue reading & watching (if any)
            if (continueItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Continue reading & watching",
                        fontFamily = SerifHeadlineFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        items(continueItems, key = { it.id }) { item ->
                            ContinueWatchingCard(
                                item = item,
                                onClick = { onOpenItem(item) }
                            )
                        }
                    }
                }
            }

            // Section: Your playlists
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your playlists",
                        fontFamily = SerifHeadlineFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Button(
                        onClick = { showCreatePlaylistDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extended.amberAccent,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "New",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Playlists are optional. Saves without one stay in your ",
                        fontSize = 13.sp,
                        color = extended.textSecondary
                    )
                    Text(
                        text = "Inbox.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            val inbox = playlists.find { it.isDefaultInbox }
                            if (inbox != null) onNavigateToPlaylist(inbox.id)
                            else onNavigateToLibrary()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Collections list (Smart Favorites collection first, then user playlists)
            val customPlaylists = playlists.filter { !it.isDefaultInbox }
            if (customPlaylists.isNotEmpty() || favoriteCount > 0) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 28.dp)
                    ) {
                        if (favoriteCount > 0) {
                            item {
                                FavoritesCollectionCard(
                                    count = favoriteCount,
                                    onClick = onNavigateToFavorites,
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(115.dp)
                                )
                            }
                        }
                        items(customPlaylists) { playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                onClick = { onNavigateToPlaylist(playlist.id) },
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(115.dp)
                            )
                        }
                    }
                }
            }

            // Section: Recent saves
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent saves",
                        fontFamily = SerifHeadlineFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Library",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable { onNavigateToLibrary() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Empty state card or recent items
            if (recentItems.isEmpty()) {
                item {
                    EmptyDashedCard(
                        title = "Nothing saved yet",
                        description = "Paste a link from Instagram, YouTube, a browser, or anywhere else. Later Box keeps the original address even if the preview cannot be read.",
                        buttonLabel = "Save something",
                        onButtonClick = { showAddDialog = true }
                    )
                }
            } else {
                items(recentItems, key = { it.id }) { item ->
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
                        onMenuClick = { selectedItemForActions = item },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
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
        HomePlaylistDialogHost(
            item = item,
            viewModel = viewModel,
            playlists = playlists,
            onDismiss = { selectedItemForPlaylists = null },
            onCreateNewPlaylist = { showCreatePlaylistDialog = true }
        )
    }
}

@Composable
private fun HomePlaylistDialogHost(
    item: SavedItemEntity,
    viewModel: HomeViewModel,
    playlists: List<com.laterbox.data.local.relation.PlaylistWithCount>,
    onDismiss: () -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    val currentPlaylists by viewModel.getPlaylistsForItem(item.id).collectAsState(initial = emptyList())
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
        initiallySelectedIds = currentPlaylists.map { it.id },
        onDismiss = onDismiss,
        onSavePlaylists = { pIds ->
            viewModel.updatePlaylistsForItem(item.id, pIds)
            onDismiss()
        },
        onCreateNewPlaylist = onCreateNewPlaylist
    )
}
