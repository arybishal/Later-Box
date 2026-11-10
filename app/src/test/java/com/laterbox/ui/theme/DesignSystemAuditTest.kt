package com.laterbox.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DesignSystemAuditTest {

    @Test
    fun testOfficialLightThemeColors() {
        assertEquals("Main background must be #F0F1F3", Color(0xFFF0F1F3), LightBackground)
        assertEquals("Card background must be #FFFFFF", Color(0xFFFFFFFF), LightCardBackground)
        assertEquals("Secondary surface must be #F5F5F5", Color(0xFFF5F5F5), LightSecondarySurface)
        assertEquals("Elevated surface must be #FFFFFF", Color(0xFFFFFFFF), LightElevatedSurface)
        assertEquals("Primary text must be #171717", Color(0xFF171717), LightPrimaryText)
        assertEquals("Secondary text must be #777777", Color(0xFF777777), LightSecondaryText)
        assertEquals("Tertiary text must be #999999", Color(0xFF999999), LightTertiaryText)
        assertEquals("Primary icons must be #171717", Color(0xFF171717), LightPrimaryIcons)
        assertEquals("Secondary icons must be #8B8B8B", Color(0xFF8B8B8B), LightSecondaryIcons)
        assertEquals("Borders and dividers must be #E9E9E9", Color(0xFFE9E9E9), LightBorder)
        assertEquals("Interactive accent must be #1687D9", Color(0xFF1687D9), LightInteractiveAccent)
        assertEquals("Pressed surface must be #E7E8EA", Color(0xFFE7E8EA), LightPressedSurface)
        assertEquals("Disabled elements must be #B5B5B5", Color(0xFFB5B5B5), LightDisabled)
    }

    @Test
    fun testOfficialDarkThemeColors() {
        assertEquals("Main background must be #101113", Color(0xFF101113), DarkBackground)
        assertEquals("Primary card background must be #1C1D20", Color(0xFF1C1D20), DarkCardBackground)
        assertEquals("Secondary surface must be #252629", Color(0xFF252629), DarkSecondarySurface)
        assertEquals("Elevated surface must be #292A2E", Color(0xFF292A2E), DarkElevatedSurface)
        assertEquals("Primary text must be #FFFFFF", Color(0xFFFFFFFF), DarkPrimaryText)
        assertEquals("Secondary text must be #B7B7BB", Color(0xFFB7B7BB), DarkSecondaryText)
        assertEquals("Tertiary text must be #88888E", Color(0xFF88888E), DarkTertiaryText)
        assertEquals("Primary icons must be #FFFFFF", Color(0xFFFFFFFF), DarkPrimaryIcons)
        assertEquals("Secondary icons must be #A0A0A5", Color(0xFFA0A0A5), DarkSecondaryIcons)
        assertEquals("Borders and dividers must be #333438", Color(0xFF333438), DarkBorder)
        assertEquals("Interactive accent must be #55A9EE", Color(0xFF55A9EE), DarkInteractiveAccent)
        assertEquals("Pressed surface must be #303136", Color(0xFF303136), DarkPressedSurface)
        assertEquals("Disabled elements must be #68686D", Color(0xFF68686D), DarkDisabled)
    }

    @Test
    fun testDestructiveColorsZeroRedFont() {
        assertEquals("Light destructive must be slate #374151", Color(0xFF374151), SlateDestructive)
        assertEquals("Dark destructive must be slate #4B5563", Color(0xFF4B5563), SlateDestructiveDark)
    }

    @Test
    fun testNoProhibitedYellowHexInColorPalette() {
        val colorsToCheck = listOf(
            LightBackground,
            LightCardBackground,
            LightSecondarySurface,
            LightElevatedSurface,
            LightBorder,
            LightPrimaryText,
            LightSecondaryText,
            LightTertiaryText,
            LightPrimaryIcons,
            LightSecondaryIcons,
            LightPressedSurface,
            LightDisabled,
            DarkBackground,
            DarkCardBackground,
            DarkSecondarySurface,
            DarkElevatedSurface,
            DarkPrimaryText,
            DarkSecondaryText,
            DarkTertiaryText,
            DarkPrimaryIcons,
            DarkSecondaryIcons,
            DarkBorder,
            DarkPressedSurface,
            DarkDisabled,
            SlateDestructive,
            SlateDestructiveDark
        )

        for (color in colorsToCheck) {
            val r = (color.red * 255).toInt()
            val g = (color.green * 255).toInt()
            val b = (color.blue * 255).toInt()

            // Prohibited: Yellow / Amber has high red, high green, but low blue (e.g., #FFF4B8: r=255, g=244, b=184)
            val isYellowOrAmber = r > 200 && g > 180 && b < 190
            assertFalse(
                "Color with RGB ($r, $g, $b) is prohibited yellow/amber!",
                isYellowOrAmber
            )

            // Prohibited: Bright Red / Crimson font (high red, very low green and blue)
            val isBrightRed = r > 180 && g < 80 && b < 80
            assertFalse(
                "Color with RGB ($r, $g, $b) is prohibited bright red!",
                isBrightRed
            )
        }
    }
}
