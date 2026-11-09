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
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var instance: LaterBoxApp private set
    }
}
