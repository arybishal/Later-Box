package com.laterbox.data.storage

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MediaStorageManager(private val context: Context) {

    private val httpClient = OkHttpClient()

    private val mediaDir: File by lazy {
        val dir = File(context.filesDir, "media")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    suspend fun saveSharedUriToInternalStorage(uri: Uri, mimeType: String?): Pair<String, Long> = withContext(Dispatchers.IO) {
        val extension = when {
            mimeType?.startsWith("image/png") == true -> ".png"
            mimeType?.startsWith("image/webp") == true -> ".webp"
            mimeType?.startsWith("image/gif") == true -> ".gif"
            mimeType?.startsWith("image/") == true -> ".jpg"
            mimeType?.startsWith("video/mp4") == true -> ".mp4"
            mimeType?.startsWith("video/") == true -> ".mp4"
            mimeType?.contains("pdf") == true -> ".pdf"
            else -> ".bin"
        }

        val destFile = File(mediaDir, "media_${UUID.randomUUID()}$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Cannot read input stream from URI: $uri")

        Pair(destFile.absolutePath, destFile.length())
    }

    suspend fun downloadRemoteMedia(url: String, onProgress: ((Float) -> Unit)? = null): Pair<String, Long>? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) return@withContext null
                val body = response.body!!
                val contentLength = body.contentLength()
                val ext = when {
                    url.contains(".png") -> ".png"
                    url.contains(".webp") -> ".webp"
                    url.contains(".gif") -> ".gif"
                    url.contains(".mp4") -> ".mp4"
                    url.contains(".pdf") -> ".pdf"
                    else -> ".jpg"
                }
                val destFile = File(mediaDir, "dl_${UUID.randomUUID()}$ext")
                var bytesCopied = 0L

                body.byteStream().use { input ->
                    FileOutputStream(destFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            output.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            if (contentLength > 0 && onProgress != null) {
                                onProgress(bytesCopied.toFloat() / contentLength.toFloat())
                            }
                            bytes = input.read(buffer)
                        }
                    }
                }
                Pair(destFile.absolutePath, destFile.length())
            }
        } catch (_: Exception) {
            null
        }
    }

    fun deleteLocalMedia(filePath: String?): Boolean {
        if (filePath.isNullOrBlank()) return false
        return try {
            val file = File(filePath)
            if (file.exists()) file.delete() else false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getMediaStorageSize(): Long = withContext(Dispatchers.IO) {
        calculateDirSize(mediaDir)
    }

    suspend fun clearMediaCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            mediaDir.listFiles()?.forEach { it.delete() }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getMediaDirectory(): File = mediaDir

    fun getAllMediaFiles(): List<File> = mediaDir.listFiles()?.filter { it.isFile } ?: emptyList()

    suspend fun restoreMediaFile(fileName: String, input: java.io.InputStream): File = withContext(Dispatchers.IO) {
        val safeName = File(fileName).name
        val dest = File(mediaDir, safeName)
        java.io.FileOutputStream(dest).use { output ->
            input.copyTo(output)
        }
        dest
    }

    private fun calculateDirSize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) calculateDirSize(file) else file.length()
        }
        return size
    }
}
