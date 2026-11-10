package com.laterbox.data.ai

import com.laterbox.data.ai.model.AiConnectionState
import com.laterbox.data.ai.model.GeminiModelInfo
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiAiTest {

    @Test
    fun `GeminiModels contains approved fallback models with flash as default`() {
        val models = GeminiModels.FALLBACK_MODELS
        assertTrue(models.any { it.id == "gemini-1.5-flash" })
        assertTrue(models.any { it.id == "gemini-2.0-flash" })
        assertTrue(models.any { it.id == "gemini-1.5-pro" })
        assertEquals("gemini-1.5-flash", GeminiModels.DEFAULT_MODEL_ID)

        val resolved = GeminiModels.findModel("unknown-model-id")
        assertEquals(GeminiModels.DEFAULT_MODEL_ID, resolved.id)
    }

    @Test
    fun `GeminiModelInfo normalizes and formats paths correctly`() {
        assertEquals("gemini-1.5-flash", GeminiModelInfo.normalizeModelId("models/gemini-1.5-flash"))
        assertEquals("gemini-1.5-flash", GeminiModelInfo.normalizeModelId("gemini-1.5-flash"))
        assertEquals("models/gemini-1.5-flash", GeminiModelInfo.toModelPath("gemini-1.5-flash"))
        assertEquals("models/gemini-1.5-flash", GeminiModelInfo.toModelPath("models/gemini-1.5-flash"))
    }

    @Test
    fun `GeminiModelInfo family categorization and token formatting work accurately`() {
        val flash = GeminiModelInfo(
            id = "gemini-2.5-flash",
            displayName = "Gemini 2.5 Flash",
            description = "Fast model",
            inputTokenLimit = 1048576,
            outputTokenLimit = 8192
        )
        assertEquals("Flash", flash.family)
        assertEquals("1M in · 8K out", flash.formatTokenLimits())

        val pro = GeminiModelInfo(
            id = "gemini-1.5-pro",
            displayName = "Gemini 1.5 Pro",
            description = "Pro model",
            inputTokenLimit = 2097152,
            outputTokenLimit = 8192
        )
        assertEquals("Pro", pro.family)
        assertEquals("2M in · 8K out", pro.formatTokenLimits())

        val exp = GeminiModelInfo(
            id = "gemini-exp-1206",
            displayName = "Gemini Experimental",
            description = "Experimental model"
        )
        assertEquals("Experimental", exp.family)
        assertEquals("", exp.formatTokenLimits())
    }

    @Test
    fun `GeminiApiClient parses models list JSON and filters non-generateContent models`() {
        val client = GeminiApiClient()
        val mockApiResponse = """
            {
              "models": [
                {
                  "name": "models/gemini-2.5-flash",
                  "version": "001",
                  "displayName": "Gemini 2.5 Flash",
                  "description": "Stable version of Gemini 2.5 Flash",
                  "inputTokenLimit": 1048576,
                  "outputTokenLimit": 8192,
                  "supportedGenerationMethods": ["generateContent", "countTokens"]
                },
                {
                  "name": "models/gemini-2.5-flash-image",
                  "version": "001",
                  "displayName": "Nano Banana",
                  "description": "Gemini 2.5 Flash Preview Image",
                  "supportedGenerationMethods": ["generateContent"]
                },
                {
                  "name": "models/text-embedding-004",
                  "version": "001",
                  "displayName": "Text Embedding 004",
                  "description": "Embedding model",
                  "supportedGenerationMethods": ["embedContent"]
                },
                {
                  "name": "models/aqa",
                  "version": "001",
                  "displayName": "Attributed Question Answering",
                  "supportedGenerationMethods": ["generateAnswer"]
                }
              ]
            }
        """.trimIndent()

        val parsed = client.parseModelsListJson(mockApiResponse)
        assertEquals(2, parsed.size)
        assertEquals("gemini-2.5-flash", parsed[0].id)
        assertTrue(parsed[0].isRecommended)
        assertEquals("gemini-2.5-flash-image", parsed[1].id)
        assertFalse(parsed[1].isRecommended)
    }

    @Test
    fun `GeminiApiClient parses paginated models response and extracts nextPageToken`() {
        val client = GeminiApiClient()
        val page1Json = """
            {
              "models": [
                {
                  "name": "models/gemini-2.5-flash",
                  "displayName": "Gemini 2.5 Flash",
                  "supportedGenerationMethods": ["generateContent"]
                }
              ],
              "nextPageToken": "token_page_2"
            }
        """.trimIndent()

        val page1 = client.parseModelsPageJson(page1Json)
        assertEquals(1, page1.first.size)
        assertEquals("gemini-2.5-flash", page1.first[0].id)
        assertEquals("token_page_2", page1.second)

        val page2Json = """
            {
              "models": [
                {
                  "name": "models/gemini-1.5-pro",
                  "displayName": "Gemini 1.5 Pro",
                  "supportedGenerationMethods": ["generateContent"]
                }
              ]
            }
        """.trimIndent()

        val page2 = client.parseModelsPageJson(page2Json)
        assertEquals(1, page2.first.size)
        assertEquals("gemini-1.5-pro", page2.first[0].id)
        assertNull(page2.second)
    }

    @Test
    fun `GeminiApiClient handles empty models list JSON gracefully`() {
        val client = GeminiApiClient()
        val emptyJson = """{"models": []}"""
        val parsed = client.parseModelsListJson(emptyJson)
        assertTrue(parsed.isEmpty())
    }

    @Test
    fun `GeminiApiClient handles single compatible model gracefully`() {
        val client = GeminiApiClient()
        val singleModelJson = """
            {
              "models": [
                {
                  "name": "models/gemini-custom-pro",
                  "displayName": "Custom Pro Model",
                  "description": "Single available model",
                  "supportedGenerationMethods": ["generateContent"]
                }
              ]
            }
        """.trimIndent()

        val parsed = client.parseModelsListJson(singleModelJson)
        assertEquals(1, parsed.size)
        assertEquals("gemini-custom-pro", parsed[0].id)
        assertTrue(parsed[0].isRecommended)
    }

    @Test
    fun `GeminiApiClient maps 404 to MODEL_NOT_FOUND and never to REGIONAL_RESTRICTION`() {
        val client = GeminiApiClient()
        val error404Json = """
            {
              "error": {
                "code": 404,
                "message": "models/gemini-1.5-flash is not found for API version v1beta",
                "status": "NOT_FOUND"
              }
            }
        """.trimIndent()

        val result = client.mapHttpError(404, error404Json, "gemini-1.5-flash")
        assertEquals(AiConnectionState.MODEL_NOT_FOUND, result.connectionState)
        assertNotEquals(AiConnectionState.REGIONAL_RESTRICTION, result.connectionState)
        assertTrue(result.message.contains("not found on the Gemini API"))
    }

    @Test
    fun `GeminiApiClient correctly maps explicit regional restrictions`() {
        val client = GeminiApiClient()
        val regionalErrorJson = """
            {
              "error": {
                "code": 400,
                "message": "User location is not supported for the API use.",
                "status": "FAILED_PRECONDITION"
              }
            }
        """.trimIndent()

        val result = client.mapHttpError(400, regionalErrorJson, "gemini-1.5-flash")
        assertEquals(AiConnectionState.REGIONAL_RESTRICTION, result.connectionState)
        assertTrue(result.message.contains("not supported in your current region"))
    }

    @Test
    fun `GeminiApiClient maps API disabled and key restrictions correctly`() {
        val client = GeminiApiClient()

        val disabledApiJson = """
            {
              "error": {
                "code": 403,
                "message": "Generative Language API has not been used in project 123456 before or it is disabled.",
                "status": "PERMISSION_DENIED"
              }
            }
        """.trimIndent()
        val resDisabled = client.mapHttpError(403, disabledApiJson, "gemini-1.5-flash")
        assertEquals(AiConnectionState.API_NOT_ENABLED, resDisabled.connectionState)

        val restrictedKeyJson = """
            {
              "error": {
                "code": 403,
                "message": "The caller does not have permission",
                "status": "PERMISSION_DENIED",
                "details": [{"reason": "IP_RESTRICTED"}]
              }
            }
        """.trimIndent()
        val resRestricted = client.mapHttpError(403, restrictedKeyJson, "gemini-1.5-flash")
        assertEquals(AiConnectionState.API_KEY_RESTRICTED, resRestricted.connectionState)

        val invalidKeyJson = """
            {
              "error": {
                "code": 400,
                "message": "API key not valid. Please pass a valid API key.",
                "status": "INVALID_ARGUMENT",
                "details": [{"reason": "API_KEY_INVALID"}]
              }
            }
        """.trimIndent()
        val resInvalidKey = client.mapHttpError(400, invalidKeyJson, "gemini-1.5-flash")
        assertEquals(AiConnectionState.INVALID_KEY, resInvalidKey.connectionState)

        val quotaExceededJson = """
            {
              "error": {
                "code": 429,
                "message": "Resource has been exhausted (e.g. check quota).",
                "status": "RESOURCE_EXHAUSTED"
              }
            }
        """.trimIndent()
        val resQuota = client.mapHttpError(429, quotaExceededJson, "gemini-1.5-flash")
        assertEquals(AiConnectionState.QUOTA_EXCEEDED, resQuota.connectionState)
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
