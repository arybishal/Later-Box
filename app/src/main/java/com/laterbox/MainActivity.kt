package com.laterbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
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
import com.laterbox.ui.navigation.MAIN_BOTTOM_NAV_ROUTES
import com.laterbox.ui.navigation.Screen
import com.laterbox.ui.screens.home.HomeScreen
import com.laterbox.ui.screens.library.LibraryScreen
import com.laterbox.ui.screens.playlist.PlaylistDetailScreen
import com.laterbox.ui.screens.playlist.PlaylistsScreen
import com.laterbox.ui.screens.search.SearchScreen
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

    var showAddDialog by remember { mutableStateOf(false) }

    val showBottomBar = currentRoute in MAIN_BOTTOM_NAV_ROUTES

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val borderColor = extended.border
                val surfaceColor = MaterialTheme.colorScheme.surface
                val activeColor = MaterialTheme.colorScheme.onBackground
                val inactiveColor = extended.textSecondary

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor)
                        .drawBehind {
                            drawLine(
                                color = borderColor,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .navigationBarsPadding()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Home
                    val isHome = currentRoute == Screen.Home.route
                    BottomNavTab(
                        label = "Home",
                        isSelected = isHome,
                        activeColor = activeColor,
                        inactiveColor = inactiveColor,
                        onClick = {
                            if (!isHome) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { tint ->
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = "Home",
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )

                    // 2. Library
                    val isLibrary = currentRoute == Screen.Library.route
                    BottomNavTab(
                        label = "Library",
                        isSelected = isLibrary,
                        activeColor = activeColor,
                        inactiveColor = inactiveColor,
                        onClick = {
                            if (!isLibrary) {
                                navController.navigate(Screen.Library.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { tint ->
                            Canvas(modifier = Modifier.size(24.dp)) {
                                val strokeW = 1.8.dp.toPx()
                                val topY = size.height * 0.16f
                                val botY = size.height * 0.84f
                                drawLine(tint, Offset(size.width * 0.22f, topY), Offset(size.width * 0.22f, botY), strokeW)
                                drawLine(tint, Offset(size.width * 0.38f, topY), Offset(size.width * 0.38f, botY), strokeW)
                                drawLine(tint, Offset(size.width * 0.54f, topY), Offset(size.width * 0.54f, botY), strokeW)
                                drawLine(tint, Offset(size.width * 0.68f, topY + 2f), Offset(size.width * 0.84f, botY), strokeW)
                            }
                        }
                    )

                    // 3. Save
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showAddDialog = true }
                            .padding(horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(extended.amberAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Save",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = activeColor
                        )
                    }

                    // 4. Search
                    val isSearch = currentRoute == Screen.Search.route
                    BottomNavTab(
                        label = "Search",
                        isSelected = isSearch,
                        activeColor = activeColor,
                        inactiveColor = inactiveColor,
                        onClick = {
                            if (!isSearch) {
                                navController.navigate(Screen.Search.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { tint ->
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )

                    // 5. Settings
                    val isSettings = currentRoute == Screen.Settings.route
                    BottomNavTab(
                        label = "Settings",
                        isSelected = isSettings,
                        activeColor = activeColor,
                        inactiveColor = inactiveColor,
                        onClick = {
                            if (!isSettings) {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { tint ->
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToPlaylist = { playlistId ->
                        navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                    },
                    onOpenItem = { item ->
                        navController.navigate(Screen.ContentViewer.createRoute(item.id))
                    }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    onOpenItem = { item ->
                        navController.navigate(Screen.ContentViewer.createRoute(item.id))
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onOpenItem = { item ->
                        navController.navigate(Screen.ContentViewer.createRoute(item.id))
                    }
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
                    onOpenItem = { item ->
                        navController.navigate(Screen.ContentViewer.createRoute(item.id))
                    }
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

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToTrash = { navController.navigate(Screen.Trash.route) }
                )
            }
        }
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
}

@Composable
private fun BottomNavTab(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    icon: @Composable (tint: Color) -> Unit
) {
    val color = if (isSelected) activeColor else inactiveColor
    val weight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        icon(color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = weight,
            color = color
        )
    }
}
