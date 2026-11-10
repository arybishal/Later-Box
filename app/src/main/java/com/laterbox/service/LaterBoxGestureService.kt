package com.laterbox.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LaterBoxGestureService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastTriggerTime: Long = 0L
    private val cooldownMs: Long = 1500L
    private var currentForegroundPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString()
            if (!pkg.isNullOrBlank() && pkg != applicationContext.packageName) {
                currentForegroundPackage = pkg
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility feedback interrupted
    }

    @Suppress("DEPRECATION")
    override fun onGesture(gestureId: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) multi-finger swipe gestures
            // GESTURE_3_FINGER_SWIPE_LEFT = 41
            // GESTURE_3_FINGER_SWIPE_RIGHT = 42
            if (gestureId == GESTURE_3_FINGER_SWIPE_LEFT || gestureId == GESTURE_3_FINGER_SWIPE_RIGHT) {
                return handleThreeFingerSwipe()
            }
        }
        return super.onGesture(gestureId)
    }

    override fun onGesture(gestureEvent: android.accessibilityservice.AccessibilityGestureEvent): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val id = gestureEvent.gestureId
            if (id == GESTURE_3_FINGER_SWIPE_LEFT || id == GESTURE_3_FINGER_SWIPE_RIGHT) {
                return handleThreeFingerSwipe()
            }
        }
        return super.onGesture(gestureEvent)
    }

    private fun handleThreeFingerSwipe(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastTriggerTime < cooldownMs) {
            return true // Cooldown active, reject duplicate swipe
        }
        lastTriggerTime = now

        serviceScope.launch {
            val prefs = LaterBoxApp.instance.preferencesRepository
            val isGestureEnabled = prefs.threeFingerSwipeEnabled.first()
            if (!isGestureEnabled) return@launch

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                captureScreenApi30()
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun captureScreenApi30() {
        try {
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                applicationContext.mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshotResult: ScreenshotResult) {
                        serviceScope.launch {
                            val hardwareBuffer = screenshotResult.hardwareBuffer
                            val colorSpace = screenshotResult.colorSpace
                            val bitmap = try {
                                Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)?.copy(Bitmap.Config.ARGB_8888, false)
                            } catch (_: Exception) {
                                null
                            } finally {
                                hardwareBuffer.close()
                            }

                            if (bitmap != null) {
                                saveScreenshotMemory(bitmap, currentForegroundPackage)
                            }
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        // Failed to take screenshot
                    }
                }
            )
        } catch (_: Exception) {
            // Service not authorized or device policy restriction
        }
    }

    private suspend fun saveScreenshotMemory(bitmap: Bitmap, sourcePkg: String?) {
        try {
            val context = applicationContext
            val storageManager = LaterBoxApp.instance.mediaStorageManager
            val file = storageManager.createCameraImageFile()

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }

            val dateFormat = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
            val timestamp = System.currentTimeMillis()
            val formattedDate = dateFormat.format(Date(timestamp))

            val item = SavedItemEntity(
                url = "laterbox://snap/$timestamp",
                title = "Snap Capture · $formattedDate",
                description = if (!sourcePkg.isNullOrBlank()) "Captured from $sourcePkg" else "Captured via 3-finger horizontal swipe",
                contentType = ContentType.IMAGE,
                localMediaPath = file.absolutePath,
                isSnap = true,
                captureMethod = "THREE_FINGER_SWIPE",
                sourcePackage = sourcePkg,
                savedAt = timestamp
            )

            val id = LaterBoxApp.instance.contentRepository.saveItem(item)

            // Trigger AI analysis if enabled
            try {
                val app = LaterBoxApp.instance
                if (app.aiSettingsRepository.isAiEnabledNow() && app.aiSettingsRepository.isAutoAnalyzeNow()) {
                    com.laterbox.data.ai.AiAnalysisWorker.enqueue(app, id)
                }
            } catch (_: Exception) {}

            serviceScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Captured to Later Box Snaps", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {}
    }

    companion object {
        var isRunning: Boolean = false
            private set

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val expectedId = ComponentName(context, LaterBoxGestureService::class.java).flattenToString()
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedId, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }
}
