package com.laterbox.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.laterbox.data.ai.model.AiAnalysisOutput
import com.laterbox.data.ai.model.GeminiModels
import com.laterbox.data.ai.network.GeminiApiClient
import com.laterbox.data.ai.network.GeminiApiResult
import com.laterbox.data.ai.prompt.GeminiPromptBuilder
import com.laterbox.data.ai.security.SecureApiKeyStorage
import com.laterbox.data.local.dao.AiAnalysisDao
import com.laterbox.data.local.dao.SavedItemDao
import com.laterbox.data.local.entity.AiAnalysisEntity
import com.laterbox.data.model.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class GeminiAnalysisService(
    private val context: Context,
    private val savedItemDao: SavedItemDao,
    private val aiAnalysisDao: AiAnalysisDao,
    private val aiSettingsRepository: AiSettingsRepository,
    private val secureApiKeyStorage: SecureApiKeyStorage = SecureApiKeyStorage.getInstance(context),
    private val apiClient: GeminiApiClient = GeminiApiClient()
) {

    suspend fun analyzeItem(
        itemId: Long,
        forceRegenerate: Boolean = false
    ): Result<AiAnalysisEntity> = withContext(Dispatchers.IO) {
        // 1. Verify Global AI is enabled
        if (!aiSettingsRepository.isAiEnabledNow()) {
            return@withContext Result.failure(IllegalStateException("Gemini AI is disabled. Enable it in Settings to analyze items."))
        }

        // 2. Retrieve secure API Key
        val apiKey = secureApiKeyStorage.getApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured. Add your API key in Settings."))
        }

        // 3. Retrieve saved item from database
        val item = savedItemDao.getItemById(itemId)
            ?: return@withContext Result.failure(IllegalArgumentException("Saved item with ID $itemId was not found."))

        val modelId = aiSettingsRepository.getSelectedModelIdNow().ifBlank { GeminiModels.DEFAULT_MODEL_ID }
        val fingerprint = GeminiPromptBuilder.computeContentFingerprint(item)

        // 4. Cache check: if not forcing regeneration, return existing success record if content hasn't changed
        val existing = aiAnalysisDao.getAnalysisSnapshot(itemId)
        if (!forceRegenerate && existing != null && existing.status == "SUCCESS" && existing.contentFingerprint == fingerprint) {
            return@withContext Result.success(existing)
        }

        // 5. Set status to PENDING
        val pendingEntity = existing?.copy(
            status = "PENDING",
            modelUsed = modelId,
            analyzedAt = System.currentTimeMillis(),
            errorMessage = null
        ) ?: AiAnalysisEntity(
            itemId = itemId,
            shortDescription = null,
            whatItsAbout = null,
            category = null,
            modelUsed = modelId,
            status = "PENDING",
            contentFingerprint = fingerprint
        )
        aiAnalysisDao.insertOrUpdate(pendingEntity)

        // 6. Build prompt and prepare multimodal assets if applicable
        val prompt = GeminiPromptBuilder.buildAnalysisPrompt(item)
        val imagePayload = if (item.contentType == ContentType.IMAGE) {
            prepareImageBase64(item.localMediaPath)
        } else {
            null
        }

        // 7. Execute Gemini API call
        val apiResult = apiClient.generateAnalysis(
            apiKey = apiKey,
            modelId = modelId,
            prompt = prompt,
            imageBase64 = imagePayload?.first,
            imageMimeType = imagePayload?.second
        )

        when (apiResult) {
            is GeminiApiResult.Success -> {
                val output = apiResult.data

                // Generate semantic search embedding
                val textForEmbedding = "${item.title} | ${output.shortDescription ?: ""} | ${output.whatItsAbout ?: ""} | ${item.notes ?: ""}"
                val embeddingResult = apiClient.generateEmbedding(apiKey, textForEmbedding)
                val embeddingJson = if (embeddingResult is GeminiApiResult.Success) {
                    JSONArray(embeddingResult.data).toString()
                } else null

                val successEntity = AiAnalysisEntity(
                    itemId = itemId,
                    shortDescription = output.shortDescription,
                    whatItsAbout = output.whatItsAbout,
                    category = output.category,
                    locationName = output.location.name,
                    locationAddress = output.location.address,
                    locationConfidence = output.location.confidence,
                    peopleAndEntitiesJson = JSONArray(output.peopleAndEntities).toString(),
                    keyDetailsJson = JSONArray(output.keyDetails).toString(),
                    keyTakeawaysJson = JSONArray(output.keyTakeaways).toString(),
                    language = output.language,
                    modelUsed = modelId,
                    analyzedAt = System.currentTimeMillis(),
                    status = "SUCCESS",
                    contentFingerprint = fingerprint,
                    errorMessage = null,
                    embeddingVectorJson = embeddingJson
                )

                aiAnalysisDao.insertOrUpdate(successEntity)
                aiSettingsRepository.incrementAnalysisCount()

                // Preserve original title/description while saving AI-enriched versions
                try {
                    val updatedItem = item.copy(
                        aiTitle = item.aiTitle ?: output.shortDescription?.take(60),
                        aiDescription = output.shortDescription
                    )
                    savedItemDao.updateItem(updatedItem)
                } catch (_: Exception) {}

                Result.success(successEntity)
            }

            is GeminiApiResult.Error -> {
                // Keep previous successful description if regeneration failed, but mark error
                val failedEntity = existing?.copy(
                    status = if (existing.status == "SUCCESS") "SUCCESS" else "FAILED",
                    errorMessage = apiResult.message,
                    analyzedAt = System.currentTimeMillis()
                ) ?: AiAnalysisEntity(
                    itemId = itemId,
                    shortDescription = null,
                    whatItsAbout = null,
                    category = null,
                    modelUsed = modelId,
                    status = "FAILED",
                    contentFingerprint = fingerprint,
                    errorMessage = apiResult.message
                )

                aiAnalysisDao.insertOrUpdate(failedEntity)
                Result.failure(Exception(apiResult.message))
            }
        }
    }

    suspend fun deleteAnalysis(itemId: Long) = withContext(Dispatchers.IO) {
        aiAnalysisDao.deleteAnalysis(itemId)
    }

    private fun prepareImageBase64(localPath: String?): Pair<String, String>? {
        if (localPath.isNullOrBlank()) return null
        val file = File(localPath)
        if (!file.exists() || !file.canRead()) return null

        return try {
            // Downscale image if too large (e.g. max dimension 1024) to conserve memory and API quota
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)

            val maxDim = maxOf(options.outWidth, options.outHeight)
            var sampleSize = 1
            while (maxDim / sampleSize > 1024) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            Pair(base64, "image/jpeg")
        } catch (_: Exception) {
            null
        }
    }
}
