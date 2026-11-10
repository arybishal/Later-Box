package com.laterbox.data.ai.model

import org.json.JSONArray
import org.json.JSONObject

data class GeminiModelInfo(
    val id: String, // Normalized ID without "models/" prefix, e.g. "gemini-1.5-flash"
    val displayName: String,
    val description: String,
    val isRecommended: Boolean = false,
    val supportsMultimodal: Boolean = true,
    val supportedGenerationMethods: List<String> = emptyList(),
    val inputTokenLimit: Int = 0,
    val outputTokenLimit: Int = 0,
    val supportedInputModalities: List<String> = emptyList(),
    val supportedOutputModalities: List<String> = emptyList(),
    val isDynamicallyDiscovered: Boolean = false
) {
    val family: String
        get() = when {
            id.contains("flash", ignoreCase = true) -> "Flash"
            id.contains("pro", ignoreCase = true) -> "Pro"
            id.contains("ultra", ignoreCase = true) -> "Ultra"
            id.contains("exp", ignoreCase = true) || id.contains("preview", ignoreCase = true) -> "Experimental"
            else -> "Other"
        }

    fun formatTokenLimits(): String {
        if (inputTokenLimit <= 0 && outputTokenLimit <= 0) return ""
        val inStr = if (inputTokenLimit >= 1_000_000) "${inputTokenLimit / 1_000_000}M" else if (inputTokenLimit >= 1000) "${inputTokenLimit / 1000}K" else "$inputTokenLimit"
        val outStr = if (outputTokenLimit >= 1000) "${outputTokenLimit / 1000}K" else "$outputTokenLimit"
        return "$inStr in · $outStr out"
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("displayName", displayName)
            put("description", description)
            put("isRecommended", isRecommended)
            put("supportsMultimodal", supportsMultimodal)
            put("supportedGenerationMethods", JSONArray(supportedGenerationMethods))
            put("inputTokenLimit", inputTokenLimit)
            put("outputTokenLimit", outputTokenLimit)
            put("supportedInputModalities", JSONArray(supportedInputModalities))
            put("supportedOutputModalities", JSONArray(supportedOutputModalities))
            put("isDynamicallyDiscovered", isDynamicallyDiscovered)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): GeminiModelInfo {
            val methods = json.optJSONArray("supportedGenerationMethods")?.toStringList() ?: emptyList()
            val inModalities = json.optJSONArray("supportedInputModalities")?.toStringList() ?: emptyList()
            val outModalities = json.optJSONArray("supportedOutputModalities")?.toStringList() ?: emptyList()

            return GeminiModelInfo(
                id = normalizeModelId(json.optString("id", GeminiModels.DEFAULT_MODEL_ID)),
                displayName = json.optString("displayName", "Gemini Model"),
                description = json.optString("description", ""),
                isRecommended = json.optBoolean("isRecommended", false),
                supportsMultimodal = json.optBoolean("supportsMultimodal", true),
                supportedGenerationMethods = methods,
                inputTokenLimit = json.optInt("inputTokenLimit", 0),
                outputTokenLimit = json.optInt("outputTokenLimit", 0),
                supportedInputModalities = inModalities,
                supportedOutputModalities = outModalities,
                isDynamicallyDiscovered = json.optBoolean("isDynamicallyDiscovered", false)
            )
        }

        fun normalizeModelId(id: String): String {
            return id.trim().removePrefix("models/")
        }

        fun toModelPath(id: String): String {
            val clean = id.trim().removePrefix("models/")
            return "models/$clean"
        }

        private fun JSONArray.toStringList(): List<String> {
            val list = mutableListOf<String>()
            for (i in 0 until length()) {
                val item = optString(i, "").trim()
                if (item.isNotEmpty() && item != "null") {
                    list.add(item)
                }
            }
            return list
        }
    }
}

object GeminiModels {
    const val DEFAULT_MODEL_ID = "gemini-1.5-flash"

    val FLASH_1_5 = GeminiModelInfo(
        id = "gemini-1.5-flash",
        displayName = "Gemini 1.5 Flash",
        description = "Fast, efficient multimodal intelligence. Best balance of speed, accuracy, and quota.",
        isRecommended = true,
        supportsMultimodal = true,
        supportedGenerationMethods = listOf("generateContent", "countTokens"),
        inputTokenLimit = 1048576,
        outputTokenLimit = 8192,
        isDynamicallyDiscovered = false
    )

