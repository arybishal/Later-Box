package com.laterbox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.laterbox.data.local.entity.AiAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiAnalysisDao {

    @Query("SELECT * FROM ai_analyses WHERE itemId = :itemId LIMIT 1")
    fun getAnalysisForItem(itemId: Long): Flow<AiAnalysisEntity?>

    @Query("SELECT * FROM ai_analyses WHERE itemId = :itemId LIMIT 1")
    suspend fun getAnalysisSnapshot(itemId: Long): AiAnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(analysis: AiAnalysisEntity)

    @Query("DELETE FROM ai_analyses WHERE itemId = :itemId")
    suspend fun deleteAnalysis(itemId: Long)

    @Query("SELECT COUNT(*) FROM ai_analyses WHERE status = 'SUCCESS'")
    fun getSuccessfulAnalysisCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ai_analyses WHERE status = 'SUCCESS'")
    suspend fun getSuccessfulAnalysisCountSnapshot(): Int

    @Query("SELECT * FROM ai_analyses")
    suspend fun getAllAnalysesSnapshot(): List<AiAnalysisEntity>
}
