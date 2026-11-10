package com.laterbox.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePaletteTest {

    @Test
    fun verifyV2DesignSystemPalette() {
        // Light Theme Tokens
        assertEquals(0xFFF0F1F3.toInt(), LightBackground.toArgb())
        assertEquals(0xFFFFFFFF.toInt(), LightCardBackground.toArgb())
        assertEquals(0xFFF5F5F5.toInt(), LightSecondarySurface.toArgb())
        assertEquals(0xFFFFFFFF.toInt(), LightElevatedSurface.toArgb())
        assertEquals(0xFF171717.toInt(), LightPrimaryText.toArgb())
        assertEquals(0xFF777777.toInt(), LightSecondaryText.toArgb())
        assertEquals(0xFF999999.toInt(), LightTertiaryText.toArgb())
        assertEquals(0xFF171717.toInt(), LightPrimaryIcons.toArgb())
        assertEquals(0xFF8B8B8B.toInt(), LightSecondaryIcons.toArgb())
        assertEquals(0xFFE9E9E9.toInt(), LightBorder.toArgb())
        assertEquals(0xFF1687D9.toInt(), LightInteractiveAccent.toArgb())
        assertEquals(0xFFE7E8EA.toInt(), LightPressedSurface.toArgb())
        assertEquals(0xFFB5B5B5.toInt(), LightDisabled.toArgb())

        // Dark Theme Tokens
        assertEquals(0xFF101113.toInt(), DarkBackground.toArgb())
        assertEquals(0xFF1C1D20.toInt(), DarkCardBackground.toArgb())
        assertEquals(0xFF252629.toInt(), DarkSecondarySurface.toArgb())
        assertEquals(0xFF292A2E.toInt(), DarkElevatedSurface.toArgb())
        assertEquals(0xFFFFFFFF.toInt(), DarkPrimaryText.toArgb())
        assertEquals(0xFFB7B7BB.toInt(), DarkSecondaryText.toArgb())
        assertEquals(0xFF88888E.toInt(), DarkTertiaryText.toArgb())
        assertEquals(0xFFFFFFFF.toInt(), DarkPrimaryIcons.toArgb())
        assertEquals(0xFFA0A0A5.toInt(), DarkSecondaryIcons.toArgb())
        assertEquals(0xFF333438.toInt(), DarkBorder.toArgb())
        assertEquals(0xFF55A9EE.toInt(), DarkInteractiveAccent.toArgb())
        assertEquals(0xFF303136.toInt(), DarkPressedSurface.toArgb())
        assertEquals(0xFF68686D.toInt(), DarkDisabled.toArgb())

        // Prohibited Colors: Ensure no yellow (#FFF4B8), amber, or brown/ochre (#9E6F21)
        assertNotEquals(0xFFFFF4B8.toInt(), LightPrimaryText.toArgb())
        assertNotEquals(0xFF9E6F21.toInt(), LightPrimaryText.toArgb())
        assertNotEquals(0xFFFFF4B8.toInt(), LightSecondarySurface.toArgb())
    }

    @Test
    fun verifyNoRedFontOrCrimsonColors() {
        // Red font is strictly prohibited across the app
        assertTrue("Destructive is not vivid red", SlateDestructive.red < 0.6f)
        assertTrue("Destructive has neutral slate balance", SlateDestructive.green > 0.2f && SlateDestructive.blue > 0.2f)
        assertTrue("Secondary text is neutral grey", LightSecondaryText.red < 0.6f)
    }
}
