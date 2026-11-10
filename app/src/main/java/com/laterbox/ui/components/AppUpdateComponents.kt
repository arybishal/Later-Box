package com.laterbox.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.data.update.UpdateInfo
import com.laterbox.data.update.UpdateManager
import com.laterbox.data.update.UpdateState
import com.laterbox.ui.theme.LaterBoxThemeTokens
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppUpdateSection(
    updateState: UpdateState,
    updateManager: UpdateManager,
    autoCheckEnabled: Boolean,
    onToggleAutoCheck: (Boolean) -> Unit,
    onShowDetails: (UpdateInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, extended.border, RoundedCornerShape(14.dp))
            .background(extended.cardBackground)
            .padding(16.dp)
    ) {
        // Section Header: App Updates
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = null,
                tint = extended.amberAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "App Updates",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Version & Build Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Installed Version",
                    fontSize = 12.sp,
                    color = extended.textSecondary
                )
                Text(
                    text = "v${updateManager.currentVersionName} (Build ${updateManager.currentVersionCode})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Status Badge
            UpdateStatusBadge(updateState = updateState)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic State Content
        when (updateState) {
            is UpdateState.Checking -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = extended.amberAccent
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Checking GitHub for updates…",
                        fontSize = 13.sp,
                        color = extended.textSecondary
                    )
                }
            }

            is UpdateState.Downloading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Downloading v${updateState.updateInfo.versionName}…",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${(updateState.progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.amberAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { updateState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = extended.amberAccent,
                        trackColor = extended.border
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val downloadedMb = updateState.bytesDownloaded.toDouble() / (1024 * 1024)
                        val totalMb = updateState.totalBytes.toDouble() / (1024 * 1024)
                        Text(
                            text = String.format(Locale.US, "%.1f MB / %.1f MB", downloadedMb, totalMb),
                            fontSize = 11.sp,
                            color = extended.textSecondary
                        )

                        TextButton(
                            onClick = { updateManager.cancelDownload() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp, color = extended.warmError)
                        }
                    }
                }
            }

            is UpdateState.Downloaded -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "Update v${updateState.updateInfo.versionName} is downloaded and verified.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            updateManager.installUpdate(context, updateState.apkFile)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extended.amberAccent,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Install Update Now", fontWeight = FontWeight.Bold)
                    }
                }
            }

            is UpdateState.Available -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Version ${updateState.updateInfo.versionName} is available (${updateState.updateInfo.formattedSize}).",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onShowDetails(updateState.updateInfo) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = extended.amberAccent,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("View & Update", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(updateState.updateInfo.releaseUrl))
                                context.startActivity(browserIntent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GitHub", fontSize = 12.sp)
                        }
                    }
                }
            }

            is UpdateState.Failed -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = updateState.message,
                        fontSize = 12.sp,
                        color = extended.warmError
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { scope.launch { updateManager.checkForUpdates() } },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extended.cardBackground,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.border(1.dp, extended.border, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry Check", fontSize = 12.sp)
                    }
                }
            }

            is UpdateState.UpToDate, is UpdateState.Idle -> {
                val lastChecked = when (updateState) {
                    is UpdateState.UpToDate -> updateState.lastChecked
                    is UpdateState.Idle -> updateState.lastChecked
                    else -> 0L
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Last checked",
                            fontSize = 11.sp,
                            color = extended.textSecondary
                        )
                        Text(
                            text = if (lastChecked > 0) formatRelativeTime(lastChecked) else "Never",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch { updateManager.checkForUpdates() }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extended.amberAccent,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            is UpdateState.Installing -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = extended.amberAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Launching package installer…", fontSize = 13.sp, color = extended.textSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = extended.border.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(10.dp))

        // Automatic Check Preference Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Automatic update checks",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Check daily in background for new releases",
                    fontSize = 12.sp,
                    color = extended.textSecondary
                )
            }

            Switch(
                checked = autoCheckEnabled,
                onCheckedChange = onToggleAutoCheck,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = extended.amberAccent,
                    uncheckedBorderColor = extended.border
                )
            )
        }
    }
}

@Composable
fun UpdateStatusBadge(
    updateState: UpdateState,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    val (badgeText, badgeBg, badgeTextColor) = when (updateState) {
        is UpdateState.Checking -> Triple("Checking…", extended.border.copy(alpha = 0.4f), extended.textSecondary)
        is UpdateState.UpToDate -> Triple("Up to date", extended.amberAccentSubtle, extended.amberAccent)
        is UpdateState.Available -> Triple("Update available", extended.amberAccent, MaterialTheme.colorScheme.onPrimary)
        is UpdateState.Downloading -> Triple("Downloading…", extended.border, MaterialTheme.colorScheme.onBackground)
        is UpdateState.Downloaded -> Triple("Ready to install", extended.amberAccent, MaterialTheme.colorScheme.onPrimary)
        is UpdateState.Installing -> Triple("Installing…", extended.border, MaterialTheme.colorScheme.onBackground)
        is UpdateState.Failed -> Triple("Check failed", extended.border, extended.warmError)
        is UpdateState.Idle -> Triple("Up to date", extended.amberAccentSubtle, extended.amberAccent)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badgeText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = badgeTextColor
        )
    }
}

@Composable
fun UpdateDetailsDialog(
    updateInfo: UpdateInfo,
    currentVersionName: String,
    currentVersionCode: Long,
    onDismiss: () -> Unit,
    onStartDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Later Box Update",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "v${updateInfo.versionName} is ready",
                        fontSize = 13.sp,
                        color = extended.amberAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = extended.textSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Info badges row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(extended.cardBackground)
                        .border(1.dp, extended.border, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Current", fontSize = 11.sp, color = extended.textSecondary)
                        Text("v$currentVersionName", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("New Version", fontSize = 11.sp, color = extended.textSecondary)
                        Text("v${updateInfo.versionName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = extended.amberAccent)
                    }
                    Column {
                        Text("Size", fontSize = 11.sp, color = extended.textSecondary)
                        Text(updateInfo.formattedSize, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Release Notes",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = updateInfo.notes.ifBlank { "General quality improvements, bug fixes, and performance updates." },
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = extended.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // View on GitHub link
                Row(
                    modifier = Modifier
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.releaseUrl))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        tint = extended.amberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "View Release on GitHub",
                        fontSize = 12.sp,
                        color = extended.amberAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onStartDownload()
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extended.amberAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Update Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", color = extended.textSecondary)
            }
        }
    )
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}
