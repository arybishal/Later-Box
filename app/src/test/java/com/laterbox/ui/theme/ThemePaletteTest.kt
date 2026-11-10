package com.laterbox.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePaletteTest {

    @Test
    fun verifyWhiteBlackAndVibrantYellowScheme() {
        assertEquals(0xFFFFFFFF.toInt(), PureWhiteBackground.toArgb())
        assertEquals(0xFF000000.toInt(), SolidBlackText.toArgb())
        assertEquals(0xFFF5BA13.toInt(), VibrantYellowAccent.toArgb())

        // Ensure brown / ochre (#9E6F21 and #9C6E1E) is NOT used
        assertNotEquals(0xFF9E6F21.toInt(), VibrantYellowAccent.toArgb())
        assertNotEquals(0xFF9C6E1E.toInt(), VibrantYellowAccent.toArgb())

        // Vibrant yellow has high red and green, low blue
        assertTrue("Yellow accent is warm", VibrantYellowAccent.red > 0.8f)
        assertTrue("Yellow accent has golden green ratio", VibrantYellowAccent.green > 0.6f)
        assertTrue("Yellow accent has low blue", VibrantYellowAccent.blue < 0.2f)
    }

    @Test
    fun verifyNoRedFontOrCrimsonColors() {
        // Red font is strictly prohibited across the app
        assertTrue("Destructive is not vivid red", SlateDestructive.red < 0.6f)
        assertTrue("Destructive has neutral balance", SlateDestructive.green > 0.2f && SlateDestructive.blue > 0.2f)
        assertTrue("Secondary text is neutral charcoal", CharcoalTextSecondary.red < 0.5f)
    }
}
