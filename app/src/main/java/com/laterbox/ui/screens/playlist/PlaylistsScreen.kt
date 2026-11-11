package com.laterbox.ui.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.laterbox.ui.components.glass.GlassFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.laterbox.LaterBoxApp
import com.laterbox.ui.components.CreatePlaylistDialog
import com.laterbox.ui.components.LaterBoxHeader
import com.laterbox.ui.components.LaterBoxSnackbarController
import com.laterbox.ui.components.PlaylistCard
import com.laterbox.ui.theme.LaterBoxThemeTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PlaylistsScreen(
    onNavigateToPlaylist: (Long) -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    val scope = rememberCoroutineScope()
    val playlists by LaterBoxApp.instance.playlistRepository.getAllPlaylistsWithCount().collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LaterBoxHeader(
                greeting = "Playlists",
                subtitle = "${playlists.size} collections"
            )
        },
        floatingActionButton = {
            GlassFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Create Playlist"
            )
        },
        containerColor = extended.background
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 88.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // New playlist quick card
            item {
                Box(
                    modifier = Modifier
                        .height(130.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, extended.border, RoundedCornerShape(24.dp))
                        .background(extended.cardBackground)
                        .clickable { showCreateDialog = true }
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
                            tint = extended.primaryText,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "New Playlist",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = extended.primaryText
                        )
                    }
                }
            }

            items(playlists, key = { it.id }) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    onClick = { onNavigateToPlaylist(playlist.id) },
                    modifier = Modifier.height(130.dp)
                )
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            existingNames = playlists.map { it.name },
            onConfirm = { name, desc, icon ->
                scope.launch {
                    LaterBoxApp.instance.playlistRepository.createPlaylist(name, desc, icon)
                    LaterBoxSnackbarController.showMessage("Created collection \"$name\"")
                }
                showCreateDialog = false
            }
        )
    }
}
