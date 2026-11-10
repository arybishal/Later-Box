package com.laterbox.data.ai.prompt

import com.laterbox.data.local.entity.AiAnalysisEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import java.security.MessageDigest

object GeminiPromptBuilder {

    /**
     * Builds a structured, content-type-aware prompt instructing Gemini to analyze
     * the item and return strictly valid JSON matching the schema.
     */
    fun buildAnalysisPrompt(
        item: SavedItemEntity,
        extraContextText: String? = null
    ): String {
        val typeInstruction = when (item.contentType) {
            ContentType.VIDEO -> "This is a saved video from ${item.sourceApp ?: item.sourceDomain ?: "the web"}. Analyze the title, source, and description to summarize what the video discusses or showcases."
            ContentType.IMAGE -> "This is a saved visual image. Describe what is depicted in the image, its key visual details, subjects, and any readable text."
            ContentType.ARTICLE -> "This is an article or editorial piece from ${item.sourceDomain ?: "the web"}. Summarize its core arguments, key takeaways, and relevant context."
            ContentType.DOCUMENT -> "This is a saved document or file (${item.mimeType ?: "document"}). Extract and summarize the primary subject, structure, and essential points."
            ContentType.NOTE -> "This is a personal saved note. Provide a structured summary of its contents, core takeaways, and any actionable details."
            ContentType.LINK -> "This is a saved web page from ${item.sourceDomain ?: "the web"}. Summarize the page's purpose, topic, and key information."
            ContentType.AUDIO -> "This is a saved audio item or podcast. Summarize the title, host/source, and topics."
        }

        return """
            You are Later Box AI, a personal content organization assistant.
            $typeInstruction

            ITEM METADATA:
            - Title: ${item.title}
            - Source Domain / App: ${item.sourceDomain ?: item.sourceApp ?: "Unknown"}
            - Original URL: ${item.url}
            ${item.description?.let { "- Source Description: $it" } ?: ""}
            ${item.notes?.let { "- User Personal Note: $it" } ?: ""}
            ${extraContextText?.let { "- Page Excerpt / Text: ${it.take(2000)}" } ?: ""}

            RESPONSE REQUIREMENTS:
            Return ONLY a valid JSON object matching the following structure:
            {
              "shortDescription": "1-2 sentence concise summary of the content.",
              "whatItsAbout": "A clear overview of the main subject and purpose.",
              "category": "Primary category (e.g., Tech & Coding, Cooking, Design, Travel, News, Entertainment, Finance, Health, Productivity, etc.)",
              "location": {
                "name": "Specific place, venue, city or null if none",
                "address": "Street address or city/state/country or null if none",
                "confidence": "high | medium | low | unknown"
              },
              "peopleAndEntities": ["List of notable people, companies, brands, or products mentioned"],
              "keyDetails": ["List of 2-4 specific interesting facts, steps, ingredients, or specifications"],
              "keyTakeaways": ["List of 2-4 core insights, learnings, or recommendations"],
              "contentType": "${item.contentType.displayName.lowercase()}",
              "language": "English"
            }

            CRITICAL ACCURACY RULES:
            - Do not invent facts, locations, or people not supported by the content.
            - If location is not identifiable, return null for name and address, and "unknown" for confidence.
            - Do not include markdown code block formatting or extra commentary outside the JSON object.
        """.trimIndent()
    }

    /**
     * Builds a rich, item-isolated prompt for context-aware questions asked about
     * a specific saved item in ItemDetailScreen.
     */
    fun buildItemQuestionPrompt(
        item: SavedItemEntity,
        aiAnalysis: AiAnalysisEntity?,
        question: String
    ): String {
        val analysisContext = if (aiAnalysis != null) {
            """
            EXISTING AI ANALYSIS FOR THIS ITEM:
            - AI Summary: ${aiAnalysis.shortDescription ?: "None"}
            - What It's About: ${aiAnalysis.whatItsAbout ?: "None"}
            - Key Takeaways: ${aiAnalysis.getKeyTakeaways().joinToString("; ")}
            - Key Details: ${aiAnalysis.getKeyDetails().joinToString("; ")}
            - Category: ${aiAnalysis.category ?: "Uncategorized"}
            - Entities & People: ${aiAnalysis.getPeopleAndEntities().joinToString(", ")}
            """.trimIndent()
        } else {
            "No prior AI analysis has been generated for this item."
        }

        return """
            You are the Later Box Personal Content Assistant inside the Android app Later Box.
            The user is currently viewing a specific saved item in their library (Item ID: ${item.id}).
            They have asked a question specifically about this saved item.

            CURRENT SAVED ITEM CONTEXT:
            - Item ID: ${item.id}
            - Title: "${item.title}"
            - Content Type: ${item.contentType.displayName}
            - Source / Domain: ${item.sourceDomain ?: item.sourceApp ?: "Unknown"}
            - Original URL: ${item.url}
            ${item.description?.let { "- Saved Description / Page Excerpt: $it" } ?: "- Saved Description: Not provided"}
            ${item.notes?.let { "- User's Personal Note: $it" } ?: "- User's Personal Note: None"}

            $analysisContext

            USER'S QUESTION ABOUT THIS ITEM:
            "$question"

            CRITICAL ACCURACY GUIDELINES:
            1. Focus your answer directly on the specific saved item described above.
            2. Do NOT mention or blend information from other unrelated items.
            3. If the answer cannot be determined from the available metadata, description, notes, or analysis, politely explain that the information is unavailable in this item's saved details rather than inventing facts.
            4. Keep your answer clear, concise, well-structured, and helpful. Avoid cluttered markdown formatting.
        """.trimIndent()
    }

    /**
     * Computes a content fingerprint to avoid re-analyzing identical content unnecessarily.
     */
    fun computeContentFingerprint(item: SavedItemEntity): String {
        val payload = "${item.url}|${item.title}|${item.description ?: ""}|${item.notes ?: ""}|${item.contentType.name}"
        val bytes = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
