package com.laterbox.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Library : Screen("library", "Library", Icons.Default.Bookmark)
    object Playlists : Screen("playlists", "Playlists", Icons.Default.Layers)
    object PlaylistDetail : Screen("playlist/{playlistId}", "Playlist") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    object ContentViewer : Screen("viewer/{contentId}", "Viewer") {
        fun createRoute(contentId: Long) = "viewer/$contentId"
    }
    object Trash : Screen("trash", "Trash")
    object Settings : Screen("settings", "Settings")
}

val BOTTOM_NAV_ITEMS = listOf(
    Screen.Home,
    Screen.Library,
    Screen.Playlists
)
