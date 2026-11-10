package com.laterbox.data.update

import java.io.File

sealed interface UpdateState {
    data class Idle(val lastChecked: Long) : UpdateState
    data object Checking : UpdateState
    data class UpToDate(val lastChecked: Long, val currentVersion: String) : UpdateState
    data class Available(val updateInfo: UpdateInfo) : UpdateState
    data class Downloading(
        val updateInfo: UpdateInfo,
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val progress: Float
    ) : UpdateState
    data class Downloaded(val updateInfo: UpdateInfo, val apkFile: File) : UpdateState
    data class Installing(val updateInfo: UpdateInfo, val apkFile: File) : UpdateState
    data class Failed(val message: String, val canRetry: Boolean) : UpdateState
}
