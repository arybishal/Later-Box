package com.laterbox.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingProgressTest {

    @Test
    fun testProgressCoercion() {
        val rawOver = 1.25f
        val clampedOver = rawOver.coerceIn(0f, 1f)
        assertEquals(1.0f, clampedOver, 0.001f)

        val rawUnder = -0.15f
        val clampedUnder = rawUnder.coerceIn(0f, 1f)
        assertEquals(0.0f, clampedUnder, 0.001f)

        val valid = 0.65f
        val clampedValid = valid.coerceIn(0f, 1f)
        assertEquals(0.65f, clampedValid, 0.001f)
    }

    @Test
    fun testPercentageFormatting() {
        val progress = 0.456f
        val percentage = (progress * 100).toInt()
        assertEquals(45, percentage)

        val fullProgress = 1.0f
        assertEquals(100, (fullProgress * 100).toInt())

        val zeroProgress = 0.0f
        assertEquals(0, (zeroProgress * 100).toInt())
    }

    @Test
    fun testRelativeDurationProgressCalculation() {
        val currentPositionMs = 45000L
        val totalDurationMs = 90000L
        val progress = (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
        assertEquals(0.5f, progress, 0.001f)
    }
}
