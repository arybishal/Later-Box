package com.laterbox.data.ai.network

import com.laterbox.data.ai.model.AiAnalysisOutput
import com.laterbox.data.ai.model.AiConnectionState
import com.laterbox.data.ai.model.GeminiModelInfo
import com.laterbox.data.ai.model.GeminiModels
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
        private const val HEADER_API_KEY = "x-goog-api-key"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val MAX_PAGINATION_PAGES = 10
    }

    /**
     * Dynamically discovers all models accessible to the provided Gemini API key.
     * Supports full pagination via nextPageToken.
     * Filters down to models supporting "generateContent".
     */
    suspend fun fetchAvailableModels(apiKey: String): GeminiApiResult<List<GeminiModelInfo>> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            return@withContext GeminiApiResult.Error(
                AiConnectionState.INVALID_KEY,
                "API key cannot be empty."
            )
        }

        val allModels = mutableListOf<GeminiModelInfo>()
        var nextPageToken: String? = null
        var pageCount = 0

        try {
            do {
                val urlBuilder = StringBuilder(BASE_URL).append("?pageSize=100")
                if (!nextPageToken.isNullOrBlank()) {
                    urlBuilder.append("&pageToken=").append(nextPageToken)
                }

                val request = Request.Builder()
                    .url(urlBuilder.toString())
                    .addHeader(HEADER_API_KEY, cleanKey)
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val bodyString = response.body?.string() ?: ""

                    if (!response.isSuccessful) {
                        return@withContext mapHttpError(response.code, bodyString, "models.list")
                    }

                    val pageResult = parseModelsPageJson(bodyString)
                    allModels.addAll(pageResult.first)
                    nextPageToken = pageResult.second
                }

                pageCount++
            } while (!nextPageToken.isNullOrBlank() && pageCount < MAX_PAGINATION_PAGES)

            if (allModels.isNotEmpty()) {
                // Determine best default/recommended model among all discovered models
                val bestModel = GeminiModels.selectBestDefault(allModels)
                val finalizedList = allModels.map { model ->
                    model.copy(isRecommended = model.id == bestModel.id)
                }.sortedWith(
                    compareByDescending<GeminiModelInfo> { it.isRecommended }
                        .thenByDescending { it.id.contains("flash", ignoreCase = true) }
                        .thenByDescending { it.id.contains("pro", ignoreCase = true) }
                        .thenBy { it.displayName }
                )
                GeminiApiResult.Success(finalizedList)
            } else {
                GeminiApiResult.Success(GeminiModels.FALLBACK_MODELS)
            }
        } catch (e: SocketTimeoutException) {
            GeminiApiResult.Error(
                AiConnectionState.TIMEOUT,
                "Connection timed out while fetching Gemini models. Check your internet connectivity."
            )
        } catch (e: IOException) {
            GeminiApiResult.Error(
                AiConnectionState.NETWORK_ERROR,
                "Network connection failed while fetching Gemini models. Verify internet access."
            )
        } catch (e: Exception) {
            GeminiApiResult.Error(
                AiConnectionState.UNKNOWN_ERROR,
                "Failed to fetch Gemini models: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    /**
     * Executes a 6-step diagnostic test against the Gemini Developer API:
     * 1. Validate API key presence/format.
     * 2. Call models.list to discover accessible models.
     * 3. Verify selected model exists in project.
     * 4. Verify selected model supports 'generateContent'.
     * 5. Send minimal test prompt ('Respond with OK') using header authentication.
     * 6. Validate candidate output response.
     */
    suspend fun testConnection(
        apiKey: String,
        modelId: String
    ): GeminiApiResult<String> = withContext(Dispatchers.IO) {
        // Step 1: Validate key format
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            return@withContext GeminiApiResult.Error(
                AiConnectionState.INVALID_KEY,
                "API key cannot be empty."
            )
        }

        val normalizedModelId = GeminiModelInfo.normalizeModelId(modelId)

        // Step 2: Query models.list
        val modelsResult = fetchAvailableModels(cleanKey)
        if (modelsResult is GeminiApiResult.Error) {
            return@withContext modelsResult
        }

        val availableModels = (modelsResult as GeminiApiResult.Success).data

        // Step 3: Verify model exists in returned list
        val targetModel = availableModels.firstOrNull { it.id.equals(normalizedModelId, ignoreCase = true) }
        if (targetModel == null && availableModels.isNotEmpty()) {
            val availableNames = availableModels.take(4).joinToString(", ") { it.id }
            return@withContext GeminiApiResult.Error(
                AiConnectionState.MODEL_NOT_FOUND,
                "Model '$normalizedModelId' is not accessible on your Google Cloud/AI Studio project. Accessible models include: $availableNames."
            )
        }

        // Step 4: Verify generateContent is supported
        if (targetModel != null && targetModel.supportedGenerationMethods.isNotEmpty() &&
            !targetModel.supportedGenerationMethods.contains("generateContent")
        ) {
            return@withContext GeminiApiResult.Error(
                AiConnectionState.MODEL_UNSUPPORTED,
                "Model '$normalizedModelId' does not support content generation ('generateContent')."
            )
        }

        // Step 5: Send minimal real ping with header authentication
        val url = "$BASE_URL/$normalizedModelId:generateContent"
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
            .addHeader(HEADER_API_KEY, cleanKey)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    // Step 6: Validate usable text response
                    val candidateText = extractCandidateText(bodyString)
                    if (candidateText != null) {
                        GeminiApiResult.Success("Connected successfully to Google Gemini ($normalizedModelId). All 6 diagnostic steps passed.")
                    } else {
                        GeminiApiResult.Success("Connected to Google Gemini ($normalizedModelId). API returned 200 OK.")
                    }
                } else {
                    mapHttpError(response.code, bodyString, normalizedModelId)
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
     * Sends a text prompt to Gemini and returns candidate text response.
     */
    suspend fun generateContentText(
        apiKey: String,
        modelId: String,
        prompt: String
    ): GeminiApiResult<String> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            return@withContext GeminiApiResult.Error(
                AiConnectionState.INVALID_KEY,
                "API key is missing or not configured."
            )
        }

        val normalizedModelId = GeminiModelInfo.normalizeModelId(modelId)
        val url = "$BASE_URL/$normalizedModelId:generateContent"

        val partsArray = JSONArray().apply {
            put(JSONObject().apply { put("text", prompt) })
        }

        val contentsArray = JSONArray().apply {
            put(JSONObject().apply { put("parts", partsArray) })
        }

        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 1024)
            })
        }

        val requestBody = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .addHeader(HEADER_API_KEY, cleanKey)
            .post(requestBody)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext mapHttpError(response.code, bodyString, normalizedModelId)
                }
                val text = extractCandidateText(bodyString)
                if (!text.isNullOrBlank()) {
                    GeminiApiResult.Success(text.trim())
                } else {
                    GeminiApiResult.Error(
                        AiConnectionState.SERVER_ERROR,
                        "Received empty response from Gemini."
                    )
                }
            }
        } catch (e: SocketTimeoutException) {
            GeminiApiResult.Error(AiConnectionState.TIMEOUT, "Request timed out. Please try again.")
        } catch (e: IOException) {
            GeminiApiResult.Error(AiConnectionState.NETWORK_ERROR, "Network connection error: ${e.message}")
        } catch (e: Exception) {
            GeminiApiResult.Error(AiConnectionState.UNKNOWN_ERROR, "Error: ${e.message}")
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

        val normalizedModelId = GeminiModelInfo.normalizeModelId(modelId)
        val url = "$BASE_URL/$normalizedModelId:generateContent"

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
            .addHeader(HEADER_API_KEY, cleanKey)
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
                    mapHttpError(response.code, bodyString, normalizedModelId)
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

    /**
     * Parses a single page response from models.list endpoint.
     * Returns a pair of (List of models supporting generateContent, nextPageToken).
     */
    fun parseModelsPageJson(responseBody: String): Pair<List<GeminiModelInfo>, String?> {
        val list = mutableListOf<GeminiModelInfo>()
        var nextToken: String? = null

        try {
            val root = JSONObject(responseBody)
            nextToken = root.optString("nextPageToken").takeIf { it.isNotBlank() }

            val modelsArray = root.optJSONArray("models") ?: return Pair(emptyList(), null)

            for (i in 0 until modelsArray.length()) {
                val modelObj = modelsArray.getJSONObject(i)
                val rawName = modelObj.optString("name", "")
                val cleanId = GeminiModelInfo.normalizeModelId(rawName)
                if (cleanId.isBlank()) continue

                val supportedMethods = modelObj.optJSONArray("supportedGenerationMethods")?.toStringList() ?: emptyList()

                // Filter to models that support generating content
                if (supportedMethods.isNotEmpty() && !supportedMethods.contains("generateContent")) {
                    continue
                }

                val displayName = modelObj.optString("displayName", cleanId).ifBlank { cleanId }
                val description = modelObj.optString("description", "")
                val inputTokenLimit = modelObj.optInt("inputTokenLimit", 0)
                val outputTokenLimit = modelObj.optInt("outputTokenLimit", 0)
                val inModalities = modelObj.optJSONArray("supportedInputModalities")?.toStringList() ?: emptyList()
                val outModalities = modelObj.optJSONArray("supportedOutputModalities")?.toStringList() ?: emptyList()

                val modelInfo = GeminiModelInfo(
                    id = cleanId,
                    displayName = displayName,
                    description = description,
                    isRecommended = false,
                    supportsMultimodal = true,
                    supportedGenerationMethods = supportedMethods,
                    inputTokenLimit = inputTokenLimit,
                    outputTokenLimit = outputTokenLimit,
                    supportedInputModalities = inModalities,
                    supportedOutputModalities = outModalities,
                    isDynamicallyDiscovered = true
                )
                list.add(modelInfo)
            }
        } catch (_: Exception) {
            return Pair(emptyList(), null)
        }

        return Pair(list, nextToken)
    }

    fun parseModelsListJson(responseBody: String): List<GeminiModelInfo> {
        val page = parseModelsPageJson(responseBody)
        val list = page.first
        if (list.isEmpty()) return emptyList()

        val best = GeminiModels.selectBestDefault(list)
        return list.map { model ->
            model.copy(isRecommended = model.id == best.id)
        }.sortedWith(
            compareByDescending<GeminiModelInfo> { it.isRecommended }
                .thenByDescending { it.id.contains("flash", ignoreCase = true) }
                .thenByDescending { it.id.contains("pro", ignoreCase = true) }
                .thenBy { it.displayName }
        )
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

    /**
     * Accurate HTTP error mapping parsing Google's JSON error response.
     * Never falsely reports 404 as a regional restriction.
     */
    fun mapHttpError(code: Int, bodyString: String, modelId: String): GeminiApiResult.Error {
        var extractedMessage = ""
        var extractedStatus = ""
        var extractedReason = ""

        try {
            val root = JSONObject(bodyString)
            val err = root.optJSONObject("error")
            if (err != null) {
                extractedMessage = err.optString("message", "")
                extractedStatus = err.optString("status", "")
                val details = err.optJSONArray("details")
                if (details != null && details.length() > 0) {
                    val firstDetail = details.getJSONObject(0)
                    extractedReason = firstDetail.optString("reason", "")
                }
            }
        } catch (_: Exception) {
            // body was not JSON
        }

        val combinedText = "$extractedMessage $extractedStatus $extractedReason".lowercase()

        // 1. Check for explicit regional restrictions
        if (combinedText.contains("location is not supported") ||
            combinedText.contains("user location is not supported") ||
            combinedText.contains("geographical region") ||
            combinedText.contains("not available in your country")
        ) {
            return GeminiApiResult.Error(
                AiConnectionState.REGIONAL_RESTRICTION,
                "Google Gemini Developer API is not supported in your current region: $extractedMessage"
            )
        }

        // 2. Check for invalid API key
        if (combinedText.contains("api_key_invalid") ||
            combinedText.contains("key not valid") ||
            combinedText.contains("api key not valid")
        ) {
            return GeminiApiResult.Error(
                AiConnectionState.INVALID_KEY,
                "Invalid Gemini API key. Please check your key in Google AI Studio."
            )
        }

        // 3. Check for API not enabled
        if (combinedText.contains("generative language api has not been used") ||
            combinedText.contains("service_disabled") ||
            (combinedText.contains("api") && combinedText.contains("not enabled"))
        ) {
            return GeminiApiResult.Error(
                AiConnectionState.API_NOT_ENABLED,
                "Generative Language API is disabled. Please enable it in your Google Cloud Console."
            )
        }

        // 4. Check for key restrictions or authorization issues
        if (combinedText.contains("ip_restricted") ||
            combinedText.contains("android_package_restricted") ||
            combinedText.contains("caller does not have permission") ||
            combinedText.contains("permission_denied") ||
            code == 403
        ) {
            return GeminiApiResult.Error(
                AiConnectionState.API_KEY_RESTRICTED,
                "API key restricted or unauthorized: ${extractedMessage.ifBlank { "Permission denied (403)" }}"
            )
        }

        // 5. Check for quota or rate limit issues
        if (code == 429 || combinedText.contains("resource_exhausted") || combinedText.contains("quota")) {
            return if (combinedText.contains("rate limit") || combinedText.contains("per minute")) {
                GeminiApiResult.Error(
                    AiConnectionState.RATE_LIMIT,
                    "Gemini API rate limit reached. Please wait a moment before trying again."
                )
            } else {
                GeminiApiResult.Error(
                    AiConnectionState.QUOTA_EXCEEDED,
                    "Gemini API quota exceeded: ${extractedMessage.ifBlank { "Please check your Google AI Studio plan." }}"
                )
            }
        }

        // 6. Check for billing restrictions
        if (combinedText.contains("billing") || combinedText.contains("billable")) {
            return GeminiApiResult.Error(
                AiConnectionState.BILLING_RESTRICTION,
                "Google Cloud billing requirement: ${extractedMessage.ifBlank { "A billing account may be required for this model." }}"
            )
        }

        // 7. Check for 404 (Model Not Found)
        if (code == 404) {
            val cleanModel = GeminiModelInfo.normalizeModelId(modelId)
            return GeminiApiResult.Error(
                AiConnectionState.MODEL_NOT_FOUND,
                "Model '$cleanModel' was not found on the Gemini API: ${extractedMessage.ifBlank { "Resource not found (404). Verify model availability." }}"
            )
        }

        // 8. Server errors
        if (code in 500..599) {
            return GeminiApiResult.Error(
                AiConnectionState.SERVER_ERROR,
                "Google Gemini service is temporarily unavailable ($code). Please retry later."
            )
        }

        // 9. Bad Request
        if (code == 400) {
            return GeminiApiResult.Error(
                AiConnectionState.UNKNOWN_ERROR,
                "Bad Request (400): ${extractedMessage.ifBlank { "Invalid request structure." }}"
            )
        }

        return GeminiApiResult.Error(
            AiConnectionState.UNKNOWN_ERROR,
            "Google API returned code $code: ${extractedMessage.ifBlank { "Unknown error" }}"
        )
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
