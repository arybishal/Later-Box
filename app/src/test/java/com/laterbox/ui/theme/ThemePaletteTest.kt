package com.laterbox.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePaletteTest {

    @Test
    fun verifyScreenshotWarmIvoryAndCaramelScheme() {
        assertEquals(0xFFF4F0E8.toInt(), WarmCreamBackground.toArgb())
        assertEquals(0xFF1C1915.toInt(), WarmTextPrimaryLight.toArgb())
        assertEquals(0xFF9C6E1E.toInt(), WarmCaramelAccent.toArgb())
        assertEquals(0xFFE2D9CC.toInt(), WarmBorderDashed.toArgb())
        assertEquals(0xFFFBF8F3.toInt(), WarmCardBackground.toArgb())

        // Caramel/Ochre accent has warm golden tone
        assertTrue("Caramel accent is warm", WarmCaramelAccent.red > WarmCaramelAccent.blue)
        assertTrue("Caramel accent has golden ratio", WarmCaramelAccent.green > WarmCaramelAccent.blue)
    }

    @Test
    fun verifyNoRedFontOrCrimsonColors() {
        // Red font is strictly prohibited across the app
        assertTrue("Destructive is not vivid red", SlateDestructive.red < 0.6f)
        assertTrue("Destructive has neutral balance", SlateDestructive.green > 0.2f && SlateDestructive.blue > 0.2f)
        assertTrue("Secondary text is neutral", WarmTextSecondaryLight.red < 0.5f)
    }
}
