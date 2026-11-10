package com.laterbox.ui.screens.snap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import com.laterbox.ui.components.LaterBoxSnackbarController
import com.laterbox.ui.theme.LaterBoxThemeTokens
import com.laterbox.util.audio.AudioRecorderManager
import com.laterbox.util.audio.RecordingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private enum class SnapMode {
    MENU,
    VOICE_RECORD,
    QUICK_NOTE,
    SAVE_LINK
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapQuickCaptureSheet(
    onDismiss: () -> Unit,
    onItemCaptured: (SavedItemEntity) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var currentMode by remember { mutableStateOf(SnapMode.MENU) }

    // Audio recorder manager
    val audioRecorder = remember { AudioRecorderManager(context) }
    val recordingState by audioRecorder.recordingState.collectAsState()
    val elapsedSeconds by audioRecorder.elapsedSeconds.collectAsState()
    val amplitude by audioRecorder.amplitude.collectAsState()

    // Temporary photo/video capture targets
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    var pendingVideoFile by remember { mutableStateOf<File?>(null) }

    // Quick note state
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    // Quick link state
    var linkUrl by remember { mutableStateOf("") }
    var isSavingLink by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (audioRecorder.recordingState.value != RecordingState.IDLE) {
                audioRecorder.cancelRecording()
            }
        }
    }

    // Camera Photo Launcher
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingPhotoFile
        if (success && file != null && file.exists() && file.length() > 0) {
            scope.launch {
                val item = SavedItemEntity(
                    url = "file://${file.absolutePath}",
                    title = "Photo Capture",
                    localMediaPath = file.absolutePath,
                    contentType = ContentType.IMAGE,
                    mimeType = "image/jpeg",
                    isSnap = true,
                    captureMethod = "SNAP_PHOTO"
                )
                val id = LaterBoxApp.instance.contentRepository.saveItem(item)
                val saved = LaterBoxApp.instance.contentRepository.getItemById(id)
                saved?.let { onItemCaptured(it) }
                LaterBoxSnackbarController.showMessage("Photo captured to Snap")
                onDismiss()
            }
        }
        pendingPhotoFile = null
    }

    // Camera Video Launcher
    val recordVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        val file = pendingVideoFile
        if (success && file != null && file.exists() && file.length() > 0) {
            scope.launch {
                val thumbPath = LaterBoxApp.instance.mediaStorageManager.generateVideoThumbnail(file.absolutePath)
                val item = SavedItemEntity(
                    url = "file://${file.absolutePath}",
                    title = "Video Capture",
                    localMediaPath = file.absolutePath,
                    thumbnailUrl = thumbPath,
                    contentType = ContentType.VIDEO,
                    mimeType = "video/mp4",
                    isSnap = true,
                    captureMethod = "SNAP_VIDEO"
                )
                val id = LaterBoxApp.instance.contentRepository.saveItem(item)
                val saved = LaterBoxApp.instance.contentRepository.getItemById(id)
                saved?.let { onItemCaptured(it) }
                LaterBoxSnackbarController.showMessage("Video captured to Snap")
                onDismiss()
            }
        }
        pendingVideoFile = null
    }

    // Visual Media Picker (Photos & Videos from gallery)
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val isVideo = mimeType.startsWith("video/")
                    val (savedPath, size) = LaterBoxApp.instance.mediaStorageManager.saveSharedUriToInternalStorage(uri, mimeType)
                    val thumbPath = if (isVideo) {
                        LaterBoxApp.instance.mediaStorageManager.generateVideoThumbnail(savedPath)
                    } else null

                    val item = SavedItemEntity(
                        url = "file://$savedPath",
                        title = if (isVideo) "Imported Video" else "Imported Photo",
                        localMediaPath = savedPath,
                        thumbnailUrl = thumbPath,
                        contentType = if (isVideo) ContentType.VIDEO else ContentType.IMAGE,
                        mimeType = mimeType,
                        mediaSize = size,
                        isSnap = true,
                        captureMethod = "SNAP_IMPORT"
                    )
                    val id = LaterBoxApp.instance.contentRepository.saveItem(item)
                    val saved = LaterBoxApp.instance.contentRepository.getItemById(id)
                    saved?.let { onItemCaptured(it) }
                    LaterBoxSnackbarController.showMessage("Media imported to Snap")
                    onDismiss()
                } catch (e: Exception) {
                    LaterBoxSnackbarController.showMessage("Failed to import media: ${e.localizedMessage}")
                }
            }
        }
    }

    // Camera Permission Launcher
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = LaterBoxApp.instance.mediaStorageManager.createCameraImageFile()
            pendingPhotoFile = file
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            takePhotoLauncher.launch(uri)
        } else {
            LaterBoxSnackbarController.showMessage("Camera permission required to capture photos")
        }
    }

    // Video Camera Permission Launcher
    val requestVideoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (cameraGranted && audioGranted) {
            val file = LaterBoxApp.instance.mediaStorageManager.createCameraVideoFile()
            pendingVideoFile = file
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            recordVideoLauncher.launch(uri)
        } else {
            LaterBoxSnackbarController.showMessage("Camera and Microphone permissions required for video recording")
        }
    }

    // Audio Permission Launcher
    val requestAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = LaterBoxApp.instance.mediaStorageManager.createAudioRecordingFile()
            if (audioRecorder.startRecording(file)) {
                currentMode = SnapMode.VOICE_RECORD
            } else {
                LaterBoxSnackbarController.showMessage("Failed to initialize audio recorder")
            }
        } else {
            LaterBoxSnackbarController.showMessage("Microphone permission required to record voice notes")
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (recordingState != RecordingState.IDLE) {
                audioRecorder.cancelRecording()
            }
            onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = extended.cardBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(extended.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentMode != SnapMode.MENU) {
                        IconButton(
                            onClick = {
                                if (recordingState != RecordingState.IDLE) {
                                    audioRecorder.cancelRecording()
                                }
                                currentMode = SnapMode.MENU
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = extended.primaryText
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column {
                        Text(
                            text = when (currentMode) {
                                SnapMode.MENU -> "Snap Memory"
                                SnapMode.VOICE_RECORD -> "Record Voice Note"
                                SnapMode.QUICK_NOTE -> "Quick Note"
                                SnapMode.SAVE_LINK -> "Save Web Link"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.primaryText
                        )
                        Text(
                            text = when (currentMode) {
                                SnapMode.MENU -> "Instant multi-modal memory capture"
                                SnapMode.VOICE_RECORD -> "Speak clearly into your microphone"
                                SnapMode.QUICK_NOTE -> "Capture thoughts, ideas, or reminders"
                                SnapMode.SAVE_LINK -> "Paste URL from browser or social apps"
                            },
                            fontSize = 12.sp,
                            color = extended.secondaryText
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (recordingState != RecordingState.IDLE) {
                            audioRecorder.cancelRecording()
                        }
                        onDismiss()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = extended.secondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Body depending on current mode
            when (currentMode) {
                SnapMode.MENU -> {
                    // 6-Tile Capture Grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SnapActionTile(
                                icon = Icons.Default.CameraAlt,
                                label = "Take Photo",
                                description = "Camera snapshot",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        val file = LaterBoxApp.instance.mediaStorageManager.createCameraImageFile()
                                        pendingPhotoFile = file
                                        val authority = "${context.packageName}.fileprovider"
                                        val uri = FileProvider.getUriForFile(context, authority, file)
                                        takePhotoLauncher.launch(uri)
                                    } else {
                                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            )

                            SnapActionTile(
                                icon = Icons.Default.Mic,
                                label = "Record Voice",
                                description = "Audio thoughts",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        val file = LaterBoxApp.instance.mediaStorageManager.createAudioRecordingFile()
                                        if (audioRecorder.startRecording(file)) {
                                            currentMode = SnapMode.VOICE_RECORD
                                        } else {
                                            LaterBoxSnackbarController.showMessage("Failed to start recording")
                                        }
                                    } else {
                                        requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SnapActionTile(
                                icon = Icons.Default.Videocam,
                                label = "Record Video",
                                description = "Live video note",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val camGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                    val micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                    if (camGranted && micGranted) {
                                        val file = LaterBoxApp.instance.mediaStorageManager.createCameraVideoFile()
                                        pendingVideoFile = file
                                        val authority = "${context.packageName}.fileprovider"
                                        val uri = FileProvider.getUriForFile(context, authority, file)
                                        recordVideoLauncher.launch(uri)
                                    } else {
                                        requestVideoPermissionLauncher.launch(
                                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                        )
                                    }
                                }
                            )

                            SnapActionTile(
                                icon = Icons.AutoMirrored.Filled.Notes,
                                label = "Quick Note",
                                description = "Ideas & notes",
                                modifier = Modifier.weight(1f),
                                onClick = { currentMode = SnapMode.QUICK_NOTE }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SnapActionTile(
                                icon = Icons.Default.Link,
                                label = "Save Link",
                                description = "Paste web URL",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val clip = clipboardManager.getText()?.text?.trim()
                                    if (!clip.isNullOrBlank() && (clip.startsWith("http://") || clip.startsWith("https://"))) {
                                        linkUrl = clip
                                    }
                                    currentMode = SnapMode.SAVE_LINK
                                }
                            )

                            SnapActionTile(
                                icon = Icons.Default.PhotoLibrary,
                                label = "Import Media",
                                description = "From gallery",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    pickMediaLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                }
                            )
                        }
                    }
                }

                SnapMode.VOICE_RECORD -> {
                    // Live Voice Recording View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(extended.surfaceSecondary)
                            .border(1.dp, extended.border, RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pulse Indicator and Timer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = if (recordingState == RecordingState.RECORDING) Color(0xFFE53935) else extended.tertiaryText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val minutes = elapsedSeconds / 60
                            val seconds = elapsedSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = extended.primaryText,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Waveform Visualizer Simulation based on real amplitude
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val normAmp = (amplitude.toFloat() / 32767f).coerceIn(0.1f, 1.0f)
                            for (i in 0..15) {
                                val heightFactor = if (recordingState == RecordingState.RECORDING) {
                                    ((Math.sin(i * 0.4 + elapsedSeconds * 2.0).toFloat().coerceAtLeast(0.15f) * normAmp)).coerceIn(0.15f, 1.0f)
                                } else {
                                    0.15f
                                }
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height((48 * heightFactor).dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (recordingState == RecordingState.RECORDING) extended.interactiveAccent else extended.border
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Control Buttons: Pause/Resume, Cancel, Finish
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cancel
                            OutlinedButton(
                                onClick = {
                                    audioRecorder.cancelRecording()
                                    currentMode = SnapMode.MENU
                                },
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, extended.border)
                            ) {
                                Text("Cancel", color = extended.secondaryText)
                            }

                            // Pause / Resume
                            IconButton(
                                onClick = {
                                    if (recordingState == RecordingState.RECORDING) {
                                        audioRecorder.pauseRecording()
                                    } else if (recordingState == RecordingState.PAUSED) {
                                        audioRecorder.resumeRecording()
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(extended.cardBackground)
                                    .border(1.dp, extended.border, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (recordingState == RecordingState.RECORDING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (recordingState == RecordingState.RECORDING) "Pause" else "Resume",
                                    tint = extended.primaryText
                                )
                            }

                            // Finish & Save
                            Button(
                                onClick = {
                                    val file = audioRecorder.stopRecording()
                                    if (file != null && file.exists() && file.length() > 0) {
                                        scope.launch {
                                            val item = SavedItemEntity(
                                                url = "file://${file.absolutePath}",
                                                title = "Voice Note (${String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)})",
                                                localMediaPath = file.absolutePath,
                                                contentType = ContentType.AUDIO,
                                                mimeType = "audio/mp4",
                                                mediaSize = file.length(),
                                                isSnap = true,
                                                captureMethod = "SNAP_VOICE"
                                            )
                                            val id = LaterBoxApp.instance.contentRepository.saveItem(item)
                                            val saved = LaterBoxApp.instance.contentRepository.getItemById(id)
                                            saved?.let { onItemCaptured(it) }
                                            LaterBoxSnackbarController.showMessage("Voice note captured to Snap")
                                            onDismiss()
                                        }
                                    } else {
                                        LaterBoxSnackbarController.showMessage("No audio recorded")
                                        currentMode = SnapMode.MENU
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = extended.buttonPrimary,
                                    contentColor = extended.buttonPrimaryText
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                SnapMode.QUICK_NOTE -> {
                    // Quick Note Form
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            placeholder = { Text("Note Title (optional)", color = extended.tertiaryText) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = extended.surfaceSecondary,
                                unfocusedContainerColor = extended.surfaceSecondary,
                                focusedBorderColor = extended.interactiveAccent,
                                unfocusedBorderColor = extended.border
                            )
                        )

                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            placeholder = { Text("Write your thoughts, memory, or draft here...", color = extended.tertiaryText) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = extended.surfaceSecondary,
                                unfocusedContainerColor = extended.surfaceSecondary,
                                focusedBorderColor = extended.interactiveAccent,
                                unfocusedBorderColor = extended.border
                            )
                        )

                        Button(
                            onClick = {
                                if (noteContent.isNotBlank() || noteTitle.isNotBlank()) {
                                    scope.launch {
                                        val title = noteTitle.ifBlank {
                                            noteContent.lines().firstOrNull()?.take(40) ?: "Quick Note"
                                        }
                                        val item = SavedItemEntity(
                                            url = "local://note/${System.currentTimeMillis()}",
                                            title = title,
                                            description = noteContent.take(160),
                                            notes = noteContent,
                                            contentType = ContentType.NOTE,
                                            isSnap = true,
                                            captureMethod = "SNAP_NOTE"
                                        )
                                        val id = LaterBoxApp.instance.contentRepository.saveItem(item)
                                        val saved = LaterBoxApp.instance.contentRepository.getItemById(id)
                                        saved?.let { onItemCaptured(it) }
                                        LaterBoxSnackbarController.showMessage("Note saved to Snap")
                                        onDismiss()
                                    }
                                }
                            },
                            enabled = noteContent.isNotBlank() || noteTitle.isNotBlank(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = extended.buttonPrimary,
                                contentColor = extended.buttonPrimaryText
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Quick Note", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                SnapMode.SAVE_LINK -> {
                    // Quick Link Form
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = linkUrl,
                            onValueChange = { linkUrl = it },
                            placeholder = { Text("https://example.com/article", color = extended.tertiaryText) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    val clip = clipboardManager.getText()?.text?.trim()
                                    if (!clip.isNullOrBlank()) linkUrl = clip
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste",
                                        tint = extended.secondaryText
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = extended.surfaceSecondary,
                                unfocusedContainerColor = extended.surfaceSecondary,
                                focusedBorderColor = extended.interactiveAccent,
                                unfocusedBorderColor = extended.border
                            )
                        )

                        Button(
                            onClick = {
                                val clean = linkUrl.trim()
                                if (clean.isNotBlank()) {
                                    isSavingLink = true
                                    scope.launch {
                                        try {
                                            val normalized = if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
                                                "https://$clean"
                                            } else clean

                                            val metadata = withContext(Dispatchers.IO) {
                                                com.laterbox.data.network.MetadataExtractor.extractMetadata(normalized)
                                            }

                                            val item = SavedItemEntity(
                                                url = normalized,
                                                title = metadata.title.ifBlank { normalized },
                                                description = metadata.description,
                                                sourceDomain = metadata.sourceDomain,
                                                thumbnailUrl = metadata.thumbnailUrl,
                                                contentType = metadata.contentType,
                                                isSnap = true,
                                                captureMethod = "SNAP_LINK"
                                            )
                                            val id = LaterBoxApp.instance.contentRepository.saveItem(item)
                                            val saved = LaterBoxApp.instance.contentRepository.getItemById(id)
                                            saved?.let { onItemCaptured(it) }
                                            LaterBoxSnackbarController.showMessage("Link saved to Snap")
                                            onDismiss()
                                        } catch (e: Exception) {
                                            LaterBoxSnackbarController.showMessage("Failed to save link: ${e.localizedMessage}")
                                        } finally {
                                            isSavingLink = false
                                        }
                                    }
                                }
                            },
                            enabled = linkUrl.isNotBlank() && !isSavingLink,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = extended.buttonPrimary,
                                contentColor = extended.buttonPrimaryText
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isSavingLink) "Saving Link…" else "Save Link", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SnapActionTile(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(extended.surfaceSecondary)
            .border(1.dp, extended.border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(extended.cardBackground)
                .border(1.dp, extended.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = extended.primaryText,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = extended.primaryText
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = extended.secondaryText
            )
        }
    }
}
