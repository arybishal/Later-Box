package com.laterbox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.ui.theme.LaterBoxThemeTokens

val PLAYLIST_ICONS = listOf(
    Pair("bookmark", Icons.Default.Bookmark),
    Pair("folder", Icons.Default.Folder),
    Pair("video", Icons.Default.Videocam),
    Pair("article", Icons.Default.MenuBook),
    Pair("music", Icons.Default.MusicNote),
    Pair("star", Icons.Default.Star),
    Pair("idea", Icons.Default.Lightbulb),
    Pair("code", Icons.Default.Code),
    Pair("document", Icons.Default.Description)
)

fun getIconForName(name: String?): ImageVector {
    return PLAYLIST_ICONS.firstOrNull { it.first == name }?.second ?: Icons.Default.Bookmark
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("bookmark") }
    val extended = LaterBoxThemeTokens.extended

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Playlist",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = extended.amberAccent,
                        cursorColor = extended.amberAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = extended.amberAccent,
                        cursorColor = extended.amberAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select Icon",
                    style = MaterialTheme.typography.labelLarge,
                    color = extended.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PLAYLIST_ICONS) { (iconKey, vector) ->
                        val isSelected = iconKey == selectedIcon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) extended.amberAccent else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = iconKey,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else extended.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), description.trim().ifEmpty { null }, selectedIcon)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extended.amberAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = extended.textSecondary)
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    allPlaylists: List<PlaylistEntity>,
    initiallySelectedIds: List<Long>,
    onDismiss: () -> Unit,
    onSavePlaylists: (List<Long>) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    val selectedIds = remember { mutableStateListOf<Long>().apply { addAll(initiallySelectedIds) } }
    val extended = LaterBoxThemeTokens.extended

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add to Playlists",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select one or more playlists for this content.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    items(allPlaylists) { playlist ->
                        val isChecked = selectedIds.contains(playlist.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isChecked) selectedIds.remove(playlist.id)
                                    else selectedIds.add(playlist.id)
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedIds.add(playlist.id)
                                    else selectedIds.remove(playlist.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = extended.amberAccent,
                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            Icon(
                                imageVector = getIconForName(playlist.iconName),
                                contentDescription = null,
                                tint = extended.amberAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreateNewPlaylist() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = extended.amberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create new playlist",
                        style = MaterialTheme.typography.labelLarge,
                        color = extended.amberAccent
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSavePlaylists(selectedIds.toList()) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = extended.amberAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = extended.textSecondary)
            }
        }
    )
}

@Composable
fun AddManualItemDialog(
    onDismiss: () -> Unit,
    onSave: (url: String, title: String, notes: String?) -> Unit
) {
    var urlOrText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val extended = LaterBoxThemeTokens.extended

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Save Content",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = urlOrText,
                    onValueChange = {
                        urlOrText = it
                        if (title.isBlank() && (it.startsWith("http://") || it.startsWith("https://"))) {
                            try {
                                val domain = java.net.URI(it).host ?: ""
                                title = domain.removePrefix("www.").replaceFirstChar { c -> c.uppercase() }
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text("URL or Note") },
                    placeholder = { Text("https://example.com/article") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = extended.amberAccent,
                        cursorColor = extended.amberAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = extended.amberAccent,
                        cursorColor = extended.amberAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = extended.amberAccent,
                        cursorColor = extended.amberAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (urlOrText.isNotBlank()) {
                        val finalTitle = title.trim().ifEmpty { urlOrText.trim() }
                        onSave(urlOrText.trim(), finalTitle, notes.trim().ifEmpty { null })
                    }
                },
                enabled = urlOrText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extended.amberAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = extended.textSecondary)
            }
        }
    )
}

@Composable
fun EditNotesDialog(
    initialNotes: String?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    var notes by remember { mutableStateOf(initialNotes ?: "") }
    val extended = LaterBoxThemeTokens.extended

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Notes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Your Notes") },
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = extended.amberAccent,
                    cursorColor = extended.amberAccent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(notes.trim().ifEmpty { null }) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = extended.amberAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = extended.textSecondary)
            }
        }
    )
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String = "Delete",
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) extended.warmError else extended.amberAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = extended.textSecondary)
            }
        }
    )
}
