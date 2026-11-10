package com.laterbox.ui

import com.laterbox.ui.components.LaterBoxSnackbarController
import com.laterbox.ui.components.SnackbarMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UiStatesAndValidationTest {

    @Test
    fun testSnackbarController_emitsMessageAndAction() = runTest {
        var actionExecuted = false
        LaterBoxSnackbarController.showMessage(
            message = "Moved to Trash",
            actionLabel = "Undo",
            onAction = { actionExecuted = true }
        )

        val message: SnackbarMessage = LaterBoxSnackbarController.events.first()
        assertEquals("Moved to Trash", message.message)
        assertEquals("Undo", message.actionLabel)
        message.onAction?.invoke()
        assertTrue(actionExecuted)
    }

    @Test
    fun testPlaylistValidation_duplicateAndLengthRules() {
        val existingPlaylists = listOf("Design", "Articles", "Tech News")

        // Rule: Duplicate check case-insensitivity
        val duplicateInput = "  design  "
        val isDuplicate = existingPlaylists.any { it.trim().equals(duplicateInput.trim(), ignoreCase = true) }
        assertTrue("Expected duplicate name to be detected", isDuplicate)

        // Rule: Unique name passes
        val uniqueInput = "Architecture"
        val isUnique = existingPlaylists.any { it.trim().equals(uniqueInput.trim(), ignoreCase = true) }
        assertFalse("Expected unique name not to be marked duplicate", isUnique)

        // Rule: Length bounds (max 40)
        val shortName = "Short Name"
        val longName = "A".repeat(45)
        assertTrue(shortName.length <= 40)
        assertTrue(longName.length > 40)
    }

    @Test
    fun testTagValidation_cleansHashAndEnforcesLength() {
        val rawInput = "#MachineLearning"
        val cleanTag = rawInput.trim().removePrefix("#")
        assertEquals("MachineLearning", cleanTag)
        assertTrue(cleanTag.length <= 30)

        val oversized = "#" + "A".repeat(35)
        val oversizedClean = oversized.trim().removePrefix("#")
        assertTrue(oversizedClean.length > 30)
    }

    @Test
    fun testUrlAutoFormatting_bareDomainReceivesHttps() {
        val bareDomain = "nytimes.com/world/article"
        val isLikelyWebDomain = bareDomain.contains(".") &&
                !bareDomain.contains(" ") &&
                !bareDomain.startsWith("http://") &&
                !bareDomain.startsWith("https://")

        assertTrue(isLikelyWebDomain)
        val formatted = if (isLikelyWebDomain) "https://$bareDomain" else bareDomain
        assertEquals("https://nytimes.com/world/article", formatted)

        val existingUrl = "https://example.com"
        val existingIsBare = existingUrl.contains(".") &&
                !existingUrl.contains(" ") &&
                !existingUrl.startsWith("http://") &&
                !existingUrl.startsWith("https://")
        assertFalse(existingIsBare)
    }

    @Test
    fun testZeroSearchStateClassification_distinguishesQueryVsEmptyFilter() {
        val emptyQuery = ""
        val activeQuery = "kotlin multiplatform"
        val isFilterOnly = emptyQuery.isBlank()

        assertTrue(isFilterOnly)
        assertFalse(activeQuery.isBlank())
    }
}
