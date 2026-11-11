package com.laterbox.ui.screens.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.laterbox.ui.components.LaterBoxSnackbarController
import com.laterbox.ui.components.PermissionNoticeCard
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
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
import com.laterbox.data.update.AppUpdateWorker
import com.laterbox.data.update.UpdateInfo
import com.laterbox.ui.components.AppUpdateSection
import com.laterbox.ui.components.UpdateDetailsDialog
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.LaterBoxApp
import com.laterbox.data.backup.BackupManifest
import com.laterbox.data.backup.BackupSummary
import com.laterbox.data.model.ThemeMode
import com.laterbox.ui.components.BackupInspectionDialog
import com.laterbox.ui.components.BackupProgressDialog
import com.laterbox.ui.components.ConfirmationDialog
import com.laterbox.ui.components.LaterBoxHeader
import com.laterbox.ui.theme.LaterBoxThemeTokens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

@Composable
fun SettingsScreen(
    onBack: (() -> Unit)? = null,
    onNavigateToTrash: () -> Unit,
    onNavigateToGeminiSettings: () -> Unit = {}
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs = LaterBoxApp.instance.preferencesRepository
    val storageManager = LaterBoxApp.instance.mediaStorageManager
    val backupManager = LaterBoxApp.instance.backupManager
    val updateManager = LaterBoxApp.instance.updateManager

    val themeMode by prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val autoFetchMetadata by prefs.autoFetchMetadata.collectAsState(initial = true)
    val directSaveToInbox by prefs.directSaveToInbox.collectAsState(initial = false)
    val inAppViewing by prefs.inAppViewing.collectAsState(initial = true)
    val lastBackupTimestamp by prefs.lastBackupTimestamp.collectAsState(initial = null)
    val autoUpdateCheck by prefs.autoUpdateCheck.collectAsState(initial = true)
    val updateState by updateManager.updateState.collectAsState()

    val aiSettings = LaterBoxApp.instance.aiSettingsRepository
    val isAiEnabled by aiSettings.isAiEnabled.collectAsState(initial = false)
    val autoAnalyze by aiSettings.autoAnalyzeNewItems.collectAsState(initial = true)
    val selectedModelId by aiSettings.selectedModelId.collectAsState(initial = com.laterbox.data.ai.model.GeminiModels.DEFAULT_MODEL_ID)
    val secureStorage = remember { com.laterbox.data.ai.security.SecureApiKeyStorage.getInstance(context) }
    var hasApiKey by remember { mutableStateOf(secureStorage.hasApiKey()) }

    var storageSize by remember { mutableLongStateOf(0L) }
    var backupSummary by remember { mutableStateOf<BackupSummary?>(null) }
    var showClearCacheConfirm by remember { mutableStateOf(false) }
    var activeUpdateForDetails by remember { mutableStateOf<UpdateInfo?>(null) }

    // Backup state
    var exportInProgress by remember { mutableStateOf(false) }
    var importInProgress by remember { mutableStateOf(false) }
    var progressValue by remember { mutableFloatStateOf(0f) }
    var progressStatus by remember { mutableStateOf("") }
    var pendingManifest by remember { mutableStateOf<BackupManifest?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }

    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true
    var dismissNotificationNotice by remember { mutableStateOf(false) }

    val alarmManager = remember(context) { context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager }
    val powerManager = remember(context) { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }

    var canScheduleExactAlarms by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager?.canScheduleExactAlarms() ?: true
            } else true
        )
    }

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
            } else true
        )
    }

    LaunchedEffect(Unit) {
        storageSize = storageManager.getMediaStorageSize()
        backupSummary = backupManager.getBackupSummary()
        hasApiKey = secureStorage.hasApiKey()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            canScheduleExactAlarms = alarmManager?.canScheduleExactAlarms() ?: true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isIgnoringBatteryOptimizations = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        }
    }

    // Storage Access Framework Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                exportInProgress = true
                progressValue = 0f
                progressStatus = "Initializing export…"
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val result = backupManager.exportBackup(outputStream) { p, s ->
                        progressValue = p
                        progressStatus = s
                    }
                    exportInProgress = false
                    result.onSuccess { manifest ->
                        backupSummary = backupManager.getBackupSummary()
                        LaterBoxSnackbarController.showMessage(
                            "Export completed (${manifest.totalItems} items, ${manifest.totalPlaylists} collections)"
                        )
                    }.onFailure { err ->
                        LaterBoxSnackbarController.showMessage("Export failed: ${err.message ?: "Unknown error"}")
                    }
                } else {
                    exportInProgress = false
                    LaterBoxSnackbarController.showMessage("Cannot open selected file for writing")
                }
            }
        }
    }

    // Storage Access Framework Import Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val inspectResult = backupManager.inspectBackup(inputStream)
                    inspectResult.onSuccess { manifest ->
                        pendingManifest = manifest
                        pendingImportUri = uri
                    }.onFailure { err ->
                        LaterBoxSnackbarController.showMessage("Invalid backup archive: ${err.message ?: "Malformed JSON"}")
                    }
                } else {
                    LaterBoxSnackbarController.showMessage("Cannot read selected file")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            LaterBoxHeader(
                greeting = "Settings",
                subtitle = "Preferences, Data & Storage",
                showBack = onBack != null,
                onBackClick = onBack ?: {}
            )
        },
        containerColor = extended.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. AI & Intelligence (High Priority #1 Section)
            item {
                SettingsSection(
                    title = "AI & Intelligence",
                    icon = Icons.Default.AutoAwesome
                ) {
                    val badgeState = AiBadgeState.from(hasApiKey = hasApiKey, isAiEnabled = isAiEnabled)
                    val statusText = when (badgeState) {
                        AiBadgeState.KEY_REQUIRED -> "Setup required"
                        AiBadgeState.PAUSED -> "Gemini AI paused"
                        AiBadgeState.ACTIVE -> if (autoAnalyze) "Connected · Auto-analysis enabled" else "Connected · Auto-analysis paused"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateToGeminiSettings() }
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Google Gemini Intelligence",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = extended.primaryBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AiStatusBadge(state = badgeState)
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = if (badgeState == AiBadgeState.ACTIVE) extended.primaryBlack else extended.secondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (hasApiKey) "Model: $selectedModelId · Automatic summaries, smart titles, topic tags & entity extraction for every saved memory." else "Add your free Google Gemini API key to unlock automatic summaries, smart titles, and instant takeaways for all saved content.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = extended.textSecondary,
                                    lineHeight = 16.sp
                                )
                            }

                            SettingsManageButton(
                                onClick = onNavigateToGeminiSettings,
                                text = if (!hasApiKey) "Setup" else "Configure"
                            )
                        }
                    }
                }
            }

            // 2. Appearance Section
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
                                        .background(if (isSelected) extended.pillActive else extended.surfaceSecondary)
                                        .border(
                                            1.dp,
                                            if (isSelected) extended.border else extended.border,
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
                                        color = if (isSelected) extended.pillActiveText else extended.secondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reminders & Reliability (Alarms & Battery Optimization)
            item {
                SettingsSection(
                    title = "Reminders & Reliability",
                    icon = Icons.Default.Alarm
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Ensure timely alerts for birthdays, anniversaries, and reminders on battery-saving systems.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extended.textSecondary
                        )

                        HorizontalDivider(color = extended.border, modifier = Modifier.padding(vertical = 4.dp))

                        // 1. Notification Permission
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Notifications",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(extended.surfaceSecondary)
                                            .border(1.dp, extended.border, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (hasNotificationPermission) "GRANTED" else "REQUIRED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasNotificationPermission) extended.primaryText else extended.secondaryText
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (hasNotificationPermission) "Alerts for upcoming reminders are enabled" else "Required to show notifications when reminders trigger",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = extended.textSecondary
                                )
                            }

                            if (!hasNotificationPermission) {
                                OutlinedButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            }
                                            context.startActivity(intent)
                                        } else {
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            context.startActivity(intent)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, extended.border)
                                ) {
                                    Text("Grant", color = extended.primaryBlack)
                                }
                            }
                        }

                        HorizontalDivider(color = extended.border, modifier = Modifier.padding(vertical = 4.dp))

                        // 2. Exact Alarms Permission (Android 12+)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Exact Alarms",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(extended.surfaceSecondary)
                                            .border(1.dp, extended.border, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (canScheduleExactAlarms) "ACTIVE" else "RESTRICTED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canScheduleExactAlarms) extended.primaryText else extended.secondaryText
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (canScheduleExactAlarms) "Alarms trigger at the exact scheduled minute" else "Required on Android 12+ for exact alarm precision",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = extended.textSecondary
                                )
                            }

                            if (!canScheduleExactAlarms) {
                                OutlinedButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            context.startActivity(intent)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, extended.border)
                                ) {
                                    Text("Enable", color = extended.primaryBlack)
                                }
                            }
                        }

                        HorizontalDivider(color = extended.border, modifier = Modifier.padding(vertical = 4.dp))

                        // 3. Battery Optimization Exemption
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Battery Optimization",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(extended.surfaceSecondary)
                                            .border(1.dp, extended.border, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isIgnoringBatteryOptimizations) "UNRESTRICTED" else "OPTIMIZED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isIgnoringBatteryOptimizations) extended.primaryText else extended.secondaryText
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isIgnoringBatteryOptimizations) "Background alarms are protected from OS sleep killing" else "Recommended: exclude app from sleep optimization for 100% reliable alarms",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = extended.textSecondary
                                )
                            }

                            if (!isIgnoringBatteryOptimizations) {
                                OutlinedButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            try {
                                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                    data = Uri.parse("package:${context.packageName}")
                                                }
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                                context.startActivity(intent)
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, extended.border)
                                ) {
                                    Text("Fix", color = extended.primaryBlack)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Content Saving Preferences
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
                                text = "Downloaded Media",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Current usage: ${formatBytes(storageSize)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = extended.textSecondary
                            )
                        }

                        OutlinedButton(
                            onClick = { showClearCacheConfirm = true },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, extended.border)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = extended.primaryBlack
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear", color = extended.primaryBlack)
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
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Recently Deleted",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "View or permanently purge deleted items",
                                style = MaterialTheme.typography.bodyMedium,
                                color = extended.textSecondary
                            )
                        }

                        SettingsManageButton(
                            onClick = onNavigateToTrash,
                            text = "Manage"
                        )
                    }
                }
            }

            // 6. Data & Backup
            item {
                SettingsSection(
                    title = "Data & Backup",
                    icon = Icons.Default.Backup
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Summary info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Last Backup",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = extended.textSecondary
                                )
                                Text(
                                    text = lastBackupTimestamp?.let { dateFormat.format(Date(it)) } ?: "Never backed up",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            backupSummary?.let { sum ->
                                Text(
                                    text = "${sum.totalItems} items · ${sum.totalPlaylists} playlists",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = extended.textSecondary
                                )
                            }
                        }

                        HorizontalDivider(color = extended.border, modifier = Modifier.padding(vertical = 4.dp))

                        // Export Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Export My Data",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Create a portable backup of your saved content, playlists, settings and downloaded media.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = extended.textSecondary
                                )
                            }

                            Button(
                                onClick = { exportLauncher.launch("LaterBox_Backup.zip") },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = extended.buttonPrimary,
                                    contentColor = extended.buttonPrimaryText
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = extended.buttonPrimaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export", fontWeight = FontWeight.Bold, color = extended.buttonPrimaryText)
                            }
                        }

                        HorizontalDivider(color = extended.border, modifier = Modifier.padding(vertical = 4.dp))

                        // Import Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Import Backup",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Restore your Later Box library and preferences from a previous backup.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = extended.textSecondary
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    importLauncher.launch(
                                        arrayOf("application/zip", "application/octet-stream", "*/*")
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, extended.border),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = extended.primaryBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import", fontWeight = FontWeight.Bold, color = extended.primaryBlack)
                            }
                        }
                    }
                }
            }

            // Privacy & Local Architecture
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

            // About App & App Updates
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
                        text = "Version ${updateManager.currentVersionName} (Build ${updateManager.currentVersionCode})",
                        style = MaterialTheme.typography.labelSmall,
                        color = extended.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SAVE NOW. ORGANIZE EASILY. ENJOY LATER.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = extended.primaryBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Open Source under the MIT License.\nhttps://github.com/arybishal/Later-Box",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dedicated In-App Update System Section
                    AppUpdateSection(
                        updateState = updateState,
                        updateManager = updateManager,
                        autoCheckEnabled = autoUpdateCheck,
                        onToggleAutoCheck = { enabled ->
                            scope.launch {
                                prefs.setAutoUpdateCheck(enabled)
                                if (enabled) {
                                    AppUpdateWorker.schedulePeriodicCheck(context)
                                } else {
                                    AppUpdateWorker.cancelPeriodicCheck(context)
                                }
                            }
                        },
                        onShowDetails = { info ->
                            activeUpdateForDetails = info
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Backup Inspection Dialog
    pendingManifest?.let { manifest ->
        BackupInspectionDialog(
            manifest = manifest,
            onConfirm = { mode ->
                val uri = pendingImportUri
                pendingManifest = null
                pendingImportUri = null
                if (uri != null) {
                    scope.launch {
                        importInProgress = true
                        progressValue = 0f
                        progressStatus = "Initializing restore…"
                        val inputStream = context.contentResolver.openInputStream(uri)
                        if (inputStream != null) {
                            val result = backupManager.importBackup(inputStream, mode) { p, s ->
                                progressValue = p
                                progressStatus = s
                            }
                            importInProgress = false
                            result.onSuccess { res ->
                                storageSize = storageManager.getMediaStorageSize()
                                backupSummary = backupManager.getBackupSummary()
                                LaterBoxSnackbarController.showMessage(
                                    "Restored: ${res.itemsRestored} items, ${res.playlistsRestored} collections (${res.mode.name})"
                                )
                            }.onFailure { err ->
                                LaterBoxSnackbarController.showMessage("Restore failed: ${err.message ?: "Unknown error"}")
                            }
                        } else {
                            importInProgress = false
                            LaterBoxSnackbarController.showMessage("Cannot read selected file")
                        }
                    }
                }
            },
            onDismiss = {
                pendingManifest = null
                pendingImportUri = null
            }
        )
    }

    // Backup Progress Dialog
    if (exportInProgress || importInProgress) {
        BackupProgressDialog(
            title = if (exportInProgress) "Exporting Backup…" else "Restoring Backup…",
            progress = progressValue,
            statusText = progressStatus
        )
    }

    // Clear Media Cache Confirmation Dialog
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

    // Update Details Modal Dialog
    activeUpdateForDetails?.let { updateInfo ->
        UpdateDetailsDialog(
            updateInfo = updateInfo,
            currentVersionName = updateManager.currentVersionName,
            currentVersionCode = updateManager.currentVersionCode,
            onDismiss = {
                scope.launch {
                    prefs.setLastDismissedUpdateTag(updateInfo.tag)
                }
                activeUpdateForDetails = null
            },
            onStartDownload = {
                updateManager.startDownload(updateInfo)
            }
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
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, extended.border, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = extended.primaryBlack,
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
                checkedThumbColor = Color.White,
                checkedTrackColor = extended.primaryBlack,
                uncheckedThumbColor = extended.textSecondary,
                uncheckedTrackColor = extended.pillInactive,
                uncheckedBorderColor = extended.border
            )
        )
    }
}

