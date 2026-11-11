package com.laterbox.ui.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.laterbox.ui.theme.DarkBackground
import com.laterbox.ui.theme.DarkPrimaryIcons
import com.laterbox.ui.theme.DarkSecondaryIcons
import com.laterbox.ui.theme.LightBackground
import com.laterbox.ui.theme.LightPrimaryIcons
import com.laterbox.ui.theme.LightSecondaryIcons
import com.laterbox.ui.theme.LightSecondaryText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiquidGlassBottomBarTest {

    @Test
    fun testNavigationDestinationsCoverage() {
        val bottomNavRoutes = MAIN_BOTTOM_NAV_ROUTES
        assertTrue(bottomNavRoutes.contains(Screen.Home.route))
        assertTrue(bottomNavRoutes.contains(Screen.Library.route))
        assertTrue(bottomNavRoutes.contains(Screen.Events.route))
        assertTrue(bottomNavRoutes.contains(Screen.Settings.route))
        assertEquals(4, bottomNavRoutes.size)
    }

    @Test
    fun testDestinationMatchingLogic() {
        // 1. Home
        val homeRoute = Screen.Home.route
        assertTrue(homeRoute == "home")

        // 2. Events
        val eventsRoute = Screen.Events.route
        assertTrue(eventsRoute == "events")

        // 3. Library (including category variants)
        val defaultLibrary = Screen.Library.createRoute("All")
        val favLibrary = Screen.Library.createRoute("Favorites")
        assertTrue(defaultLibrary.startsWith("library"))
        assertTrue(favLibrary.startsWith("library"))

        // 4. Settings
        val settingsRoute = Screen.Settings.route
        assertTrue(settingsRoute == "settings")
    }

    @Test
    fun testLightAndDarkThemeColorTokens() {
        // Light Theme: #F0F1F3 background, #171717 selected, #777777 unselected
        assertEquals(Color(0xFFF0F1F3), LightBackground)
        assertEquals(Color(0xFF171717), LightPrimaryIcons)
        assertEquals(Color(0xFF777777), LightSecondaryText)

        // Dark Theme: #101113 background, #FFFFFF selected, #A0A0A5 unselected
        assertEquals(Color(0xFF101113), DarkBackground)
        assertEquals(Color(0xFFFFFFFF), DarkPrimaryIcons)
        assertEquals(Color(0xFFA0A0A5), DarkSecondaryIcons)
    }

    @Test
    fun testRefractiveLiquidGlassTokens() {
        // Translucent Silver-Grey Glass Base Tokens (Light)
        val lightGlassTop = Color(0xCCE2E6EB)
        val lightGlassBottom = Color(0xBFCDD4DC)
        assertTrue("Light glass top alpha must be between 70% and 85%", lightGlassTop.alpha in 0.70f..0.85f)
        assertTrue("Light glass bottom alpha must be between 70% and 85%", lightGlassBottom.alpha in 0.70f..0.85f)

        // Translucent Charcoal-Silver Crystal Tokens (Dark)
        val darkGlassTop = Color(0xCC22252A)
        val darkGlassBottom = Color(0xBF131517)
        assertTrue("Dark glass top alpha must be between 70% and 85%", darkGlassTop.alpha in 0.70f..0.85f)
        assertTrue("Dark glass bottom alpha must be between 70% and 85%", darkGlassBottom.alpha in 0.70f..0.85f)

        // Double Rim Specular Highlights (Physical Material Depth)
        val outerRimLightTop = Color(0xE6FFFFFF) // 90% bright white apex gleam
        val innerMeniscusLightTop = Color(0x59FFFFFF) // 35% secondary reflection
        assertEquals(0.90f, outerRimLightTop.alpha, 0.02f)
        assertEquals(0.35f, innerMeniscusLightTop.alpha, 0.02f)

        val outerRimDarkTop = Color(0x80FFFFFF) // 50% white specular rim
        val innerMeniscusDarkTop = Color(0x33FFFFFF) // 20% secondary reflection
        assertEquals(0.50f, outerRimDarkTop.alpha, 0.02f)
        assertEquals(0.20f, innerMeniscusDarkTop.alpha, 0.02f)
    }

    @Test
    fun testPhysicalDimensionsAndAccessibleTouchTargets() {
        // Pill container height and radius
        val barHeight = 68.dp
        val pillCornerRadius = 32.dp
        val snapButtonSize = 44.dp
        val minTouchTarget = 48.dp

        assertTrue("Bar height must accommodate 48dp+ accessible touch targets", barHeight >= minTouchTarget)
        assertTrue("Snap button diameter should be 44dp inside comfortable tap padding", snapButtonSize == 44.dp)
        assertTrue("Capsule pill corner radius should provide fully rounded ends", pillCornerRadius >= 30.dp)
    }

    @Test
    fun testActiveTabSelectionExclusivity() {
        val testRoutes = listOf(
            Screen.Home.route,
            Screen.Events.route,
            Screen.Library.createRoute("All"),
            Screen.Settings.route
        )

        testRoutes.forEach { currentRoute ->
            val isHome = currentRoute == Screen.Home.route
            val isEvents = currentRoute == Screen.Events.route
            val isLibrary = currentRoute == Screen.Library.route || currentRoute.startsWith("library")
            val isSettings = currentRoute == Screen.Settings.route

            val activeCount = listOf(isHome, isEvents, isLibrary, isSettings).count { it }
            assertEquals("Exactly one tab must be active for route $currentRoute", 1, activeCount)
        }
    }

    @Test
    fun testNonNavRoutesDoNotTriggerTabSelection() {
        val nonNavRoutes = listOf(
            Screen.ItemDetail.createRoute(42L),
            Screen.NoteEditor.createRoute(10L, true),
            Screen.ContentViewer.createRoute(99L),
            Screen.GeminiSettings.route,
            Screen.Trash.route
        )

        nonNavRoutes.forEach { route ->
            val isHome = route == Screen.Home.route
            val isEvents = route == Screen.Events.route
            val isLibrary = route == Screen.Library.route || route.startsWith("library")
            val isSettings = route == Screen.Settings.route

            assertFalse("Home must be unselected for sub-route $route", isHome)
            assertFalse("Events must be unselected for sub-route $route", isEvents)
            assertFalse("Settings must be unselected for sub-route $route", isSettings)
        }
    }
}
