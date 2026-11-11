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
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class AiAnalysisWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_ITEM_ID = "ai_item_id"
        private const val WORK_NAME_PREFIX = "ai_analysis_item_"

        fun enqueue(context: Context, itemId: Long, requireWifi: Boolean = false) {
            val networkType = if (requireWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(networkType)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AiAnalysisWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_ITEM_ID to itemId))
                .setInitialDelay(1, TimeUnit.SECONDS)
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

        // Check Wi-Fi constraint if configured
        val requiresWifi = try { aiSettings.requireWifiForAi.first() } catch (_: Exception) { false }
        if (requiresWifi) {
            val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            val isWifiOrEthernet = capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true ||
                    capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) == true
            if (!isWifiOrEthernet) {
                // Retry when Wi-Fi is connected
                return Result.retry()
            }
        }

        return try {
            val result = geminiService.analyzeItem(itemId, forceRegenerate = false)
            if (result.isSuccess) {
                Result.success()
            } else {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        } catch (_: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
