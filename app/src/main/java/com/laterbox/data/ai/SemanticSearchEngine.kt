package com.laterbox.data.ai

import com.laterbox.data.ai.network.GeminiApiClient
import com.laterbox.data.ai.network.GeminiApiResult
import com.laterbox.data.local.dao.AiAnalysisDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SemanticMatch(
    val itemId: Long,
    val similarityScore: Float
)

class SemanticSearchEngine(
    private val apiClient: GeminiApiClient,
    private val aiAnalysisDao: AiAnalysisDao
) {

    suspend fun search(
        query: String,
        apiKey: String,
        minSimilarity: Float = 0.45f
    ): List<SemanticMatch> = withContext(Dispatchers.IO) {
        if (query.isBlank() || apiKey.isBlank()) return@withContext emptyList()

        val embedRes = apiClient.generateEmbedding(apiKey, query.trim())
        if (embedRes !is GeminiApiResult.Success) {
            return@withContext emptyList()
        }

        val queryVec = embedRes.data
        if (queryVec.isEmpty()) return@withContext emptyList()

        val allAnalyses = aiAnalysisDao.getAllAnalysesSnapshot()
        val matches = mutableListOf<SemanticMatch>()

        for (analysis in allAnalyses) {
            val itemVec = analysis.getEmbeddingVector()
            if (itemVec.isNotEmpty()) {
                val sim = cosineSimilarity(queryVec, itemVec)
                if (sim >= minSimilarity) {
                    matches.add(SemanticMatch(analysis.itemId, sim))
                }
            }
        }

        matches.sortedByDescending { it.similarityScore }
    }

    companion object {
        fun cosineSimilarity(vecA: List<Float>, vecB: List<Float>): Float {
            if (vecA.isEmpty() || vecB.isEmpty() || vecA.size != vecB.size) return 0f
            var dot = 0.0
            var normA = 0.0
            var normB = 0.0
            for (i in vecA.indices) {
                val a = vecA[i].toDouble()
                val b = vecB[i].toDouble()
                dot += a * b
                normA += a * a
                normB += b * b
            }
            val denom = Math.sqrt(normA * normB)
            return if (denom > 0.0) (dot / denom).toFloat() else 0f
        }
    }
}
