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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.laterbox.data.backup.BackupManifest
import com.laterbox.data.backup.ImportMode
import com.laterbox.ui.theme.LaterBoxThemeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupInspectionDialog(
    manifest: BackupManifest,
    onConfirm: (ImportMode) -> Unit,
    onDismiss: () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    var selectedMode by remember { mutableStateOf(ImportMode.MERGE) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }
    val formattedDate = remember(manifest.createdAt) { dateFormat.format(Date(manifest.createdAt)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = extended.amberAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Restore Later Box Backup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Manifest summary card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(extended.cardBackground)
                        .border(1.dp, extended.border, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Backup Information",
                        style = MaterialTheme.typography.labelLarge,
                        color = extended.amberAccent
                    )
                    Text(
                        text = "Created: $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary
                    )
                    Text(
                        text = "App Version: ${manifest.appVersion} (code ${manifest.appVersionCode})",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Saved Items: ${manifest.totalItems}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Playlists: ${manifest.totalPlaylists}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Media: ${manifest.totalMediaFiles}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "Choose Restore Mode",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                // Option 1: MERGE (Safe default)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedMode == ImportMode.MERGE) extended.amberAccentSubtle else Color.Transparent)
                        .border(
                            1.dp,
                            if (selectedMode == ImportMode.MERGE) extended.amberAccent else extended.border,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedMode = ImportMode.MERGE }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMode == ImportMode.MERGE,
                        onClick = { selectedMode = ImportMode.MERGE },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = extended.amberAccent,
                            unselectedColor = extended.textSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Merge with Library (Recommended)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Adds new items and playlists without deleting your current data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary
                        )
                    }
                }

                // Option 2: REPLACE (Full overwrite)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedMode == ImportMode.REPLACE) extended.amberAccentSubtle else Color.Transparent)
                        .border(
                            1.dp,
                            if (selectedMode == ImportMode.REPLACE) extended.amberAccent else extended.border,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedMode = ImportMode.REPLACE }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMode == ImportMode.REPLACE,
                        onClick = { selectedMode = ImportMode.REPLACE },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = extended.amberAccent,
                            unselectedColor = extended.textSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Replace Existing Library",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Wipes your current library and restores the backup completely.",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedMode) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extended.amberAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (selectedMode == ImportMode.REPLACE) "Replace & Restore" else "Merge & Restore",
                    fontWeight = FontWeight.Bold
                )
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
fun BackupProgressDialog(
    title: String,
    progress: Float,
    statusText: String
) {
    val extended = LaterBoxThemeTokens.extended

    AlertDialog(
        onDismissRequest = {}, // Non-cancelable during critical I/O
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = extended.amberAccent,
                    trackColor = extended.border
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary
                )
            }
        },
        confirmButton = {}
    )
}
