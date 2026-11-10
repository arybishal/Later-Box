package com.laterbox.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePaletteTest {

    @Test
    fun verifyMonochromePalette() {
        // Light Theme Tokens
        assertEquals(0xFFFFFFFF.toInt(), PureWhiteBackground.toArgb())
        assertEquals(0xFF111111.toInt(), PrimaryBlackText.toArgb())
        assertEquals(0xFFF2F2F2.toInt(), NeutralSurfaceLight.toArgb())
        assertEquals(0xFFE5E5E5.toInt(), SubtleBorderLight.toArgb())
        assertEquals(0xFF666666.toInt(), NeutralGreySecondary.toArgb())

        // Dark Theme Tokens
        assertEquals(0xFF000000.toInt(), CharcoalBackgroundDark.toArgb())
        assertEquals(0xFF171717.toInt(), CharcoalSurfaceDark.toArgb())
        assertEquals(0xFF222222.toInt(), CharcoalSurfaceVariantDark.toArgb())
        assertEquals(0xFFFFFFFF.toInt(), WarmTextPrimaryDark.toArgb())
        assertEquals(0xFFB3B3B3.toInt(), WarmTextSecondaryDark.toArgb())
        assertEquals(0xFF303030.toInt(), CharcoalBorderDark.toArgb())

        // Prohibited Colors: Ensure no yellow (#FFF4B8), amber, or brown/ochre (#9E6F21)
        assertNotEquals(0xFFFFF4B8.toInt(), PrimaryBlackText.toArgb())
        assertNotEquals(0xFF9E6F21.toInt(), PrimaryBlackText.toArgb())
        assertNotEquals(0xFFFFF4B8.toInt(), NeutralSurfaceLight.toArgb())
    }

    @Test
    fun verifyNoRedFontOrCrimsonColors() {
        // Red font is strictly prohibited across the app
        assertTrue("Destructive is not vivid red", SlateDestructive.red < 0.6f)
        assertTrue("Destructive has neutral slate balance", SlateDestructive.green > 0.2f && SlateDestructive.blue > 0.2f)
        assertTrue("Secondary text is neutral grey", NeutralGreySecondary.red < 0.5f)
    }
}
