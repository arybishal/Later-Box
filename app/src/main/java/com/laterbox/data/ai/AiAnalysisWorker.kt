package com.laterbox.data.ai

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.laterbox.LaterBoxApp
import java.util.concurrent.TimeUnit

class AiAnalysisWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_ITEM_ID = "ai_item_id"
        private const val WORK_NAME_PREFIX = "ai_analysis_item_"

        fun enqueue(context: Context, itemId: Long) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AiAnalysisWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_ITEM_ID to itemId))
                .setInitialDelay(2, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "$WORK_NAME_PREFIX$itemId",
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        val itemId = inputData.getLong(KEY_ITEM_ID, -1L)
        if (itemId <= 0L) return Result.failure()

        val app = applicationContext as? LaterBoxApp ?: return Result.failure()
        val aiSettings = app.aiSettingsRepository
        val geminiService = app.geminiAnalysisService

        // Check if user has AI enabled and auto-analysis enabled
        if (!aiSettings.isAiEnabledNow() || !aiSettings.isAutoAnalyzeNow()) {
            return Result.success()
        }

        return try {
            val result = geminiService.analyzeItem(itemId, forceRegenerate = false)
            if (result.isSuccess) {
                Result.success()
            } else {
                if (runAttemptCount < 2) Result.retry() else Result.failure()
            }
        } catch (_: Exception) {
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }
}
