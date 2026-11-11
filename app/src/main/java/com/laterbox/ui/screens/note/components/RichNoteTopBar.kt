package com.laterbox.ui.screens.note.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.ui.components.glass.GlassSurface
import com.laterbox.ui.screens.note.SaveStatus
import com.laterbox.ui.theme.GlassVariant
import com.laterbox.ui.theme.LaterBoxThemeTokens

@Composable
fun RichNoteTopBar(
    isReadingMode: Boolean,
    saveStatus: SaveStatus,
    canUndo: Boolean,
    canRedo: Boolean,
    wordCount: Int,
    characterCount: Int,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleReadingMode: () -> Unit,
    onRetrySave: () -> Unit,
    onClearFormatting: () -> Unit,
    onCopyPlainText: () -> Unit,
    onDeleteNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(extended.background)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Back button & Save Status
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = extended.primaryBlack
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Save status pill
            SaveStatusBadge(
                status = saveStatus,
                onRetry = onRetrySave
            )
        }

        // Right Actions: Undo, Redo, Mode Toggle, Overflow Menu
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isReadingMode) {
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) extended.primaryBlack else extended.disabled
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) extended.primaryBlack else extended.disabled
                    )
                }
            }

            // Reading / Editing Mode Toggle
            IconButton(
                onClick = onToggleReadingMode,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(
                    imageVector = if (isReadingMode) Icons.Default.Edit else Icons.Default.Visibility,
                    contentDescription = if (isReadingMode) "Switch to Editing Mode" else "Switch to Reading Mode",
                    tint = extended.primaryBlack
                )
            }

            // More Options Dropdown
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More actions",
                        tint = extended.primaryBlack
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(extended.cardBackground)
                        .border(1.dp, extended.border, RoundedCornerShape(12.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "$wordCount words · $characterCount chars",
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.secondaryText
                            )
                        },
                        onClick = { },
                        enabled = false
                    )

                    DropdownMenuItem(
                        text = { Text("Copy Plain Text", color = extended.primaryBlack) },
                        onClick = {
                            showMenu = false
                            onCopyPlainText()
                        }
                    )

                    if (!isReadingMode) {
                        DropdownMenuItem(
                            text = { Text("Clear Formatting", color = extended.primaryBlack) },
                            onClick = {
                                showMenu = false
                                onClearFormatting()
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Delete Note", color = extended.warmError) },
                        onClick = {
                            showMenu = false
                            onDeleteNote()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveStatusBadge(
    status: SaveStatus,
    onRetry: () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended

    val badgeShape = RoundedCornerShape(8.dp)

    GlassSurface(
        variant = GlassVariant.SUBTLE,
        shape = badgeShape,
        modifier = Modifier
            .clip(badgeShape)
            .clickable(enabled = status == SaveStatus.ERROR) { onRetry() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            when (status) {
                SaveStatus.SAVING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        strokeWidth = 1.5.dp,
                        color = extended.secondaryText
                    )
                    Text(
                        text = "Saving…",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = extended.secondaryText
                    )
                }
                SaveStatus.SAVED -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = extended.secondaryText
                    )
                    Text(
                        text = "Saved",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = extended.secondaryText
                    )
                }
                SaveStatus.LOCAL_SAVE -> {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = extended.secondaryText
                    )
                    Text(
                        text = "Local save",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = extended.secondaryText
                    )
                }
                SaveStatus.ERROR -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = extended.warmError
                    )
                    Text(
                        text = "Save failed (Retry)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = extended.warmError
                    )
                }
            }
        }
    }
}
