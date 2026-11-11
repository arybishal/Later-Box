package com.laterbox.data.backup

import android.content.Context
import android.net.Uri
import com.laterbox.data.local.LaterBoxDatabase
import com.laterbox.data.local.entity.ItemPlaylistCrossRef
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.preferences.UserPreferencesRepository
import com.laterbox.data.model.ContentType
import com.laterbox.data.model.DownloadStatus
import com.laterbox.data.storage.MediaStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(
    private val context: Context,
    private val database: LaterBoxDatabase,
    private val preferencesRepository: UserPreferencesRepository,
    private val mediaStorageManager: MediaStorageManager
) {

    private val savedItemDao = database.savedItemDao()
    private val playlistDao = database.playlistDao()

    suspend fun getBackupSummary(): BackupSummary = withContext(Dispatchers.IO) {
        val items = savedItemDao.getAllItemsSnapshot()
        val playlists = playlistDao.getAllPlaylistsSnapshot()
        val mediaFiles = mediaStorageManager.getAllMediaFiles()
        val totalMediaSize = mediaStorageManager.getMediaStorageSize()

        BackupSummary(
            totalItems = items.size,
            totalPlaylists = playlists.size,
            totalMediaFiles = mediaFiles.size,
            estimatedSizeBytes = totalMediaSize + (items.size * 512L) + 4096L
        )
    }

    suspend fun exportBackup(
        outputStream: OutputStream,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result<BackupManifest> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.05f, "Preparing library snapshot…")
            val items = savedItemDao.getAllItemsSnapshot()
            val playlists = playlistDao.getAllPlaylistsSnapshot()
            val crossRefs = playlistDao.getAllCrossRefsSnapshot()
            val preferences = preferencesRepository.getAllPreferencesSnapshot()
            val mediaFiles = mediaStorageManager.getAllMediaFiles()

            // Build payload JSON
            onProgress(0.20f, "Serializing database records…")
            val payloadObj = JSONObject()

            // Map IDs to relative references
            val itemIdToUrl = items.associate { it.id to it.url }
            val playlistIdToName = playlists.associate { it.id to it.name }

            val itemsArray = JSONArray()
            for (item in items) {
                val itemObj = JSONObject().apply {
                    put("id", item.id)
                    put("url", item.url)
                    put("title", item.title)
                    put("description", item.description ?: JSONObject.NULL)
                    put("sourceDomain", item.sourceDomain ?: JSONObject.NULL)
                    put("sourceApp", item.sourceApp ?: JSONObject.NULL)
                    put("thumbnailUrl", item.thumbnailUrl ?: JSONObject.NULL)
                    // Store relative media path for device-agnostic portability
                    val relMedia = item.localMediaPath?.let { File(it).name }
                    put("localMediaName", relMedia ?: JSONObject.NULL)
                    put("contentType", item.contentType.name)
                    put("mimeType", item.mimeType ?: JSONObject.NULL)
                    put("savedAt", item.savedAt)
                    put("lastOpenedAt", item.lastOpenedAt ?: JSONObject.NULL)
                    put("isFavorite", item.isFavorite)
                    put("isArchived", item.isArchived)
                    put("isDeleted", item.isDeleted)
                    put("deletedAt", item.deletedAt ?: JSONObject.NULL)
                    put("notes", item.notes ?: JSONObject.NULL)
                    put("readProgress", item.readProgress.toDouble())
                    put("mediaSize", item.mediaSize)
                    put("downloadStatus", item.downloadStatus.name)
                    put("isDemo", item.isDemo)
                    put("demoRecordType", item.demoRecordType ?: JSONObject.NULL)
                }
                itemsArray.put(itemObj)
            }
            payloadObj.put("items", itemsArray)

            val playlistsArray = JSONArray()
            for (p in playlists) {
                val pObj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("description", p.description ?: JSONObject.NULL)
                    put("iconName", p.iconName)
                    put("coverUrl", p.coverUrl ?: JSONObject.NULL)
                    put("createdAt", p.createdAt)
                    put("isDefaultInbox", p.isDefaultInbox)
                    put("isDemo", p.isDemo)
                }
                playlistsArray.put(pObj)
            }
            payloadObj.put("playlists", playlistsArray)

            val crossRefsArray = JSONArray()
            for (cr in crossRefs) {
                val itemUrl = itemIdToUrl[cr.contentId]
                val pName = playlistIdToName[cr.playlistId]
                if (itemUrl != null && pName != null) {
                    val crObj = JSONObject().apply {
                        put("contentUrl", itemUrl)
                        put("playlistName", pName)
                        put("addedAt", cr.addedAt)
                    }
                    crossRefsArray.put(crObj)
                }
            }
            payloadObj.put("crossRefs", crossRefsArray)

            val prefsObj = JSONObject()
            preferences.forEach { (k, v) -> prefsObj.put(k, v) }
            payloadObj.put("preferences", prefsObj)

            val payloadBytes = payloadObj.toString().toByteArray(StandardCharsets.UTF_8)
            val sha256 = computeSha256(payloadBytes)

            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val appVersion = packageInfo.versionName ?: "1.2.0"
            @Suppress("DEPRECATION")
            val appVersionCode = packageInfo.versionCode

            val manifest = BackupManifest(
                schemaVersion = 1,
                appVersion = appVersion,
                appVersionCode = appVersionCode,
                createdAt = System.currentTimeMillis(),
                totalItems = items.size,
                totalPlaylists = playlists.size,
                totalMediaFiles = mediaFiles.size,
                estimatedBytes = payloadBytes.size.toLong() + mediaFiles.sumOf { it.length() },
                sha256Checksum = sha256
            )

            val manifestObj = JSONObject().apply {
                put("schemaVersion", manifest.schemaVersion)
                put("appVersion", manifest.appVersion)
                put("appVersionCode", manifest.appVersionCode)
                put("createdAt", manifest.createdAt)
                put("totalItems", manifest.totalItems)
                put("totalPlaylists", manifest.totalPlaylists)
                put("totalMediaFiles", manifest.totalMediaFiles)
                put("estimatedBytes", manifest.estimatedBytes)
                put("sha256Checksum", manifest.sha256Checksum)
            }
            val manifestBytes = manifestObj.toString().toByteArray(StandardCharsets.UTF_8)

            // Stream into ZIP archive
            onProgress(0.40f, "Writing archive payload…")
            val zipOut = ZipOutputStream(BufferedOutputStream(outputStream))
            zipOut.use { zos ->
                // 1. Entry: manifest.json
                val manifestEntry = ZipEntry("manifest.json")
                zos.putNextEntry(manifestEntry)
                zos.write(manifestBytes)
                zos.closeEntry()

                // 2. Entry: data.json
                val payloadEntry = ZipEntry("data.json")
                zos.putNextEntry(payloadEntry)
                zos.write(payloadBytes)
                zos.closeEntry()

                // 3. Media files
                val totalMedia = mediaFiles.size
                mediaFiles.forEachIndexed { index, file ->
                    val progressRatio = 0.45f + (index.toFloat() / totalMedia.coerceAtLeast(1) * 0.50f)
                    onProgress(progressRatio, "Archiving media: ${file.name} (${index + 1}/$totalMedia)")
                    if (file.exists() && file.isFile) {
                        val mediaEntry = ZipEntry("media/${file.name}")
                        zos.putNextEntry(mediaEntry)
                        FileInputStream(file).use { fis ->
                            fis.copyTo(zos)
                        }
                        zos.closeEntry()
                    }
                }
            }

            // Record last backup timestamp
            preferencesRepository.setLastBackupTimestamp(manifest.createdAt)
            onProgress(1.0f, "Backup completed successfully!")

            Result.success(manifest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun inspectBackup(inputStream: InputStream): Result<BackupManifest> = withContext(Dispatchers.IO) {
        try {
            val zipIn = ZipInputStream(BufferedInputStream(inputStream))
            var manifest: BackupManifest? = null

            var entry = zipIn.nextEntry
            while (entry != null) {
                if (entry.name == "manifest.json") {
                    val baos = ByteArrayOutputStream()
                    zipIn.copyTo(baos)
                    val jsonStr = baos.toString(StandardCharsets.UTF_8.name())
                    val obj = JSONObject(jsonStr)

                    manifest = BackupManifest(
                        schemaVersion = obj.optInt("schemaVersion", 1),
                        appVersion = obj.optString("appVersion", "Unknown"),
                        appVersionCode = obj.optInt("appVersionCode", 1),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        totalItems = obj.optInt("totalItems", 0),
                        totalPlaylists = obj.optInt("totalPlaylists", 0),
                        totalMediaFiles = obj.optInt("totalMediaFiles", 0),
                        estimatedBytes = obj.optLong("estimatedBytes", 0L),
                        sha256Checksum = obj.optString("sha256Checksum", "")
                    )
                    break
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }

            if (manifest == null) {
                return@withContext Result.failure(IllegalArgumentException("Invalid Later Box backup: manifest.json missing"))
            }

            if (manifest.schemaVersion > 1) {
                return@withContext Result.failure(
                    IllegalStateException("Unsupported backup schema (${manifest.schemaVersion}). Please update Later Box to restore this backup.")
                )
            }

            Result.success(manifest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(
        inputStream: InputStream,
        mode: ImportMode,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.05f, "Reading and validating backup archive…")
            val tempDir = File(context.cacheDir, "backup_restore_${System.currentTimeMillis()}").apply { mkdirs() }

            var manifestBytes: ByteArray? = null
            var payloadBytes: ByteArray? = null
            val extractedMediaFiles = mutableListOf<File>()

            // Extract with Zip Slip prevention
            val zipIn = ZipInputStream(BufferedInputStream(inputStream))
            zipIn.use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    // Zip Slip Path Traversal Verification
                    val targetFile = File(tempDir, entryName)
                    val canonicalDest = tempDir.canonicalPath
                    val canonicalTarget = targetFile.canonicalPath
                    if (!canonicalTarget.startsWith(canonicalDest + File.separator) && canonicalTarget != canonicalDest) {
                        throw SecurityException("Zip entry path traversal detected: $entryName")
                    }

                    if (entry.isDirectory) {
                        targetFile.mkdirs()
                    } else {
                        targetFile.parentFile?.mkdirs()
                        if (entryName == "manifest.json") {
                            val baos = ByteArrayOutputStream()
                            zis.copyTo(baos)
                            manifestBytes = baos.toByteArray()
                        } else if (entryName == "data.json") {
                            val baos = ByteArrayOutputStream()
                            zis.copyTo(baos)
                            payloadBytes = baos.toByteArray()
                        } else if (entryName.startsWith("media/")) {
                            FileOutputStream(targetFile).use { fos ->
                                zis.copyTo(fos)
                            }
                            extractedMediaFiles.add(targetFile)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            if (manifestBytes == null || payloadBytes == null) {
                tempDir.deleteRecursively()
                return@withContext Result.failure(IllegalArgumentException("Corrupted backup: manifest or database data missing."))
            }

            onProgress(0.25f, "Verifying checksums and archive integrity…")
            val manifestObj = JSONObject(String(manifestBytes!!, StandardCharsets.UTF_8))
            val expectedSha256 = manifestObj.getString("sha256Checksum")
            val computedSha256 = computeSha256(payloadBytes!!)

            if (!expectedSha256.equals(computedSha256, ignoreCase = true)) {
                tempDir.deleteRecursively()
                return@withContext Result.failure(SecurityException("Data integrity check failed: archive payload SHA-256 mismatch."))
            }

            onProgress(0.40f, "Parsing database payload…")
            val payloadObj = JSONObject(String(payloadBytes!!, StandardCharsets.UTF_8))
            val itemsArray = payloadObj.optJSONArray("items") ?: JSONArray()
            val playlistsArray = payloadObj.optJSONArray("playlists") ?: JSONArray()
            val crossRefsArray = payloadObj.optJSONArray("crossRefs") ?: JSONArray()
            val prefsObj = payloadObj.optJSONObject("preferences")

            // Copy extracted media into app media directory
            onProgress(0.50f, "Restoring local media files…")
            val mediaDir = mediaStorageManager.getMediaDirectory()
            extractedMediaFiles.forEach { file ->
                val destFile = File(mediaDir, file.name)
                if (!destFile.exists()) {
                    file.copyTo(destFile, overwrite = true)
                }
            }

            // Restore Preferences
            if (prefsObj != null) {
                val prefMap = mutableMapOf<String, String>()
                val keys = prefsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    prefMap[k] = prefsObj.getString(k)
                }
                preferencesRepository.restorePreferences(prefMap)
            }

            var itemsRestored = 0
            var itemsSkipped = 0
            var playlistsRestored = 0

            onProgress(0.70f, "Applying database changes (${mode.name})…")
            if (mode == ImportMode.REPLACE) {
                // Clear existing records
                savedItemDao.clearAllItems()
                playlistDao.clearAllPlaylists()
                playlistDao.clearAllCrossRefs()

                // Insert Playlists
                val playlistNameToId = mutableMapOf<String, Long>()
                for (i in 0 until playlistsArray.length()) {
                    val pObj = playlistsArray.getJSONObject(i)
                    val p = PlaylistEntity(
                        id = pObj.optLong("id", 0L),
                        name = pObj.getString("name"),
                        description = if (pObj.isNull("description")) null else pObj.optString("description"),
                        iconName = pObj.optString("iconName", "bookmark"),
                        coverUrl = if (pObj.isNull("coverUrl")) null else pObj.optString("coverUrl"),
                        createdAt = pObj.optLong("createdAt", System.currentTimeMillis()),
                        isDefaultInbox = pObj.optBoolean("isDefaultInbox", false),
                        isDemo = pObj.optBoolean("isDemo", false)
                    )
                    val insertedId = playlistDao.insertPlaylist(p)
                    val finalId = if (p.id != 0L) p.id else insertedId
                    playlistNameToId[p.name.lowercase()] = finalId
                    playlistsRestored++
                }

                // Insert Items
                val itemUrlToId = mutableMapOf<String, Long>()
                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(i)
                    val localMediaName = if (itemObj.isNull("localMediaName")) null else itemObj.optString("localMediaName")
                    val fullMediaPath = localMediaName?.let { File(mediaDir, it).absolutePath }

                    val item = SavedItemEntity(
                        id = itemObj.optLong("id", 0L),
                        url = itemObj.getString("url"),
                        title = itemObj.getString("title"),
                        description = if (itemObj.isNull("description")) null else itemObj.optString("description"),
                        sourceDomain = if (itemObj.isNull("sourceDomain")) null else itemObj.optString("sourceDomain"),
                        sourceApp = if (itemObj.isNull("sourceApp")) null else itemObj.optString("sourceApp"),
                        thumbnailUrl = if (itemObj.isNull("thumbnailUrl")) null else itemObj.optString("thumbnailUrl"),
                        localMediaPath = fullMediaPath,
                        contentType = try { ContentType.valueOf(itemObj.getString("contentType")) } catch (_: Exception) { ContentType.LINK },
                        mimeType = if (itemObj.isNull("mimeType")) null else itemObj.optString("mimeType"),
                        savedAt = itemObj.optLong("savedAt", System.currentTimeMillis()),
                        lastOpenedAt = if (itemObj.isNull("lastOpenedAt")) null else itemObj.optLong("lastOpenedAt"),
                        isFavorite = itemObj.optBoolean("isFavorite", false),
                        isArchived = itemObj.optBoolean("isArchived", false),
                        isDeleted = itemObj.optBoolean("isDeleted", false),
                        deletedAt = if (itemObj.isNull("deletedAt")) null else itemObj.optLong("deletedAt"),
                        notes = if (itemObj.isNull("notes")) null else itemObj.optString("notes"),
                        readProgress = itemObj.optDouble("readProgress", 0.0).toFloat(),
                        mediaSize = itemObj.optLong("mediaSize", 0L),
                        downloadStatus = try { DownloadStatus.valueOf(itemObj.getString("downloadStatus")) } catch (_: Exception) { DownloadStatus.NONE },
                        isDemo = itemObj.optBoolean("isDemo", false),
                        demoRecordType = if (itemObj.isNull("demoRecordType")) null else itemObj.optString("demoRecordType")
                    )
                    val insertedId = savedItemDao.insertItem(item)
                    val finalId = if (item.id != 0L) item.id else insertedId
                    itemUrlToId[item.url] = finalId
                    itemsRestored++
                }

                // Insert CrossRefs
                for (i in 0 until crossRefsArray.length()) {
                    val crObj = crossRefsArray.getJSONObject(i)
                    val cUrl = crObj.getString("contentUrl")
                    val pName = crObj.getString("playlistName")
                    val addedAt = crObj.optLong("addedAt", System.currentTimeMillis())

                    val cId = itemUrlToId[cUrl]
                    val pId = playlistNameToId[pName.lowercase()]
                    if (cId != null && pId != null) {
                        playlistDao.insertCrossRef(ItemPlaylistCrossRef(contentId = cId, playlistId = pId, addedAt = addedAt))
                    }
                }
            } else {
                // MERGE Mode: Keep existing items and playlists, avoid duplicates by URL and name
                val existingPlaylists = playlistDao.getAllPlaylistsSnapshot()
                val playlistNameToId = existingPlaylists.associate { it.name.lowercase() to it.id }.toMutableMap()

                for (i in 0 until playlistsArray.length()) {
                    val pObj = playlistsArray.getJSONObject(i)
                    val pName = pObj.getString("name")
                    if (!playlistNameToId.containsKey(pName.lowercase())) {
                        val p = PlaylistEntity(
                            name = pName,
                            description = if (pObj.isNull("description")) null else pObj.optString("description"),
                            iconName = pObj.optString("iconName", "bookmark"),
                            coverUrl = if (pObj.isNull("coverUrl")) null else pObj.optString("coverUrl"),
                            createdAt = pObj.optLong("createdAt", System.currentTimeMillis()),
                            isDefaultInbox = pObj.optBoolean("isDefaultInbox", false),
                            isDemo = pObj.optBoolean("isDemo", false)
                        )
                        val insertedId = playlistDao.insertPlaylist(p)
                        playlistNameToId[pName.lowercase()] = insertedId
                        playlistsRestored++
                    }
                }

                val itemUrlToId = mutableMapOf<String, Long>()
                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(i)
                    val url = itemObj.getString("url")
                    val existing = savedItemDao.getItemByUrl(url)

                    if (existing != null) {
                        itemUrlToId[url] = existing.id
                        itemsSkipped++
                    } else {
                        val localMediaName = if (itemObj.isNull("localMediaName")) null else itemObj.optString("localMediaName")
                        val fullMediaPath = localMediaName?.let { File(mediaDir, it).absolutePath }

                        val item = SavedItemEntity(
                            url = url,
                            title = itemObj.getString("title"),
                            description = if (itemObj.isNull("description")) null else itemObj.optString("description"),
                            sourceDomain = if (itemObj.isNull("sourceDomain")) null else itemObj.optString("sourceDomain"),
                            sourceApp = if (itemObj.isNull("sourceApp")) null else itemObj.optString("sourceApp"),
                            thumbnailUrl = if (itemObj.isNull("thumbnailUrl")) null else itemObj.optString("thumbnailUrl"),
                            localMediaPath = fullMediaPath,
                            contentType = try { ContentType.valueOf(itemObj.getString("contentType")) } catch (_: Exception) { ContentType.LINK },
                            mimeType = if (itemObj.isNull("mimeType")) null else itemObj.optString("mimeType"),
                            savedAt = itemObj.optLong("savedAt", System.currentTimeMillis()),
                            lastOpenedAt = if (itemObj.isNull("lastOpenedAt")) null else itemObj.optLong("lastOpenedAt"),
                            isFavorite = itemObj.optBoolean("isFavorite", false),
                            isArchived = itemObj.optBoolean("isArchived", false),
                            isDeleted = itemObj.optBoolean("isDeleted", false),
                            deletedAt = if (itemObj.isNull("deletedAt")) null else itemObj.optLong("deletedAt"),
                            notes = if (itemObj.isNull("notes")) null else itemObj.optString("notes"),
                            readProgress = itemObj.optDouble("readProgress", 0.0).toFloat(),
                            mediaSize = itemObj.optLong("mediaSize", 0L),
                            downloadStatus = try { DownloadStatus.valueOf(itemObj.getString("downloadStatus")) } catch (_: Exception) { DownloadStatus.NONE },
                            isDemo = itemObj.optBoolean("isDemo", false),
                            demoRecordType = if (itemObj.isNull("demoRecordType")) null else itemObj.optString("demoRecordType")
                        )
                        val insertedId = savedItemDao.insertItem(item)
                        itemUrlToId[url] = insertedId
                        itemsRestored++
                    }
                }

                // CrossRefs
                for (i in 0 until crossRefsArray.length()) {
                    val crObj = crossRefsArray.getJSONObject(i)
                    val cUrl = crObj.getString("contentUrl")
                    val pName = crObj.getString("playlistName")
                    val addedAt = crObj.optLong("addedAt", System.currentTimeMillis())

                    val cId = itemUrlToId[cUrl]
                    val pId = playlistNameToId[pName.lowercase()]
                    if (cId != null && pId != null) {
                        playlistDao.insertCrossRef(ItemPlaylistCrossRef(contentId = cId, playlistId = pId, addedAt = addedAt))
                    }
                }
            }

            tempDir.deleteRecursively()
            onProgress(1.0f, "Restore completed successfully!")

            Result.success(
                ImportResult(
                    itemsRestored = itemsRestored,
                    itemsSkipped = itemsSkipped,
                    playlistsRestored = playlistsRestored,
                    mediaFilesRestored = extractedMediaFiles.size,
                    mode = mode
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun computeSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