    val FLASH_2_0 = GeminiModelInfo(
        id = "gemini-2.0-flash",
        displayName = "Gemini 2.0 Flash",
        description = "Next-generation high-speed multimodal intelligence for real-time analysis.",
        isRecommended = false,
        supportsMultimodal = true,
        supportedGenerationMethods = listOf("generateContent", "countTokens"),
        inputTokenLimit = 1048576,
        outputTokenLimit = 8192,
        isDynamicallyDiscovered = false
    )

    val PRO_1_5 = GeminiModelInfo(
        id = "gemini-1.5-pro",
        displayName = "Gemini 1.5 Pro",
        description = "Deep reasoning and extensive context for complex documents and articles.",
        isRecommended = false,
        supportsMultimodal = true,
        supportedGenerationMethods = listOf("generateContent", "countTokens"),
        inputTokenLimit = 2097152,
        outputTokenLimit = 8192,
        isDynamicallyDiscovered = false
    )

    val FALLBACK_MODELS = listOf(FLASH_1_5, FLASH_2_0, PRO_1_5)

    fun findModel(id: String, availableModels: List<GeminiModelInfo> = FALLBACK_MODELS): GeminiModelInfo {
        val cleanId = GeminiModelInfo.normalizeModelId(id)
        return availableModels.firstOrNull { it.id.equals(cleanId, ignoreCase = true) }
            ?: FALLBACK_MODELS.firstOrNull { it.id.equals(cleanId, ignoreCase = true) }
            ?: selectBestDefault(availableModels)
    }

    fun selectBestDefault(models: List<GeminiModelInfo>): GeminiModelInfo {
        if (models.isEmpty()) return FLASH_1_5

        // Prioritize general-purpose Flash models (avoid specialized ones like image or tts first)
        val flashCandidates = models.filter {
            it.id.contains("flash", ignoreCase = true) &&
            !it.id.contains("image", ignoreCase = true) &&
            !it.id.contains("tts", ignoreCase = true)
        }

        // 1. Try gemini-2.5-flash or gemini-2.0-flash or gemini-1.5-flash
        flashCandidates.firstOrNull { it.id == "gemini-2.5-flash" }?.let { return it }
        flashCandidates.firstOrNull { it.id == "gemini-1.5-flash" }?.let { return it }
        flashCandidates.firstOrNull { it.id == "gemini-2.0-flash" }?.let { return it }
        if (flashCandidates.isNotEmpty()) return flashCandidates.first()

        // 2. Try any model marked isRecommended
        models.firstOrNull { it.isRecommended }?.let { return it }

        // 3. Try any flash model
        models.firstOrNull { it.id.contains("flash", ignoreCase = true) }?.let { return it }

        // 4. Try any pro model
        models.firstOrNull { it.id.contains("pro", ignoreCase = true) }?.let { return it }

        // 5. Fallback to first available model
        return models.first()
    }
}

data class LocationInfo(
    val name: String? = null,
    val address: String? = null,
    val confidence: String = "unknown"
)

/**
 * Structured output received from Gemini API.
 */
data class AiAnalysisOutput(
    val shortDescription: String,
    val whatItsAbout: String,
    val category: String,
    val location: LocationInfo = LocationInfo(),
    val peopleAndEntities: List<String> = emptyList(),
    val keyDetails: List<String> = emptyList(),
    val keyTakeaways: List<String> = emptyList(),
    val contentType: String = "link",
    val language: String = "English"
)

enum class AiConnectionState {
    IDLE,
    TESTING,
    SUCCESS,
    INVALID_KEY,
    API_KEY_RESTRICTED,
    API_NOT_ENABLED,
    MODEL_NOT_FOUND,
    MODEL_UNSUPPORTED,
    MODEL_UNAVAILABLE,
    REGIONAL_RESTRICTION,
    QUOTA_EXCEEDED,
    BILLING_RESTRICTION,
    RATE_LIMIT,
    NETWORK_ERROR,
    SERVER_ERROR,
    TIMEOUT,
    UNKNOWN_ERROR
}
