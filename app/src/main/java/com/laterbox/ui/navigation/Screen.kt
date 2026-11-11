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
    object ItemDetail : Screen("detail/{itemId}", "Item Detail") {
        fun createRoute(itemId: Long) = "detail/$itemId"
    }
    object GeminiSettings : Screen("settings/gemini", "Gemini AI")
    object Trash : Screen("trash", "Trash")
    object Events : Screen("events", "Events")
    object NoteEditor : Screen("note_editor?itemId={itemId}&initialEdit={initialEdit}", "Note") {
        fun createRoute(itemId: Long = 0L, initialEdit: Boolean = true) = "note_editor?itemId=$itemId&initialEdit=$initialEdit"
    }
}

val MAIN_BOTTOM_NAV_ROUTES = listOf(
    Screen.Home.route,
    Screen.Library.route,
    Screen.Events.route,
    Screen.Settings.route
)
