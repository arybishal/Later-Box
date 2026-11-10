package com.laterbox.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePaletteTest {

    @Test
    fun verifyWhiteBlackAndSoftYellowScheme() {
        assertEquals(0xFFFFFFFF.toInt(), PureWhiteBackground.toArgb())
        assertEquals(0xFF111111.toInt(), PrimaryBlackText.toArgb())
        assertEquals(0xFFFFF4B8.toInt(), SoftButterYellow.toArgb())

        // Ensure brown / ochre (#9E6F21 and #9C6E1E) is NOT used
        assertNotEquals(0xFF9E6F21.toInt(), SoftButterYellow.toArgb())
        assertNotEquals(0xFF9C6E1E.toInt(), SoftButterYellow.toArgb())

        // Soft butter yellow is bright, light, and delicate (#FFF4B8)
        assertTrue("Soft yellow highlight is bright red", SoftButterYellow.red > 0.95f)
        assertTrue("Soft yellow highlight is bright green", SoftButterYellow.green > 0.9f)
        assertTrue("Soft yellow highlight has subtle butter blue", SoftButterYellow.blue in 0.65f..0.8f)

        // White background is pure white
        assertEquals(1f, PureWhiteBackground.red, 0.01f)
        assertEquals(1f, PureWhiteBackground.green, 0.01f)
        assertEquals(1f, PureWhiteBackground.blue, 0.01f)
    }

    @Test
    fun verifyNoRedFontOrCrimsonColors() {
        // Red font is strictly prohibited across the app
        assertTrue("Destructive is not vivid red", SlateDestructive.red < 0.6f)
        assertTrue("Destructive has neutral balance", SlateDestructive.green > 0.2f && SlateDestructive.blue > 0.2f)
        assertTrue("Secondary text is neutral charcoal", CharcoalTextSecondary.red < 0.5f)
    }
}
