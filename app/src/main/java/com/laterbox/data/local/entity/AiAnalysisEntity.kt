package com.laterbox.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(
    tableName = "ai_analyses",
    foreignKeys = [
        ForeignKey(
            entity = SavedItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["status"]),
        Index(value = ["analyzedAt"])
    ]
)
data class AiAnalysisEntity(
    @PrimaryKey
    val itemId: Long,
    val shortDescription: String?,
    val whatItsAbout: String?,
    val category: String?,
    val locationName: String? = null,
    val locationAddress: String? = null,
    val locationConfidence: String? = "unknown",
    val peopleAndEntitiesJson: String = "[]",
    val keyDetailsJson: String = "[]",
    val keyTakeawaysJson: String = "[]",
    val language: String? = "English",
    val modelUsed: String,
    val analyzedAt: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS",
    val contentFingerprint: String? = null,
    val errorMessage: String? = null,
    val embeddingVectorJson: String? = null
) {
    fun getEmbeddingVector(): List<Float> = try {
        if (embeddingVectorJson.isNullOrBlank()) emptyList()
        else {
            val arr = JSONArray(embeddingVectorJson)
            (0 until arr.length()).map { arr.getDouble(it).toFloat() }
        }
    } catch (_: Exception) {
        emptyList()
    }
    fun getPeopleAndEntities(): List<String> = try {
        val arr = JSONArray(peopleAndEntitiesJson)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (_: Exception) {
        emptyList()
    }

    fun getKeyDetails(): List<String> = try {
        val arr = JSONArray(keyDetailsJson)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (_: Exception) {
        emptyList()
    }

    fun getKeyTakeaways(): List<String> = try {
        val arr = JSONArray(keyTakeawaysJson)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (_: Exception) {
        emptyList()
    }
}
