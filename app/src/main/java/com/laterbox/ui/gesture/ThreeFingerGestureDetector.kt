package com.laterbox.ui.gesture

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Window
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs

enum class SwipeDirection {
    LEFT,
    RIGHT
}

/**
 * Robust multi-touch gesture tracker that detects when exactly three fingers
 * perform a horizontal swipe gesture (left or right).
 *
 * Rejects 1-finger and 2-finger interactions so normal scrolling, tapping,
 * and pinch-to-zoom gestures are completely uninhibited.
 */
class ThreeFingerSwipeTracker(
    private val minDistancePx: Float,
    private val maxVerticalDriftPx: Float,
    private val cooldownMs: Long = 1500L,
    private val onSwipe: (SwipeDirection) -> Unit
) {
    private var startPositions = mutableMapOf<PointerId, Offset>()
    private var lastTriggerTime: Long = 0L

    fun processEvent(event: PointerEvent) {
        val changes = event.changes
        val pointerCount = changes.size

        // Only process when exactly 3 fingers are touching the screen
        if (pointerCount != 3) {
            startPositions.clear()
            return
        }

        val allDown = changes.all { it.pressed }
        val anyJustDown = changes.any { it.previousPressed.not() }

        if (anyJustDown || startPositions.size != 3) {
            startPositions.clear()
            changes.forEach { change ->
                startPositions[change.id] = change.position
            }
            return
        }

        // Calculate displacement for each of the 3 pointers
        var totalDx = 0f
        var totalDy = 0f
        var validPointers = 0

        for (change in changes) {
            val start = startPositions[change.id] ?: continue
            val dx = change.position.x - start.x
            val dy = change.position.y - start.y

            totalDx += dx
            totalDy += dy
            validPointers++
        }

        if (validPointers == 3) {
            val direction = evaluateSwipe(
                changes.map { change ->
                    val start = startPositions[change.id] ?: Offset.Zero
                    Offset(change.position.x - start.x, change.position.y - start.y)
                },
                minDistancePx,
                maxVerticalDriftPx
            )

            if (direction != null) {
                val now = System.currentTimeMillis()
                if (now - lastTriggerTime >= cooldownMs) {
                    lastTriggerTime = now
                    onSwipe(direction)
                    startPositions.clear()
                    changes.forEach { it.consume() }
                }
            }
        }

        if (!allDown) {
            startPositions.clear()
        }
    }

    fun isCooldownActive(): Boolean {
        return System.currentTimeMillis() - lastTriggerTime < cooldownMs
    }

    companion object {
        fun evaluateSwipe(
            displacements: List<Offset>,
            minDistancePx: Float,
            maxVerticalDriftPx: Float
        ): SwipeDirection? {
            if (displacements.size != 3) return null
            var totalDx = 0f
            var totalDy = 0f
            for (d in displacements) {
                totalDx += d.x
                totalDy += d.y
            }
            val avgDx = totalDx / 3f
            val avgDy = totalDy / 3f
            if (abs(avgDy) <= maxVerticalDriftPx && abs(avgDx) >= minDistancePx) {
                return if (avgDx > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
            }
            return null
        }
    }
}

/**
 * Root Compose modifier that listens for three-finger horizontal swipe gestures.
 */
fun Modifier.threeFingerSwipeDetector(
    minDistance: Dp = 70.dp,
    maxVerticalDrift: Dp = 60.dp,
    cooldownMs: Long = 1500L,
    enabled: Boolean = true,
    onSwipe: (SwipeDirection) -> Unit
): Modifier = if (!enabled) this else pointerInput(minDistance, maxVerticalDrift, cooldownMs, enabled) {
    val minDistancePx = minDistance.toPx()
    val maxVerticalDriftPx = maxVerticalDrift.toPx()

    val tracker = ThreeFingerSwipeTracker(
        minDistancePx = minDistancePx,
        maxVerticalDriftPx = maxVerticalDriftPx,
        cooldownMs = cooldownMs,
        onSwipe = onSwipe
    )

    awaitEachGesture {
        while (true) {
            val event = awaitPointerEvent()
            tracker.processEvent(event)
        }
    }
}

/**
 * PixelCopy window capture utility to capture the current in-app window safely
 * across all modern Android versions (Android 8 through Android 15).
 */
object WindowCaptureHelper {

    suspend fun captureWindow(activity: Activity): Bitmap? {
        val window: Window = activity.window ?: return null
        val view = window.decorView

        val width = view.width
        val height = view.height
        if (width <= 0 || height <= 0) return null

        return suspendCancellableCoroutine { continuation ->
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val handler = Handler(Looper.getMainLooper())

            try {
                PixelCopy.request(
                    window,
                    Rect(0, 0, width, height),
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            continuation.resume(bitmap)
                        } else {
                            continuation.resume(null)
                        }
                    },
                    handler
                )
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }
}
