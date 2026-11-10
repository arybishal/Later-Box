package com.laterbox.data.update

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.laterbox.LaterBoxApp
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class AppUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? LaterBoxApp ?: return Result.failure()
        val prefs = app.preferencesRepository

        val autoCheckEnabled = prefs.autoUpdateCheck.first()
        if (!autoCheckEnabled) {
            return Result.success()
        }

        val updateManager = app.updateManager
        val checkResult = updateManager.checkForUpdates()

        checkResult.onSuccess { updateInfo ->
            if (updateInfo != null && updateInfo.isNewer) {
                val lastDismissedTag = prefs.lastDismissedUpdateTag.first()
                if (lastDismissedTag != updateInfo.tag) {
                    val notificationHelper = UpdateNotificationHelper(applicationContext)
                    notificationHelper.showUpdateNotification(updateInfo)
                }
            }
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "laterbox_periodic_update_check"

        fun schedulePeriodicCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<AppUpdateWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancelPeriodicCheck(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
