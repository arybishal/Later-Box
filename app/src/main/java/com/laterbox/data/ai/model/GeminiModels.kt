package com.laterbox.data.ai.model

data class GeminiModelInfo(
    val id: String,
    val displayName: String,
    val description: String,
    val isRecommended: Boolean = false,
    val supportsMultimodal: Boolean = true
)

object GeminiModels {
    val FLASH_1_5 = GeminiModelInfo(
        id = "gemini-1.5-flash",
        displayName = "Gemini 1.5 Flash",
        description = "Fast, efficient multimodal intelligence. Best balance of speed, accuracy, and quota.",
        isRecommended = true,
        supportsMultimodal = true
    )

    val FLASH_2_0 = GeminiModelInfo(
        id = "gemini-2.0-flash",
        displayName = "Gemini 2.0 Flash",
        description = "Next-generation high-speed multimodal intelligence for real-time analysis.",
        isRecommended = false,
        supportsMultimodal = true
    )

    val PRO_1_5 = GeminiModelInfo(
        id = "gemini-1.5-pro",
        displayName = "Gemini 1.5 Pro",
        description = "Deep reasoning and extensive context for complex documents and articles.",
        isRecommended = false,
        supportsMultimodal = true
    )

    val ALL_MODELS = listOf(FLASH_1_5, FLASH_2_0, PRO_1_5)
    const val DEFAULT_MODEL_ID = "gemini-1.5-flash"

    fun findModel(id: String): GeminiModelInfo {
        return ALL_MODELS.firstOrNull { it.id == id } ?: FLASH_1_5
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
    QUOTA_EXCEEDED,
    MODEL_UNAVAILABLE,
    NETWORK_ERROR,
    SERVER_ERROR,
    TIMEOUT,
    UNKNOWN_ERROR
}
