package com.laterbox.ui.gesture

import androidx.compose.ui.geometry.Offset
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreeFingerSwipeTest {

    private val minDistancePx = 100f
    private val maxVerticalDriftPx = 80f

    @Test
    fun `test 3-finger horizontal swipe right detected accurately`() {
        val displacements = listOf(
            Offset(120f, 10f),
            Offset(130f, -5f),
            Offset(110f, 15f)
        )
        val result = ThreeFingerSwipeTracker.evaluateSwipe(displacements, minDistancePx, maxVerticalDriftPx)
        assertEquals(SwipeDirection.RIGHT, result)
    }

    @Test
    fun `test 3-finger horizontal swipe left detected accurately`() {
        val displacements = listOf(
            Offset(-120f, 10f),
            Offset(-130f, -5f),
            Offset(-110f, 15f)
        )
        val result = ThreeFingerSwipeTracker.evaluateSwipe(displacements, minDistancePx, maxVerticalDriftPx)
        assertEquals(SwipeDirection.LEFT, result)
    }

    @Test
    fun `test 1-finger swipe is rejected to allow normal scrolling`() {
        val displacements = listOf(
            Offset(200f, 0f)
        )
        val result = ThreeFingerSwipeTracker.evaluateSwipe(displacements, minDistancePx, maxVerticalDriftPx)
        assertNull(result)
    }

    @Test
    fun `test 2-finger swipe is rejected to allow pinch-to-zoom`() {
        val displacements = listOf(
            Offset(150f, 0f),
            Offset(160f, 0f)
        )
        val result = ThreeFingerSwipeTracker.evaluateSwipe(displacements, minDistancePx, maxVerticalDriftPx)
        assertNull(result)
    }

    @Test
    fun `test 4-finger touch is rejected`() {
        val displacements = listOf(
            Offset(150f, 0f),
            Offset(160f, 0f),
            Offset(140f, 0f),
            Offset(155f, 0f)
        )
        val result = ThreeFingerSwipeTracker.evaluateSwipe(displacements, minDistancePx, maxVerticalDriftPx)
        assertNull(result)
    }

    @Test
    fun `test excessive vertical drift is rejected to prevent vertical scroll conflict`() {
        // Vertical movement of 120px exceeds 80px tolerance
        val displacements = listOf(
            Offset(150f, 120f),
            Offset(140f, 110f),
            Offset(160f, 130f)
        )
        val result = ThreeFingerSwipeTracker.evaluateSwipe(displacements, minDistancePx, maxVerticalDriftPx)
        assertNull(result)
    }

    @Test
    fun `test movement below horizontal threshold is rejected`() {
        // Horizontal movement of 50px is below 100px minimum threshold
        val displacements = listOf(
            Offset(50f, 10f),
            Offset(45f, 5f),
            Offset(55f, 15f)
        )
        val result = ThreeFingerSwipeTracker.evaluateSwipe(displacements, minDistancePx, maxVerticalDriftPx)
        assertNull(result)
    }

    @Test
    fun `test SavedItemEntity gesture snap memory integrity`() {
        val item = SavedItemEntity(
            id = 500L,
            url = "laterbox://snap/1700000000000",
            title = "Snap Capture · Nov 14, 2026",
            contentType = ContentType.IMAGE,
            isSnap = true,
            captureMethod = "THREE_FINGER_SWIPE",
            sourcePackage = "com.android.chrome"
        )

        assertTrue(item.isSnap)
        assertEquals("THREE_FINGER_SWIPE", item.captureMethod)
        assertEquals("com.android.chrome", item.sourcePackage)
        assertEquals(ContentType.IMAGE, item.contentType)
    }
}
