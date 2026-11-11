package com.laterbox

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.laterbox.data.update.UpdateNotificationHelper
import com.laterbox.data.update.UpdateInfo
import com.laterbox.ui.components.UpdateDetailsDialog
import kotlinx.coroutines.flow.first
import com.laterbox.ui.screens.detail.ItemDetailScreen
import com.laterbox.ui.screens.note.RichNoteEditorScreen
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import com.laterbox.ui.screens.events.EventsScreen
import com.laterbox.ui.screens.snap.SnapQuickCaptureSheet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ThemeMode
import com.laterbox.ui.components.AddManualItemDialog
import com.laterbox.ui.components.LaterBoxSnackbarController
import com.laterbox.ui.components.LiquidGlassBottomBar
import com.laterbox.ui.components.OfflineNoticeBanner
import com.laterbox.ui.navigation.MAIN_BOTTOM_NAV_ROUTES
import com.laterbox.ui.navigation.Screen
import com.laterbox.ui.screens.home.HomeScreen
import com.laterbox.ui.screens.library.LibraryScreen
import com.laterbox.ui.screens.playlist.PlaylistDetailScreen
import com.laterbox.ui.screens.playlist.PlaylistsScreen
import com.laterbox.ui.screens.search.SearchScreen
import com.laterbox.ui.screens.settings.GeminiSettingsScreen
import com.laterbox.ui.screens.settings.SettingsScreen
import com.laterbox.ui.screens.trash.TrashScreen
import com.laterbox.ui.screens.viewer.ContentViewerScreen
import com.laterbox.ui.theme.LaterBoxTheme
import com.laterbox.ui.theme.LaterBoxThemeTokens
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Request maximum available refresh rate (e.g. 120Hz / 90Hz) for fluid UI performance
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.decorView.post {
                val currentDisplay = display
                val modes = currentDisplay?.supportedModes
                val maxMode = modes?.maxByOrNull { it.refreshRate }
                if (maxMode != null) {
                    val params = window.attributes
                    params.preferredDisplayModeId = maxMode.modeId
                    window.attributes = params
                }
            }
        }

        setContent {
            val themeMode by LaterBoxApp.instance.preferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            LaterBoxTheme(themeMode = themeMode) {
                MainAppScaffold()
            }
        }
    }
}

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val extended = LaterBoxThemeTokens.extended
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity

    var showAddDialog by remember { mutableStateOf(false) }
    var showSnapSheet by remember { mutableStateOf(false) }

    val updateManager = remember { LaterBoxApp.instance.updateManager }
    val userPrefs = remember { LaterBoxApp.instance.preferencesRepository }
    var availableUpdateForDialog by remember { mutableStateOf<UpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val completedFirstLaunch = userPrefs.hasCompletedFirstLaunchCheck.first()
        if (!completedFirstLaunch) {
            val result = updateManager.checkForUpdates()
            result.onSuccess { updateInfo ->
                userPrefs.setCompletedFirstLaunchCheck(true)
                if (updateInfo != null && updateInfo.isNewer) {
                    availableUpdateForDialog = updateInfo
                }
            }.onFailure {
                // If offline or network error on first launch, allow checking next launch
            }
        }
    }

    LaunchedEffect(activity?.intent) {
        val navigateToUpdates = activity?.intent?.getBooleanExtra(
            UpdateNotificationHelper.EXTRA_NAVIGATE_TO_UPDATES,
            false
        ) ?: false
        if (navigateToUpdates) {
            activity?.intent?.removeExtra(UpdateNotificationHelper.EXTRA_NAVIGATE_TO_UPDATES)
            navController.navigate(Screen.Settings.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        val openSnap = activity?.intent?.getBooleanExtra(
            com.laterbox.service.SnapTileService.EXTRA_OPEN_SNAP,
            false
        ) ?: false
        if (openSnap) {
            activity?.intent?.removeExtra(com.laterbox.service.SnapTileService.EXTRA_OPEN_SNAP)
            showSnapSheet = true
        }

        val openItemId = activity?.intent?.getLongExtra("open_item_id", -1L) ?: -1L
        if (openItemId != -1L) {
            activity?.intent?.removeExtra("open_item_id")
            navController.navigate(Screen.ItemDetail.createRoute(openItemId))
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val isOnline by LaterBoxApp.instance.networkMonitor.isOnline.collectAsState()
    var dismissOfflineBanner by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarHostState) {
        LaterBoxSnackbarController.observeAndShow(snackbarHostState, scope)
    }

    val showBottomBar = currentRoute in MAIN_BOTTOM_NAV_ROUTES || currentRoute?.startsWith("library") == true

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = extended.cardBackground,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    actionColor = extended.primaryText,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .padding(bottom = if (showBottomBar) 84.dp else 0.dp)
                        .border(1.dp, extended.border, RoundedCornerShape(12.dp))
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val context = androidx.compose.ui.platform.LocalContext.current
        val handleOpenItem: (SavedItemEntity) -> Unit = { item ->
            if (item.contentType == com.laterbox.data.model.ContentType.NOTE) {
                navController.navigate(Screen.NoteEditor.createRoute(item.id, initialEdit = false))
            } else {
                navController.navigate(Screen.ItemDetail.createRoute(item.id))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (!isOnline && !dismissOfflineBanner) {
                    OfflineNoticeBanner(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        onDismiss = { dismissOfflineBanner = true }
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToLibrary = { navController.navigate(Screen.Library.createRoute("All")) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToPlaylist = { playlistId ->
                        navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                    },
                    onNavigateToFavorites = {
                        navController.navigate(Screen.Library.createRoute("Favorites"))
                    },
                    onNavigateToEvents = {
                        navController.navigate(Screen.Events.route)
                    },
                    onOpenItem = handleOpenItem,
                    onOpenNoteEditor = { noteId, isEdit ->
                        navController.navigate(Screen.NoteEditor.createRoute(noteId, isEdit))
                    }
                )
            }

            composable(
                route = Screen.Library.route,
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                        defaultValue = "All"
                    }
                )
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: "All"
                LibraryScreen(
                    initialCategory = category,
                    onOpenItem = handleOpenItem,
                    onOpenNoteEditor = { noteId, isEdit ->
                        navController.navigate(Screen.NoteEditor.createRoute(noteId, isEdit))
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onOpenItem = handleOpenItem
                )
            }

            composable(Screen.Playlists.route) {
                PlaylistsScreen(
                    onNavigateToPlaylist = { playlistId ->
                        navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                    }
                )
            }

            composable(
                route = Screen.PlaylistDetail.route,
                arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                PlaylistDetailScreen(
                    playlistId = playlistId,
                    onBack = { navController.popBackStack() },
                    onOpenItem = handleOpenItem
                )
            }

            composable(
                route = Screen.ContentViewer.route,
                arguments = listOf(navArgument("contentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val contentId = backStackEntry.arguments?.getLong("contentId") ?: 0L
                var item by remember(contentId) {
                    mutableStateOf<SavedItemEntity?>(null)
                }

                androidx.compose.runtime.LaunchedEffect(contentId) {
                    item = LaterBoxApp.instance.contentRepository.getItemById(contentId)
                }

                item?.let { validItem ->
                    ContentViewerScreen(
                        item = validItem,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Trash.route) {
                TrashScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ItemDetail.route,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
                ItemDetailScreen(
                    itemId = itemId,
                    onBack = { navController.popBackStack() },
                    onOpenViewer = { item ->
                        navController.navigate(Screen.ContentViewer.createRoute(item.id))
                    },
                    onOpenNoteEditor = { noteItemId, isEdit ->
                        navController.navigate(Screen.NoteEditor.createRoute(noteItemId, isEdit))
                    },
                    onNavigateToAiSettings = {
                        navController.navigate(Screen.GeminiSettings.route)
                    }
                )
            }

            composable(
                route = Screen.NoteEditor.route,
                arguments = listOf(
                    navArgument("itemId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                    navArgument("initialEdit") {
                        type = NavType.BoolType
                        defaultValue = true
                    }
                )
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
                val initialEdit = backStackEntry.arguments?.getBoolean("initialEdit") ?: true
                RichNoteEditorScreen(
                    itemId = itemId,
                    initialEdit = initialEdit,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Events.route) {
                EventsScreen(
                    onOpenItem = { itemId ->
                        navController.navigate(Screen.ItemDetail.createRoute(itemId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = null,
                    onNavigateToTrash = { navController.navigate(Screen.Trash.route) },
                    onNavigateToGeminiSettings = { navController.navigate(Screen.GeminiSettings.route) },
                    onNavigateToDemoDataLab = { navController.navigate(Screen.DemoDataLab.route) }
                )
            }

            composable(Screen.DemoDataLab.route) {
                com.laterbox.ui.screens.developer.DemoDataLabScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.GeminiSettings.route) {
                GeminiSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showBottomBar) {
        LiquidGlassBottomBar(
            currentRoute = currentRoute,
            onNavigateToHome = {
                if (currentRoute != Screen.Home.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            onNavigateToEvents = {
                if (currentRoute != Screen.Events.route) {
                    navController.navigate(Screen.Events.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            onNavigateToLibrary = {
                val isLibrary = currentRoute == Screen.Library.route || currentRoute?.startsWith("library") == true
                if (!isLibrary) {
                    navController.navigate(Screen.Library.createRoute("All")) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            onNavigateToSettings = {
                if (currentRoute != Screen.Settings.route) {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            onSnapClick = { showSnapSheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}
}

    if (showSnapSheet) {
        SnapQuickCaptureSheet(
            onDismiss = { showSnapSheet = false },
            onItemCaptured = { item ->
                navController.navigate(Screen.ItemDetail.createRoute(item.id))
            },
            onOpenRichEditor = { noteId ->
                navController.navigate(Screen.NoteEditor.createRoute(noteId, true))
            }
        )
    }

    if (showAddDialog) {
        AddManualItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { url, title, notes ->
                scope.launch {
                    LaterBoxApp.instance.contentRepository.saveContent(
                        url = url,
                        title = title,
                        notes = notes
                    )
                    showAddDialog = false
                }
            }
        )
    }

    availableUpdateForDialog?.let { updateInfo ->
        UpdateDetailsDialog(
            updateInfo = updateInfo,
            currentVersionName = updateManager.currentVersionName,
            currentVersionCode = updateManager.currentVersionCode,
            onDismiss = { availableUpdateForDialog = null },
            onStartDownload = {
                availableUpdateForDialog = null
                navController.navigate(Screen.Settings.route)
                updateManager.startDownload(updateInfo)
            }
        )
    }
}
