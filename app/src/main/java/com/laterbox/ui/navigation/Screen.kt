package com.laterbox.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Library : Screen("library?category={category}", "Library") {
        fun createRoute(category: String = "All") = "library?category=$category"
    }
    object Search : Screen("search", "Search")
    object Settings : Screen("settings", "Settings")
    object Playlists : Screen("playlists", "Playlists")
    object PlaylistDetail : Screen("playlist/{playlistId}", "Playlist") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    object ContentViewer : Screen("viewer/{contentId}", "Viewer") {
        fun createRoute(contentId: Long) = "viewer/$contentId"
    }
    object Trash : Screen("trash", "Trash")
}

val MAIN_BOTTOM_NAV_ROUTES = listOf(
    Screen.Home.route,
    Screen.Library.route,
    Screen.Search.route,
    Screen.Settings.route
)