/**
 * Settings Manage Button
 * Minimal Monochrome Design System:
 * Light theme: Background #111111, Text #FFFFFF, Icon #FFFFFF
 * Dark theme: Background #FFFFFF, Text #111111, Icon #111111
 * Includes 10dp rounded corners, minimum 48dp touch target, and visible pressed/disabled states.
 */
@Composable
fun SettingsManageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = "Manage"
) {
    val extended = LaterBoxThemeTokens.extended

    val containerColor = if (enabled) extended.buttonPrimary else extended.buttonPrimary.copy(alpha = 0.4f)
    val contentColor = if (enabled) extended.buttonPrimaryText else extended.buttonPrimaryText.copy(alpha = 0.4f)

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

/**
 * AI Badge State representing the configuration and operational status of Gemini AI.
 */
enum class AiBadgeState(val label: String) {
    KEY_REQUIRED("KEY REQUIRED"),
    ACTIVE("ACTIVE"),
    PAUSED("PAUSED");

    companion object {
        fun from(hasApiKey: Boolean, isAiEnabled: Boolean): AiBadgeState = when {
            !hasApiKey -> KEY_REQUIRED
            !isAiEnabled -> PAUSED
            else -> ACTIVE
        }
    }
}

/**
 * AI Status Badge
 * Displays the current status of Google Gemini AI in Settings.
 * Minimalist design system token compliance:
 * - KEY REQUIRED: Surface secondary background, border, high-contrast primary text.
 * - ACTIVE: Subtle interactive blue accent background and border, interactive blue text.
 * - PAUSED: Surface secondary background, border, muted secondary text.
 */
@Composable
fun AiStatusBadge(
    state: AiBadgeState,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    val (backgroundColor, borderColor, textColor) = when (state) {
        AiBadgeState.ACTIVE -> Triple(
            extended.interactiveAccent.copy(alpha = 0.12f),
            extended.interactiveAccent.copy(alpha = 0.35f),
            extended.interactiveAccent
        )
        AiBadgeState.KEY_REQUIRED -> Triple(
            extended.surfaceSecondary,
            extended.border,
            extended.primaryBlack
        )
        AiBadgeState.PAUSED -> Triple(
            extended.surfaceSecondary,
            extended.border,
            extended.secondaryText
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = state.label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 13.sp,
            letterSpacing = 0.5.sp,
            color = textColor,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )
    }
}
