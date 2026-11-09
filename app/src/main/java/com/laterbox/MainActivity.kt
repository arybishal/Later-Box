package com.laterbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ThemeMode
import com.laterbox.ui.navigation.BOTTOM_NAV_ITEMS
import com.laterbox.ui.navigation.Screen
import com.laterbox.ui.screens.home.HomeScreen
import com.laterbox.ui.screens.library.LibraryScreen
import com.laterbox.ui.screens.playlist.PlaylistDetailScreen
import com.laterbox.ui.screens.playlist.PlaylistsScreen
import com.laterbox.ui.screens.settings.SettingsScreen
import com.laterbox.ui.screens.trash.TrashScreen
import com.laterbox.ui.screens.viewer.ContentViewerScreen
import com.laterbox.ui.theme.LaterBoxTheme
import com.laterbox.ui.theme.LaterBoxThemeTokens

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

    val showBottomBar = currentRoute in BOTTOM_NAV_ITEMS.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val borderColor = extended.border
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .drawBehind {
                            drawLine(
                                color = borderColor,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                ) {
                    BOTTOM_NAV_ITEMS.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = extended.amberAccent,
                                selectedTextColor = extended.amberAccent,
                                unselectedIconColor = extended.textSecondary,
                                unselectedTextColor = extended.textSecondary,
                                indicatorColor = extended.amberAccentSubtle
                            )
                        )
                    }
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
                var item by androidx.compose.runtime.remember(contentId) {
                    androidx.compose.runtime.mutableStateOf<SavedItemEntity?>(null)
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
}
