package com.laterbox.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laterbox.LaterBoxApp
import com.laterbox.data.model.ThemeMode
import com.laterbox.ui.components.ConfirmationDialog
import com.laterbox.ui.components.LaterBoxHeader
import com.laterbox.ui.theme.LaterBoxThemeTokens
import kotlinx.coroutines.launch

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToTrash: () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs = LaterBoxApp.instance.preferencesRepository
    val storageManager = LaterBoxApp.instance.mediaStorageManager

    val themeMode by prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val autoFetchMetadata by prefs.autoFetchMetadata.collectAsState(initial = true)
    val directSaveToInbox by prefs.directSaveToInbox.collectAsState(initial = false)
    val inAppViewing by prefs.inAppViewing.collectAsState(initial = true)

    var storageSize by remember { mutableLongStateOf(0L) }
    var showClearCacheConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        storageSize = storageManager.getMediaStorageSize()
    }

    Scaffold(
        topBar = {
            LaterBoxHeader(
                greeting = "Settings",
                subtitle = "Preferences & Storage",
                showBack = true,
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            item {
                SettingsSection(
                    title = "Appearance",
                    icon = Icons.Default.Palette
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Theme Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extended.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.values().forEach { mode ->
                                val isSelected = themeMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) extended.amberAccentSubtle else MaterialTheme.colorScheme.surface)
                                        .border(
                                            1.dp,
                                            if (isSelected) extended.amberAccent else extended.border,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            scope.launch { prefs.setThemeMode(mode) }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) extended.amberAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Content Saving Preferences
            item {
                SettingsSection(
                    title = "Saving & Organizing",
                    icon = Icons.Default.Tune
                ) {
                    SettingsToggleRow(
                        title = "Auto-fetch Web Metadata",
                        subtitle = "Retrieve page title, OpenGraph preview image, and domain automatically",
                        checked = autoFetchMetadata,
                        onCheckedChange = { checked ->
                            scope.launch { prefs.setAutoFetchMetadata(checked) }
                        }
                    )
                    HorizontalDivider(color = extended.border, modifier = Modifier.padding(vertical = 8.dp))
                    SettingsToggleRow(
                        title = "Direct Save to Inbox",
                        subtitle = "Skip playlist picker on share and save directly to Inbox",
                        checked = directSaveToInbox,
                        onCheckedChange = { checked ->
                            scope.launch { prefs.setDirectSaveToInbox(checked) }
                        }
                    )
                    HorizontalDivider(color = extended.border, modifier = Modifier.padding(vertical = 8.dp))
                    SettingsToggleRow(
                        title = "In-App Content Viewer",
                        subtitle = "Open articles and media inside Later Box rather than external browser",
                        checked = inAppViewing,
                        onCheckedChange = { checked ->
                            scope.launch { prefs.setInAppViewing(checked) }
                        }
                    )
                }
            }

            // Storage & Trash Management
            item {
                SettingsSection(
                    title = "Storage & Cleanup",
                    icon = Icons.Default.Storage
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Media Cache",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${formatBytes(storageSize)} used for offline files",
                                style = MaterialTheme.typography.bodyMedium,
                                color = extended.textSecondary
                            )
                        }

                        OutlinedButton(
                            onClick = { showClearCacheConfirm = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear")
                        }
                    }

                    HorizontalDivider(color = extended.border, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTrash() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Recently Deleted",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "View or permanently purge deleted items",
                                style = MaterialTheme.typography.bodyMedium,
                                color = extended.textSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = extended.warmError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Privacy & Architecture
            item {
                SettingsSection(
                    title = "Privacy & Local Storage",
                    icon = Icons.Default.Security
                ) {
                    Text(
                        text = "100% Local-First Architecture",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Later Box does not require account registration or cloud sign-in. All bookmarks, notes, and downloaded media reside securely on your local device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary
                    )
                }
            }

            // About App
            item {
                SettingsSection(
                    title = "About",
                    icon = Icons.Default.Info
                ) {
                    Text(
                        text = "Later Box",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 1.0.0 (Build 1)",
                        style = MaterialTheme.typography.labelSmall,
                        color = extended.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SAVE NOW. ORGANIZE EASILY. ENJOY LATER.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = extended.amberAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Open Source under the MIT License.\nhttps://github.com/arybishal/Later-Box",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showClearCacheConfirm) {
        ConfirmationDialog(
            title = "Clear Media Cache",
            message = "This will remove downloaded media copies to free up storage space. Your saved bookmark links and titles will remain intact.",
            confirmLabel = "Clear Cache",
            isDestructive = false,
            onConfirm = {
                scope.launch {
                    storageManager.clearMediaCache()
                    storageSize = storageManager.getMediaStorageSize()
                    Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
                }
                showClearCacheConfirm = false
            },
            onDismiss = { showClearCacheConfirm = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, extended.border, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = extended.cardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = extended.amberAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val extended = LaterBoxThemeTokens.extended

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = extended.amberAccent,
                uncheckedBorderColor = extended.border
            )
        )
    }
}
