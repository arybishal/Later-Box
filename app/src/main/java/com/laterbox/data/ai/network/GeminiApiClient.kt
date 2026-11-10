package com.laterbox.data.ai.network

import com.laterbox.data.ai.model.AiAnalysisOutput
import com.laterbox.data.ai.model.AiConnectionState
import com.laterbox.data.ai.model.LocationInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

sealed class GeminiApiResult<out T> {
    data class Success<T>(val data: T) : GeminiApiResult<T>()
    data class Error(val connectionState: AiConnectionState, val message: String) : GeminiApiResult<Nothing>()
}

class GeminiApiClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * Executes a lightweight test request against the Gemini API to verify API key validity,
     * model availability, and account quota without analyzing any user library items.
     */
    suspend fun testConnection(
        apiKey: String,
        modelId: String
    ): GeminiApiResult<String> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            return@withContext GeminiApiResult.Error(
                AiConnectionState.INVALID_KEY,
                "API key cannot be empty."
            )
        }

        val url = "$BASE_URL/$modelId:generateContent?key=$cleanKey"
        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply { put("text", "Respond with 'OK'") })
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val candidateText = extractCandidateText(bodyString)
                    if (candidateText != null) {
                        GeminiApiResult.Success("Connected successfully to Google Gemini ($modelId).")
                    } else {
                        GeminiApiResult.Success("API responded successfully.")
                    }
                } else {
                    mapHttpError(response.code, bodyString, modelId)
                }
            }
        } catch (e: SocketTimeoutException) {
            GeminiApiResult.Error(
                AiConnectionState.TIMEOUT,
                "Connection timed out. Please check your internet connectivity."
            )
        } catch (e: IOException) {
            GeminiApiResult.Error(
                AiConnectionState.NETWORK_ERROR,
                "Network connection failed. Verify internet access."
            )
        } catch (e: Exception) {
            GeminiApiResult.Error(
                AiConnectionState.UNKNOWN_ERROR,
                "Unexpected error during connection test: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    /**
     * Sends content analysis prompt to Gemini and returns structured JSON output.
     */
    suspend fun generateAnalysis(
        apiKey: String,
        modelId: String,
        prompt: String,
        imageBase64: String? = null,
        imageMimeType: String? = null
    ): GeminiApiResult<AiAnalysisOutput> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            return@withContext GeminiApiResult.Error(
                AiConnectionState.INVALID_KEY,
                "API key is missing or not configured."
            )
        }

        val url = "$BASE_URL/$modelId:generateContent?key=$cleanKey"

        // Build parts array: text prompt + optional image part
        val partsArray = JSONArray().apply {
            put(JSONObject().apply { put("text", prompt) })

            if (!imageBase64.isNullOrBlank() && !imageMimeType.isNullOrBlank()) {
                val inlineDataObj = JSONObject().apply {
                    put("mimeType", imageMimeType)
                    put("data", imageBase64)
                }
                put(JSONObject().apply { put("inlineData", inlineDataObj) })
            }
        }

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            // Request structured JSON response
            val generationConfig = JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            }
            put("generationConfig", generationConfig)
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val rawText = extractCandidateText(bodyString)
                    if (rawText.isNullOrBlank()) {
                        return@withContext GeminiApiResult.Error(
                            AiConnectionState.SERVER_ERROR,
                            "Gemini returned an empty response."
                        )
                    }

                    val parsedOutput = parseStructuredJson(rawText)
                    GeminiApiResult.Success(parsedOutput)
                } else {
                    mapHttpError(response.code, bodyString, modelId)
                }
            }
        } catch (e: SocketTimeoutException) {
            GeminiApiResult.Error(
                AiConnectionState.TIMEOUT,
                "Analysis request timed out. Please retry."
            )
        } catch (e: IOException) {
            GeminiApiResult.Error(
                AiConnectionState.NETWORK_ERROR,
                "Network connection lost during analysis."
            )
        } catch (e: Exception) {
            GeminiApiResult.Error(
                AiConnectionState.UNKNOWN_ERROR,
                "Analysis failed: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    private fun extractCandidateText(responseBody: String): String? {
        return try {
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val firstPart = parts.getJSONObject(0)
            if (firstPart.has("text")) firstPart.getString("text") else null
        } catch (_: Exception) {
            null
        }
    }

    private fun mapHttpError(code: Int, bodyString: String, modelId: String): GeminiApiResult.Error {
        val extractedMessage = try {
            val root = JSONObject(bodyString)
            val err = root.optJSONObject("error")
            err?.optString("message") ?: ""
        } catch (_: Exception) {
            ""
        }

        return when (code) {
            400 -> {
                if (extractedMessage.contains("API_KEY_INVALID", ignoreCase = true) ||
                    extractedMessage.contains("key not valid", ignoreCase = true)
                ) {
                    GeminiApiResult.Error(AiConnectionState.INVALID_KEY, "Invalid Gemini API key. Please check your key in Settings.")
                } else {
                    GeminiApiResult.Error(AiConnectionState.INVALID_KEY, "Bad Request: $extractedMessage")
                }
            }
            403 -> GeminiApiResult.Error(AiConnectionState.INVALID_KEY, "Permission denied. Check that the Generative Language API is enabled on your Google Cloud Project.")
            404 -> GeminiApiResult.Error(AiConnectionState.MODEL_UNAVAILABLE, "Model '$modelId' is unavailable or not supported in your region.")
            429 -> GeminiApiResult.Error(AiConnectionState.QUOTA_EXCEEDED, "Gemini API quota exceeded or rate limit reached. Please wait or check your Google AI Studio plan.")
            500, 503 -> GeminiApiResult.Error(AiConnectionState.SERVER_ERROR, "Google Gemini service is temporarily unavailable. Please retry later.")
            else -> GeminiApiResult.Error(AiConnectionState.UNKNOWN_ERROR, "Google API returned code $code: ${extractedMessage.ifEmpty { "Unknown error" }}")
        }
    }

    fun parseStructuredJson(rawResponseText: String): AiAnalysisOutput {
        // Strip code fence if Gemini wraps it in ```json ... ```
        val cleaned = rawResponseText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val json = JSONObject(cleaned)

            val shortDescription = json.optString("shortDescription", "").trim()
            val whatItsAbout = json.optString("whatItsAbout", "").trim()
            val category = json.optString("category", "General").trim()

            val locationObj = json.optJSONObject("location")
            val location = if (locationObj != null) {
                LocationInfo(
                    name = locationObj.optString("name").takeIf { it.isNotBlank() && it != "null" },
                    address = locationObj.optString("address").takeIf { it.isNotBlank() && it != "null" },
                    confidence = locationObj.optString("confidence", "unknown")
                )
            } else {
                LocationInfo()
            }

            val peopleAndEntities = json.optJSONArray("peopleAndEntities")?.toStringList() ?: emptyList()
            val keyDetails = json.optJSONArray("keyDetails")?.toStringList() ?: emptyList()
            val keyTakeaways = json.optJSONArray("keyTakeaways")?.toStringList() ?: emptyList()
            val contentType = json.optString("contentType", "article")
            val language = json.optString("language", "English")

            AiAnalysisOutput(
                shortDescription = shortDescription.ifBlank { whatItsAbout.ifBlank { "Content analyzed." } },
                whatItsAbout = whatItsAbout.ifBlank { shortDescription },
                category = category.ifBlank { "General" },
                location = location,
                peopleAndEntities = peopleAndEntities,
                keyDetails = keyDetails,
                keyTakeaways = keyTakeaways,
                contentType = contentType,
                language = language
            )
        } catch (_: Exception) {
            // Fallback for unexpected text without crashing
            AiAnalysisOutput(
                shortDescription = cleaned.take(200),
                whatItsAbout = cleaned.take(400),
                category = "General"
            )
        }
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
