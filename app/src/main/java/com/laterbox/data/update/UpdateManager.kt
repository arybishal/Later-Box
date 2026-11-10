package com.laterbox.data.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.laterbox.data.local.preferences.UserPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class UpdateManager(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val releaseService: GitHubReleaseService = GitHubReleaseService(),
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var downloadJob: Job? = null

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle(0L))
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    val currentVersionName: String by lazy {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.3.1"
        } catch (_: Exception) {
            "1.3.1"
        }
    }

    val currentVersionCode: Long by lazy {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
        } catch (_: Exception) {
            6L
        }
    }

    init {
        scope.launch {
            val lastChecked = preferencesRepository.lastUpdateCheckTimestamp.first()
            _updateState.value = UpdateState.Idle(lastChecked)
        }
    }

    /**
     * Checks for updates from GitHub Releases API.
     */
    suspend fun checkForUpdates(): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            _updateState.value = UpdateState.Checking

            val updateInfo = releaseService.fetchLatestRelease(currentVersionName, currentVersionCode)
            val now = System.currentTimeMillis()
            preferencesRepository.setLastUpdateCheckTimestamp(now)

            if (updateInfo != null && updateInfo.isNewer) {
                _updateState.value = UpdateState.Available(updateInfo)
                Result.success(updateInfo)
            } else {
                _updateState.value = UpdateState.UpToDate(now, currentVersionName)
                Result.success(null)
            }
        } catch (e: Exception) {
            val lastChecked = preferencesRepository.lastUpdateCheckTimestamp.first()
            val errorMessage = when (e) {
                is UpdateCheckException -> e.message ?: "Update check failed"
                else -> e.localizedMessage ?: "Failed to check for updates. Check your internet connection."
            }
            _updateState.value = UpdateState.Failed(
                message = errorMessage,
                canRetry = true
            )
            Result.failure(e)
        }
    }

    /**
     * Downloads the APK file with streaming progress.
     */
    fun startDownload(updateInfo: UpdateInfo) {
        downloadJob?.cancel()
        downloadJob = scope.launch(Dispatchers.IO) {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            // Clean older APK files
            updatesDir.listFiles()?.forEach { it.delete() }

            val targetFile = File(updatesDir, updateInfo.apkFileName)

            try {
                _updateState.value = UpdateState.Downloading(
                    updateInfo = updateInfo,
                    bytesDownloaded = 0L,
                    totalBytes = updateInfo.apkSizeBytes,
                    progress = 0f
                )

                val request = Request.Builder()
                    .url(updateInfo.apkUrl)
                    .header("User-Agent", "LaterBox-Android-App")
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code}: ${response.message}")
                    }

                    val body = response.body ?: throw IOException("Empty response body")
                    val totalBytes = if (updateInfo.apkSizeBytes > 0) updateInfo.apkSizeBytes else body.contentLength()

                    body.byteStream().use { input ->
                        FileOutputStream(targetFile).use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesCopied = 0L
                            var read: Int

                            while (input.read(buffer).also { read = it } != -1) {
                                if (!isActive) {
                                    targetFile.delete()
                                    throw CancellationException("Download was cancelled")
                                }
                                output.write(buffer, 0, read)
                                bytesCopied += read

                                val progress = if (totalBytes > 0) {
                                    (bytesCopied.toFloat() / totalBytes).coerceIn(0f, 1f)
                                } else 0f

                                _updateState.value = UpdateState.Downloading(
                                    updateInfo = updateInfo,
                                    bytesDownloaded = bytesCopied,
                                    totalBytes = totalBytes,
                                    progress = progress
                                )
                            }
                        }
                    }
                }

                // Verification of downloaded APK
                validateDownloadedApk(targetFile, updateInfo)

                _updateState.value = UpdateState.Downloaded(updateInfo, targetFile)

            } catch (e: CancellationException) {
                targetFile.delete()
                val lastChecked = preferencesRepository.lastUpdateCheckTimestamp.first()
                _updateState.value = UpdateState.Idle(lastChecked)
            } catch (e: Exception) {
                targetFile.delete()
                _updateState.value = UpdateState.Failed(
                    message = "Download failed: ${e.localizedMessage ?: "Network error"}",
                    canRetry = true
                )
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
    }

    /**
     * Validates that the downloaded APK is genuine, belongs to com.laterbox, and can be parsed.
     */
    private fun validateDownloadedApk(apkFile: File, updateInfo: UpdateInfo) {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            throw IOException("Downloaded file is empty or missing")
        }

        if (updateInfo.apkSizeBytes > 0 && apkFile.length() != updateInfo.apkSizeBytes) {
            throw IOException("Downloaded APK size (${apkFile.length()} bytes) does not match expected size (${updateInfo.apkSizeBytes} bytes)")
        }

        val packageInfo = context.packageManager.getPackageArchiveInfo(
            apkFile.absolutePath,
            PackageManager.GET_ACTIVITIES
        ) ?: throw IOException("Downloaded file is not a valid Android APK")

        if (packageInfo.packageName != context.packageName) {
            throw IOException("Downloaded APK package ID (${packageInfo.packageName}) does not match Later Box (${context.packageName})")
        }

        // Validate SHA-256 checksum if available
        val expectedSha = updateInfo.expectedSha256
        if (!expectedSha.isNullOrEmpty()) {
            val actualSha = computeSha256(apkFile)
            if (!actualSha.equals(expectedSha, ignoreCase = true)) {
                throw IOException("APK checksum mismatch. Expected: $expectedSha, Actual: $actualSha")
            }
        }
    }

    private fun computeSha256(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies permissions and launches the system Package Installer.
     */
    fun installUpdate(activityContext: Context, apkFile: File): Boolean {
        if (!apkFile.exists()) {
            _updateState.value = UpdateState.Failed("APK file not found on device", canRetry = true)
            return false
        }

        // Android 8.0+ Unknown App Sources Permission Check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!activityContext.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${activityContext.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activityContext.startActivity(intent)
                return false
            }
        }

        return try {
            val contentUri: Uri = FileProvider.getUriForFile(
                activityContext,
                "${activityContext.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            activityContext.startActivity(installIntent)
            true
        } catch (e: Exception) {
            _updateState.value = UpdateState.Failed(
                "Unable to start installation: ${e.localizedMessage}",
                canRetry = true
            )
            false
        }
    }
}
