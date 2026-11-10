package com.laterbox.data.ai

import com.laterbox.data.ai.model.GeminiModels
import com.laterbox.data.ai.network.GeminiApiClient
import com.laterbox.data.ai.prompt.GeminiPromptBuilder
import com.laterbox.data.ai.security.maskApiKey
import com.laterbox.data.local.entity.AiAnalysisEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiAiTest {

    @Test
    fun `GeminiModels contains approved models with flash as default`() {
        val models = GeminiModels.ALL_MODELS
        assertTrue(models.any { it.id == "gemini-1.5-flash" })
        assertTrue(models.any { it.id == "gemini-2.0-flash" })
        assertTrue(models.any { it.id == "gemini-1.5-pro" })
        assertEquals("gemini-1.5-flash", GeminiModels.DEFAULT_MODEL_ID)

        val resolved = GeminiModels.findModel("unknown-model-id")
        assertEquals(GeminiModels.DEFAULT_MODEL_ID, resolved.id)
    }

    @Test
    fun `GeminiPromptBuilder generates content-aware prompt`() {
        val articleItem = SavedItemEntity(
            id = 1L,
            url = "https://developer.android.com/jetpack/compose",
            title = "Modern Android Development with Jetpack Compose",
            description = "Declarative UI toolkit for building native Android interfaces.",
            notes = "Review this for Later Box redesign",
            contentType = ContentType.ARTICLE
        )

        val prompt = GeminiPromptBuilder.buildAnalysisPrompt(articleItem)
        assertTrue(prompt.contains("Title: Modern Android Development with Jetpack Compose"))
        assertTrue(prompt.contains("Original URL: https://developer.android.com/jetpack/compose"))
        assertTrue(prompt.contains("User Personal Note: Review this for Later Box redesign"))
        assertTrue(prompt.contains("shortDescription"))
        assertTrue(prompt.contains("keyTakeaways"))
    }

    @Test
    fun `GeminiPromptBuilder produces deterministic fingerprints`() {
        val item1 = SavedItemEntity(
            id = 10L,
            url = "https://kotlinlang.org",
            title = "Kotlin Programming Language",
            contentType = ContentType.ARTICLE
        )

        val item2 = SavedItemEntity(
            id = 10L,
            url = "https://kotlinlang.org",
            title = "Kotlin Programming Language",
            contentType = ContentType.ARTICLE
        )

        val item3 = SavedItemEntity(
            id = 10L,
            url = "https://kotlinlang.org",
            title = "Kotlin Programming Language (Updated)",
            contentType = ContentType.ARTICLE
        )

        val fp1 = GeminiPromptBuilder.computeContentFingerprint(item1)
        val fp2 = GeminiPromptBuilder.computeContentFingerprint(item2)
        val fp3 = GeminiPromptBuilder.computeContentFingerprint(item3)

        assertEquals(fp1, fp2)
        assertNotEquals(fp1, fp3)
        assertEquals(64, fp1.length) // SHA-256 hex length
    }

    @Test
    fun `GeminiApiClient parses raw structured JSON successfully`() {
        val client = GeminiApiClient()
        val jsonPayload = """
            {
              "shortDescription": "A comprehensive guide to Kotlin Coroutines.",
              "whatItsAbout": "Explains structured concurrency and asynchronous programming in Kotlin.",
              "category": "Technology",
              "location": {
                "name": "San Francisco, CA",
                "confidence": "high"
              },
              "peopleAndEntities": ["Roman Elizarov", "JetBrains", "Android"],
              "keyDetails": ["Supports async/await pattern", "Built on suspend functions"],
              "keyTakeaways": ["Simplifies asynchronous code", "Prevents thread leaks"],
              "contentType": "Article",
              "language": "English"
            }
        """.trimIndent()

        val parsed = client.parseStructuredJson(jsonPayload)
        assertEquals("A comprehensive guide to Kotlin Coroutines.", parsed.shortDescription)
        assertEquals("Explains structured concurrency and asynchronous programming in Kotlin.", parsed.whatItsAbout)
        assertEquals("Technology", parsed.category)
        assertEquals("San Francisco, CA", parsed.location?.name)
        assertEquals(3, parsed.peopleAndEntities.size)
        assertEquals("Roman Elizarov", parsed.peopleAndEntities[0])
        assertEquals(2, parsed.keyTakeaways.size)
        assertEquals("Simplifies asynchronous code", parsed.keyTakeaways[0])
    }

    @Test
    fun `GeminiApiClient parses markdown-fenced JSON successfully`() {
        val client = GeminiApiClient()
        val markdownFenced = """
            ```json
            {
              "shortDescription": "Recipe for authentic Italian pasta carbonara.",
              "whatItsAbout": "Traditional Roman pasta preparation using guanciale and pecorino.",
              "category": "Cooking",
              "peopleAndEntities": ["Italy", "Rome"],
              "keyTakeaways": ["Never use cream", "Cook guanciale until crispy"]
            }
            ```
        """.trimIndent()

        val parsed = client.parseStructuredJson(markdownFenced)
        assertEquals("Recipe for authentic Italian pasta carbonara.", parsed.shortDescription)
        assertEquals("Cooking", parsed.category)
        assertEquals(2, parsed.keyTakeaways.size)
    }

    @Test
    fun `GeminiApiClient provides graceful fallback for unparseable responses`() {
        val client = GeminiApiClient()
        val plainText = "This article discusses renewable energy trends in 2026."

        val parsed = client.parseStructuredJson(plainText)
        assertEquals("This article discusses renewable energy trends in 2026.", parsed.shortDescription)
        assertEquals("General", parsed.category)
        assertTrue(parsed.whatItsAbout.isNotEmpty())
    }

    @Test
    fun `AiAnalysisEntity parses JSON arrays safely`() {
        val entity = AiAnalysisEntity(
            itemId = 100L,
            shortDescription = "Sample description",
            whatItsAbout = "Sample about",
            category = "Design",
            peopleAndEntitiesJson = "[\"Figma\", \"Material Design 3\"]",
            keyTakeawaysJson = "[\"Use 8dp grid\", \"Support dynamic color\"]",
            modelUsed = "gemini-1.5-flash"
        )

        val entities = entity.getPeopleAndEntities()
        assertEquals(2, entities.size)
        assertEquals("Figma", entities[0])

        val takeaways = entity.getKeyTakeaways()
        assertEquals(2, takeaways.size)
        assertEquals("Use 8dp grid", takeaways[0])

        // Verify corrupted JSON returns emptyList without throwing exception
        val corruptedEntity = entity.copy(
            peopleAndEntitiesJson = "not-a-json-array",
            keyTakeawaysJson = "{ invalid }"
        )
        assertTrue(corruptedEntity.getPeopleAndEntities().isEmpty())
        assertTrue(corruptedEntity.getKeyTakeaways().isEmpty())
    }

    @Test
    fun `SecureApiKeyStorage masking masks sensitive API key characters`() {
        val key = "AIzaSyB1234567890abcdefABCDEF12345"
        val masked = maskApiKey(key)

        assertTrue(masked.startsWith("AIzaSy"))
        assertTrue(masked.endsWith("2345"))
        assertTrue(masked.contains("••••"))
        assertFalse(masked.contains("1234567890abcdef"))
    }
}
