package com.laterbox.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePaletteTest {

    @Test
    fun verifyNoBlueDominantColorsInAccent() {
        // Red, Green, Blue components
        val amberRed = AmberAccent.red
        val amberGreen = AmberAccent.green
        val amberBlue = AmberAccent.blue

        // Amber/warm yellow must have higher red and green than blue
        assertTrue("Amber must be warm (red > blue)", amberRed > amberBlue)
        assertTrue("Amber must be warm (green > blue)", amberGreen > amberBlue)

        val amberLightRed = AmberAccentLight.red
        val amberLightGreen = AmberAccentLight.green
        val amberLightBlue = AmberAccentLight.blue

        assertTrue("Amber light must be warm (red > blue)", amberLightRed > amberLightBlue)
        assertTrue("Amber light must be warm (green > blue)", amberLightGreen > amberLightBlue)
    }

    @Test
    fun verifyNoCrimsonColors() {
        // Crimson typically has high red (~0.85+) and very low green (<0.1) with moderate blue (~0.25-0.35)
        val terracottaRed = WarmTerracotta.red
        val terracottaGreen = WarmTerracotta.green
        val terracottaBlue = WarmTerracotta.blue

        // Ensure terracotta is an earthy warm brick tone rather than vivid crimson
        assertTrue("Error color should be balanced terracotta", terracottaRed < 0.9f)
        assertTrue("Terracotta has warm earth undertone", terracottaGreen > 0.2f)
    }

    @Test
    fun verifyExactScreenshotPaletteCalibration() {
        assertEquals(0xFFF4F0E8.toInt(), WarmCreamBackground.toArgb())
        assertEquals(0xFFFBF8F3.toInt(), WarmCardBackground.toArgb())
        assertEquals(0xFF9C6E1E.toInt(), WarmCaramelAccent.toArgb())
        assertEquals(0xFF1C1915.toInt(), WarmTextPrimaryLight.toArgb())
        assertEquals(0xFF5C564E.toInt(), WarmTextSecondaryLight.toArgb())
        assertEquals(0xFFE2D9CC.toInt(), WarmBorderDashed.toArgb())
        assertEquals(0xFFEFE8DC.toInt(), PillInactiveBackground.toArgb())
        assertEquals(0xFFF3E4C4.toInt(), GridListActiveBackground.toArgb())
        assertTrue("Warm Caramel has strong warm character", WarmCaramelAccent.red > WarmCaramelAccent.blue)
        assertTrue("Warm Cream is soft warm ivory", WarmCreamBackground.red > WarmCreamBackground.blue)
    }
}
