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
        assertEquals("Main background must be #FFFFFF", Color(0xFFFFFFFF), PureWhiteBackground)
        assertEquals("Card background must be #FFFFFF", Color(0xFFFFFFFF), CleanCardBackground)
        assertEquals("Secondary surface must be #F2F2F2", Color(0xFFF2F2F2), NeutralSurfaceLight)
        assertEquals("Borders and dividers must be #E5E5E5", Color(0xFFE5E5E5), SubtleBorderLight)
        assertEquals("Primary text must be #111111", Color(0xFF111111), PrimaryBlackText)
        assertEquals("Secondary text must be #666666", Color(0xFF666666), NeutralGreySecondary)
        assertEquals("Primary button container must be #111111", Color(0xFF111111), PrimaryButtonLight)
        assertEquals("Primary button text must be #FFFFFF", Color(0xFFFFFFFF), PrimaryButtonTextLight)
        assertEquals("Disabled elements must be #A0A0A0", Color(0xFFA0A0A0), DisabledNeutralLight)
    }

    @Test
    fun testOfficialDarkThemeColors() {
        assertEquals("Main background must be #000000", Color(0xFF000000), CharcoalBackgroundDark)
        assertEquals("Primary surface must be #171717", Color(0xFF171717), CharcoalSurfaceDark)
        assertEquals("Secondary surface must be #222222", Color(0xFF222222), CharcoalSurfaceVariantDark)
        assertEquals("Borders and dividers must be #303030", Color(0xFF303030), CharcoalBorderDark)
        assertEquals("Primary text must be #FFFFFF", Color(0xFFFFFFFF), WarmTextPrimaryDark)
        assertEquals("Secondary text must be #B3B3B3", Color(0xFFB3B3B3), WarmTextSecondaryDark)
        assertEquals("Primary button container must be #FFFFFF", Color(0xFFFFFFFF), PrimaryButtonDark)
        assertEquals("Primary button text must be #111111", Color(0xFF111111), PrimaryButtonTextDark)
        assertEquals("Disabled elements must be #666666", Color(0xFF666666), DisabledNeutralDark)
    }

    @Test
    fun testDestructiveColorsZeroRedFont() {
        assertEquals("Light destructive must be slate #374151", Color(0xFF374151), SlateDestructive)
        assertEquals("Dark destructive must be slate #4B5563", Color(0xFF4B5563), SlateDestructiveDark)
    }

    @Test
    fun testNoProhibitedYellowHexInColorPalette() {
        val colorsToCheck = listOf(
            PureWhiteBackground,
            CleanCardBackground,
            NeutralSurfaceLight,
            SubtleBorderLight,
            PrimaryBlackText,
            NeutralGreySecondary,
            DisabledNeutralLight,
            CharcoalBackgroundDark,
            CharcoalSurfaceDark,
            CharcoalSurfaceVariantDark,
            WarmTextPrimaryDark,
            WarmTextSecondaryDark,
            CharcoalBorderDark,
            DisabledNeutralDark,
            PrimaryButtonLight,
            PrimaryButtonTextLight,
            PrimaryButtonDark,
            PrimaryButtonTextDark,
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

    @Test
    fun testSourceCodeHasZeroActiveYellowReferences() {
        val rootDir = File("src/main/java/com/laterbox")
        if (!rootDir.exists()) return // skip if path differs in test runner

        val prohibitedHexPatterns = listOf(
            "FFF4B8", "FFFBEA", "F9EEB8", "F7E582", "FFD700", "F59E0B"
        )

        rootDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val content = file.readText()
            for (pattern in prohibitedHexPatterns) {
                assertFalse(
                    "File ${file.name} contains prohibited color pattern $pattern!",
                    content.contains(pattern, ignoreCase = true)
                )
            }
        }
    }
}
