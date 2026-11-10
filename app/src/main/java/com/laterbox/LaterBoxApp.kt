package com.laterbox

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.laterbox.data.local.LaterBoxDatabase
import com.laterbox.data.local.preferences.UserPreferencesRepository
import com.laterbox.data.repository.ContentRepository
import com.laterbox.data.repository.PlaylistRepository
import com.laterbox.data.storage.MediaStorageManager

class LaterBoxApp : Application(), ImageLoaderFactory {

    lateinit var database: LaterBoxDatabase private set
    lateinit var contentRepository: ContentRepository private set
    lateinit var playlistRepository: PlaylistRepository private set
    lateinit var preferencesRepository: UserPreferencesRepository private set
    lateinit var mediaStorageManager: MediaStorageManager private set
    lateinit var backupManager: com.laterbox.data.backup.BackupManager private set
    lateinit var tagRepository: com.laterbox.data.repository.TagRepository private set
    lateinit var reminderRepository: com.laterbox.data.repository.ReminderRepository private set
    lateinit var searchHistoryRepository: com.laterbox.data.repository.SearchHistoryRepository private set
    lateinit var updateManager: com.laterbox.data.update.UpdateManager private set
    lateinit var aiSettingsRepository: com.laterbox.data.ai.AiSettingsRepository private set
    lateinit var geminiAnalysisService: com.laterbox.data.ai.GeminiAnalysisService private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = LaterBoxDatabase.getInstance(this)
        mediaStorageManager = MediaStorageManager(this)
        preferencesRepository = UserPreferencesRepository(this)
        contentRepository = ContentRepository(
            savedItemDao = database.savedItemDao(),
            playlistDao = database.playlistDao(),
            mediaStorageManager = mediaStorageManager
        )
        playlistRepository = PlaylistRepository(
            playlistDao = database.playlistDao()
        )
        tagRepository = com.laterbox.data.repository.TagRepository(
            tagDao = database.tagDao()
        )
        reminderRepository = com.laterbox.data.repository.ReminderRepository(
            reminderDao = database.reminderDao()
        )
        searchHistoryRepository = com.laterbox.data.repository.SearchHistoryRepository(
            searchHistoryDao = database.searchHistoryDao()
        )
        backupManager = com.laterbox.data.backup.BackupManager(
            context = this,
            database = database,
            preferencesRepository = preferencesRepository,
            mediaStorageManager = mediaStorageManager
        )
        updateManager = com.laterbox.data.update.UpdateManager(
            context = this,
            preferencesRepository = preferencesRepository
        )
        aiSettingsRepository = com.laterbox.data.ai.AiSettingsRepository(this)
        geminiAnalysisService = com.laterbox.data.ai.GeminiAnalysisService(
            context = this,
            savedItemDao = database.savedItemDao(),
            aiAnalysisDao = database.aiAnalysisDao(),
            aiSettingsRepository = aiSettingsRepository
        )

        // Schedule periodic update checks
        com.laterbox.data.update.AppUpdateWorker.schedulePeriodicCheck(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024L * 1024L) // 100MB disk cache for instant loads
                    .build()
            }
            .crossfade(200)
            .respectCacheHeaders(false) // Cache web thumbnails reliably
            .build()
    }

    companion object {
        lateinit var instance: LaterBoxApp private set
    }
}
