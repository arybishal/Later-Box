package com.laterbox.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.ui.components.glass.GlassSurface
import com.laterbox.ui.theme.GlassVariant
import com.laterbox.ui.theme.LaterBoxThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentActionsSheet(
    item: SavedItemEntity,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onOpenInApp: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleArchive: () -> Unit,
    onEditNotes: () -> Unit,
    onMoveToTrash: () -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onRestore: (() -> Unit)? = null,
    onPermanentDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val extended = LaterBoxThemeTokens.extended

    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        shape = sheetShape,
        dragHandle = null
    ) {
        GlassSurface(
            variant = GlassVariant.ELEVATED,
            shape = sheetShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Centered glass drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 14.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(extended.secondaryText.copy(alpha = 0.35f))
                )

                // Header: Title & URL
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.url,
                style = MaterialTheme.typography.labelSmall,
                color = extended.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = extended.border)
            Spacer(modifier = Modifier.height(8.dp))

            if (!item.isDeleted) {
                ActionItemRow(
                    icon = Icons.AutoMirrored.Filled.Launch,
                    label = "Open in Later Box",
                    onClick = {
                        onDismiss()
                        onOpenInApp()
                    }
                )

                val appDestination = com.laterbox.util.SocialLinkResolver.getPreferredDestinationName(context, item.url)
                ActionItemRow(
                    icon = Icons.Default.OpenInBrowser,
                    label = if (appDestination != "Browser") "Open in $appDestination" else "Open in Browser",
                    onClick = {
                        onDismiss()
                        openInExternalApp(context, item.url)
                    }
                )

                if (appDestination != "Browser") {
                    ActionItemRow(
                        icon = Icons.Default.OpenInBrowser,
                        label = "Open in Browser",
                        onClick = {
                            onDismiss()
                            openInExternalApp(context, item.url, preferBrowser = true)
                        }
                    )
                }

                ActionItemRow(
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    label = "Add to Playlists…",
                    onClick = {
                        onDismiss()
                        onAddToPlaylist()
                    }
                )

                if (onRemoveFromPlaylist != null) {
                    ActionItemRow(
                        icon = Icons.Default.RemoveCircleOutline,
                        label = "Remove from this Playlist",
                        tint = extended.primaryText,
                        onClick = {
                            onDismiss()
                            onRemoveFromPlaylist()
                        }
                    )
                }

                ActionItemRow(
                    icon = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    label = if (item.isFavorite) "Remove from Favorites" else "Mark as Favorite",
                    tint = if (item.isFavorite) extended.primaryText else MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        onDismiss()
                        onToggleFavorite()
                    }
                )

                ActionItemRow(
                    icon = if (item.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                    label = if (item.isArchived) "Unarchive" else "Archive",
                    onClick = {
                        onDismiss()
                        onToggleArchive()
                    }
                )

                ActionItemRow(
                    icon = Icons.Default.EditNote,
                    label = "Add / Edit Note",
                    onClick = {
                        onDismiss()
                        onEditNotes()
                    }
                )

                ActionItemRow(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy Link",
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("URL", item.url))
                        Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                ActionItemRow(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${item.title}\n${item.url}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                        onDismiss()
                    }
                )

                ActionItemRow(
                    icon = Icons.Default.Delete,
                    label = "Move to Trash",
                    tint = extended.warmError,
                    onClick = {
                        onDismiss()
                        onMoveToTrash()
                    }
                )
            } else {
                // Trashed item actions
                ActionItemRow(
                    icon = Icons.Default.RestoreFromTrash,
                    label = "Restore to Library",
                    onClick = {
                        onDismiss()
                        onRestore?.invoke()
                    }
                )

                ActionItemRow(
                    icon = Icons.Default.DeleteForever,
                    label = "Delete Permanently",
                    tint = extended.warmError,
                    onClick = {
                        onDismiss()
                        onPermanentDelete?.invoke()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}

@Composable
private fun ActionItemRow(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = tint
        )
    }
}

fun openInExternalApp(context: Context, rawUrl: String, preferBrowser: Boolean = false) {
    com.laterbox.util.SocialLinkResolver.openSocialOrWebLink(context, rawUrl, preferBrowser)
}
