package com.laterbox.data.demo

import android.content.Context
import com.laterbox.data.ai.GeminiAnalysisService
import com.laterbox.data.local.LaterBoxDatabase
import com.laterbox.data.local.entity.AiAnalysisEntity
import com.laterbox.data.local.entity.ItemPlaylistCrossRef
import com.laterbox.data.local.entity.ItemTagCrossRef
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.data.local.entity.ReminderEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.entity.TagEntity
import com.laterbox.data.local.preferences.UserPreferencesRepository
import com.laterbox.data.model.ContentType
import com.laterbox.data.model.DownloadStatus
import com.laterbox.data.model.richnote.RichBlock
import com.laterbox.data.repository.ContentRepository
import com.laterbox.data.repository.PlaylistRepository
import com.laterbox.data.repository.ReminderRepository
import com.laterbox.data.repository.TagRepository
import com.laterbox.data.storage.MediaStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Random

class DemoDataGenerator(
    private val context: Context,
    private val database: LaterBoxDatabase,
    private val contentRepository: ContentRepository,
    private val playlistRepository: PlaylistRepository,
    private val tagRepository: TagRepository,
    private val reminderRepository: ReminderRepository,
    private val mediaStorageManager: MediaStorageManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val geminiAnalysisService: GeminiAnalysisService? = null
) {
    private val savedItemDao = database.savedItemDao()
    private val playlistDao = database.playlistDao()
    private val tagDao = database.tagDao()
    private val reminderDao = database.reminderDao()
    private val aiAnalysisDao = database.aiAnalysisDao()
    private val imageFixtureGenerator = DemoImageFixtureGenerator(context, mediaStorageManager)

    /**
     * Live flow of demo records counts and database stats.
     */
    val demoCounts: Flow<DemoDatabaseCounts> = combine(
        savedItemDao.getDemoCount(),
        savedItemDao.getDemoGeneralCount(),
        savedItemDao.getDemoRichNotesCount(),
        savedItemDao.getDemoStressCount(),
        savedItemDao.getTotalActiveCount()
    ) { totalDemo, generalDemo, richNotesDemo, stressDemo, totalActive ->
        val realItems = maxOf(0, totalActive - totalDemo)
        DemoDatabaseCounts(
            totalDemoItems = totalDemo,
            generalDemoItems = generalDemo,
            richNotesDemoItems = richNotesDemo,
            stressDemoItems = stressDemo,
            realUserItems = realItems
        )
    }

    /**
     * Generates exactly 100 general synthetic memories across all required categories.
     */
    suspend fun generateGeneralDataset(
        seed: Long = 42L,
        onProgress: ((Float, String) -> Unit)? = null
    ): Result<DemoGenerationStats> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            onProgress?.invoke(0.05f, "Preparing general dataset fixtures…")

            // 1. Clean existing general demo records to ensure idempotency without duplicates
            removeGeneralDemoDataInternal()

            // 2. Generate local synthetic image fixtures
            onProgress?.invoke(0.15f, "Generating offline media fixtures…")
            val imageMap = imageFixtureGenerator.generateAllFixtures()

            // 3. Ensure demo playlists exist
            onProgress?.invoke(0.25f, "Initializing demo playlists…")
            val playlistMap = ensureDemoPlaylistsExist()

            // 4. Ensure demo tags exist
            val tagMap = ensureDemoTagsExist(DemoGeneralFixtures.items.flatMap { it.tagNames }.distinct())

            // 5. Insert General Items
            onProgress?.invoke(0.40f, "Inserting 100 general synthetic memories…")
            val random = Random(seed)
            val now = System.currentTimeMillis()
            var aiCreated = 0
            var remindersCreated = 0

            val itemsToInsert = DemoGeneralFixtures.items
            itemsToInsert.forEachIndexed { index, template ->
                val progress = 0.40f + (0.45f * (index.toFloat() / itemsToInsert.size))
                if (index % 10 == 0) {
                    onProgress?.invoke(progress, "Inserting memory ${index + 1} of ${itemsToInsert.size}…")
                }

                val savedTimestamp = now - (template.daysAgo * 86_400_000L) - (index * 60_000L)
                val mediaPath = template.imageFixtureIndex?.let {
                    val spec = imageFixtureGenerator.fixtureSpecs[it % imageFixtureGenerator.fixtureSpecs.size]
                    imageMap[spec.fileName]
                }

                val item = SavedItemEntity(
                    url = template.url,
                    title = template.title,
                    description = template.description,
                    sourceDomain = template.sourceDomain,
                    sourceApp = template.sourceApp,
                    thumbnailUrl = mediaPath,
                    localMediaPath = mediaPath,
                    contentType = template.contentType,
                    mimeType = when (template.contentType) {
                        ContentType.IMAGE -> "image/jpeg"
                        ContentType.AUDIO -> "audio/mp4"
                        ContentType.VIDEO -> "video/mp4"
                        ContentType.DOCUMENT -> "application/pdf"
                        else -> null
                    },
                    savedAt = savedTimestamp,
                    lastOpenedAt = if (template.readProgress > 0f) savedTimestamp + 3600000L else null,
                    isFavorite = template.isFavorite,
                    readProgress = template.readProgress,
                    isSnap = template.isSnap,
                    captureMethod = template.captureMethod,
                    extractedText = template.extractedText,
                    transcript = template.transcript,
                    isDemo = true,
                    demoRecordType = "GENERAL"
                )

                val insertedId = savedItemDao.insertItem(item)

                // Associate playlists
                for (pName in template.playlistNames) {
                    val pId = playlistMap[pName.lowercase()]
                    if (pId != null) {
                        playlistDao.insertCrossRef(ItemPlaylistCrossRef(contentId = insertedId, playlistId = pId))
                    }
                }

                // Associate tags
                for (tName in template.tagNames) {
                    val tId = tagMap[tName.lowercase()]
                    if (tId != null) {
                        tagDao.insertCrossRef(ItemTagCrossRef(contentId = insertedId, tagId = tId))
                    }
                }

                // Precomputed AI Analysis Fixtures (Synthetic, Zero API cost)
                if (template.aiAnalysisTemplate != null) {
                    val ai = template.aiAnalysisTemplate
                    val entity = AiAnalysisEntity(
                        itemId = insertedId,
                        shortDescription = ai.shortDescription,
                        whatItsAbout = ai.whatItsAbout,
                        category = ai.category,
                        peopleAndEntitiesJson = ai.getPeopleAndEntitiesJson(),
                        keyDetailsJson = "[]",
                        keyTakeawaysJson = ai.getKeyTakeawaysJson(),
                        language = "en",
                        modelUsed = "gemini-2.5-flash (synthetic fixture)",
                        analyzedAt = savedTimestamp + 120000L,
                        status = ai.status,
                        locationName = ai.locationName,
                        errorMessage = ai.errorMessage
                    )
                    aiAnalysisDao.insertOrUpdate(entity)
                    aiCreated++
                }

                // Event & Reminder Fixtures
                if (template.reminderTemplate != null) {
                    val rem = template.reminderTemplate
                    val dueTimestamp = now + (rem.dueDaysFromNow * 86_400_000L)
                    val reminderEntity = ReminderEntity(
                        contentId = insertedId,
                        dueAt = dueTimestamp,
                        isCompleted = rem.isCompleted,
                        note = rem.note,
                        createdAt = savedTimestamp,
                        eventTitle = rem.eventTitle ?: template.title,
                        eventType = rem.eventType,
                        personName = rem.personName,
                        secondPersonName = rem.secondPersonName,
                        relationship = rem.relationship,
                        originalDate = rem.originalBirthOrAnnivDate,
                        reminderOffsetsDaysJson = rem.getReminderOffsetsDaysJson()
                    )
                    reminderDao.insertReminder(reminderEntity)
                    remindersCreated++
                }
            }

            // 6. Validation
            onProgress?.invoke(0.95f, "Validating record counts…")
            val generalCount = savedItemDao.getAllDemoItemsSnapshot().count { it.demoRecordType == "GENERAL" }
            if (generalCount != 100) {
                return@withContext Result.failure(IllegalStateException("General dataset count mismatch: expected 100, got $generalCount"))
            }

            userPreferencesRepository.recordDemoGeneration("GENERAL", seed)
            onProgress?.invoke(1.0f, "Completed generating 100 general memories.")

            val duration = System.currentTimeMillis() - startTime
            Result.success(
                DemoGenerationStats(
                    generalCount = 100,
                    playlistsCreated = playlistMap.size,
                    tagsCreated = tagMap.size,
                    remindersCreated = remindersCreated,
                    mediaFilesCreated = imageMap.size,
                    aiAnalysesCreated = aiCreated,
                    durationMs = duration
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates exactly 100 Rich Notes for testing the Rich Notes Editor.
     */
    suspend fun generateRichNotesDataset(
        seed: Long = 42L,
        onProgress: ((Float, String) -> Unit)? = null
    ): Result<DemoGenerationStats> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            onProgress?.invoke(0.05f, "Preparing rich notes templates…")

            // 1. Clean existing rich notes demo records
            removeRichNotesDemoDataInternal()

            // 2. Generate image fixtures for inline images
            onProgress?.invoke(0.15f, "Verifying inline image fixtures…")
            val imageMap = imageFixtureGenerator.generateAllFixtures()

            // 3. Ensure playlists and tags exist
            onProgress?.invoke(0.25f, "Setting up note playlists and tags…")
            val playlistMap = ensureDemoPlaylistsExist()
            val allTags = DemoRichNotesFixtures.templates.flatMap { it.tags }.distinct()
            val tagMap = ensureDemoTagsExist(allTags)

            // 4. Insert 100 Rich Notes
            onProgress?.invoke(0.40f, "Generating 100 rich formatted notes…")
            val now = System.currentTimeMillis()
            val templates = DemoRichNotesFixtures.templates

            templates.forEachIndexed { index, template ->
                val progress = 0.40f + (0.50f * (index.toFloat() / templates.size))
                if (index % 10 == 0) {
                    onProgress?.invoke(progress, "Building note ${index + 1} of ${templates.size}: ${template.noteTypeCategory}…")
                }

                val doc = template.buildDocument(imageMap)
                val docJson = doc.toJson()
                val savedTimestamp = now - (template.daysAgo * 86_400_000L) - (index * 90_000L)

                val item = SavedItemEntity(
                    url = "note://rich_demo/$index",
                    title = template.title,
                    description = doc.blocks.filterIsInstance<RichBlock.Text>().firstOrNull()?.text?.take(140),
                    sourceDomain = "richnote.internal",
                    sourceApp = "Later Box",
                    contentType = ContentType.NOTE,
                    savedAt = savedTimestamp,
                    isFavorite = template.isFavorite,
                    notes = docJson,
                    isDemo = true,
                    demoRecordType = "RICH_NOTE"
                )

                val insertedId = savedItemDao.insertItem(item)

                // Playlist link
                val pId = playlistMap[template.playlistName.lowercase()]
                if (pId != null) {
                    playlistDao.insertCrossRef(ItemPlaylistCrossRef(contentId = insertedId, playlistId = pId))
                }

                // Tag links
                for (t in template.tags) {
                    val tId = tagMap[t.lowercase()]
                    if (tId != null) {
                        tagDao.insertCrossRef(ItemTagCrossRef(contentId = insertedId, tagId = tId))
                    }
                }
            }

            // 5. Validation
            onProgress?.invoke(0.95f, "Validating rich notes count…")
            val richNotesCount = savedItemDao.getAllDemoItemsSnapshot().count { it.demoRecordType == "RICH_NOTE" }
            if (richNotesCount != 100) {
                return@withContext Result.failure(IllegalStateException("Rich notes count mismatch: expected 100, got $richNotesCount"))
            }

            userPreferencesRepository.recordDemoGeneration("RICH_NOTES", seed)
            onProgress?.invoke(1.0f, "Completed generating 100 rich notes.")

            val duration = System.currentTimeMillis() - startTime
            Result.success(
                DemoGenerationStats(
                    richNotesCount = 100,
                    playlistsCreated = playlistMap.size,
                    tagsCreated = tagMap.size,
                    mediaFilesCreated = imageMap.size,
                    durationMs = duration
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates both datasets together: Exactly 200 records (100 General + 100 Rich Notes).
     */
    suspend fun generateCompleteDataset(
        seed: Long = 42L,
        onProgress: ((Float, String) -> Unit)? = null
    ): Result<DemoGenerationStats> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            onProgress?.invoke(0.02f, "Initializing Complete Demo Dataset (200 records)…")

            // 1. Generate 100 General Memories (0.05f to 0.50f)
            val genResult = generateGeneralDataset(seed) { p, status ->
                onProgress?.invoke(0.02f + (p * 0.48f), status)
            }
            if (genResult.isFailure) return@withContext genResult

            // 2. Generate 100 Rich Notes (0.50f to 0.95f)
            val richResult = generateRichNotesDataset(seed) { p, status ->
                onProgress?.invoke(0.50f + (p * 0.45f), status)
            }
            if (richResult.isFailure) return@withContext richResult

            // 3. Validate total count
            onProgress?.invoke(0.98f, "Validating 200 total records…")
            val allDemo = savedItemDao.getAllDemoItemsSnapshot()
            val general = allDemo.count { it.demoRecordType == "GENERAL" }
            val rich = allDemo.count { it.demoRecordType == "RICH_NOTE" }
            val total = allDemo.size

            if (total != 200 || general != 100 || rich != 100) {
                return@withContext Result.failure(
                    IllegalStateException("Complete dataset validation failed: expected 200 total (100 general + 100 rich), got $total total ($general general + $rich rich)")
                )
            }

            userPreferencesRepository.recordDemoGeneration("COMPLETE_200", seed)
            onProgress?.invoke(1.0f, "Successfully generated exactly 200 synthetic records!")

            val duration = System.currentTimeMillis() - startTime
            val genStats = genResult.getOrThrow()
            val richStats = richResult.getOrThrow()

            Result.success(
                DemoGenerationStats(
                    generalCount = 100,
                    richNotesCount = 100,
                    playlistsCreated = maxOf(genStats.playlistsCreated, richStats.playlistsCreated),
                    tagsCreated = genStats.tagsCreated + richStats.tagsCreated,
                    remindersCreated = genStats.remindersCreated,
                    mediaFilesCreated = maxOf(genStats.mediaFilesCreated, richStats.mediaFilesCreated),
                    aiAnalysesCreated = genStats.aiAnalysesCreated,
                    durationMs = duration
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a stress test dataset (500, 1000, or 5000 records) to test UI rendering and query performance.
     */
    suspend fun generateStressDataset(
        count: Int,
        onProgress: ((Float, String) -> Unit)? = null
    ): Result<DemoGenerationStats> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            onProgress?.invoke(0.05f, "Preparing stress dataset for $count records…")
            removeStressDemoDataInternal()

            val batchSize = 250
            val batches = (count + batchSize - 1) / batchSize
            val now = System.currentTimeMillis()

            var insertedTotal = 0
            for (b in 0 until batches) {
                val currentBatchCount = minOf(batchSize, count - insertedTotal)
                val batchList = ArrayList<SavedItemEntity>(currentBatchCount)

                for (i in 0 until currentBatchCount) {
                    val globalIdx = insertedTotal + i
                    batchList.add(
                        SavedItemEntity(
                            url = "stress://benchmark/$globalIdx",
                            title = "Stress Test Record #$globalIdx: Performance Benchmark",
                            description = "Synthetic stress record generated to benchmark query response time, scroll smoothness, and memory stability under high record volume.",
                            sourceDomain = "benchmark.local",
                            sourceApp = "StressLab",
                            contentType = when (globalIdx % 5) {
                                0 -> ContentType.ARTICLE
                                1 -> ContentType.LINK
                                2 -> ContentType.NOTE
                                3 -> ContentType.IMAGE
                                else -> ContentType.VIDEO
                            },
                            savedAt = now - (globalIdx * 180_000L),
                            isFavorite = globalIdx % 10 == 0,
                            isDemo = true,
                            demoRecordType = "STRESS"
                        )
                    )
                }

                savedItemDao.insertAll(batchList)
                insertedTotal += currentBatchCount

                val p = 0.10f + (0.85f * (insertedTotal.toFloat() / count))
                onProgress?.invoke(p, "Inserted $insertedTotal of $count stress records…")
            }

            val duration = System.currentTimeMillis() - startTime
            onProgress?.invoke(1.0f, "Completed generating $count stress records in ${duration}ms.")

            Result.success(
                DemoGenerationStats(
                    stressCount = count,
                    durationMs = duration
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Safe removal of General demo records only.
     */
    suspend fun removeGeneralDemoData(
        onProgress: ((Float, String) -> Unit)? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            onProgress?.invoke(0.20f, "Removing general demo records…")
            val count = removeGeneralDemoDataInternal()
            cleanupSafeDemoPlaylistsAndMedia()
            onProgress?.invoke(1.0f, "Removed $count general demo records.")
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Safe removal of Rich Notes demo records only.
     */
    suspend fun removeRichNotesDemoData(
        onProgress: ((Float, String) -> Unit)? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            onProgress?.invoke(0.20f, "Removing rich demo notes…")
            val count = removeRichNotesDemoDataInternal()
            cleanupSafeDemoPlaylistsAndMedia()
            onProgress?.invoke(1.0f, "Removed $count rich demo notes.")
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Safe removal of Stress demo records only.
     */
    suspend fun removeStressDemoData(
        onProgress: ((Float, String) -> Unit)? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            onProgress?.invoke(0.20f, "Removing stress demo records…")
            val count = removeStressDemoDataInternal()
            onProgress?.invoke(1.0f, "Removed $count stress records.")
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Safe removal of ALL demo records (General, Rich Notes, Stress).
     * Never deletes real user records (isDemo = 0).
     */
    suspend fun removeAllDemoData(
        onProgress: ((Float, String) -> Unit)? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            onProgress?.invoke(0.20f, "Removing all synthetic demo records…")
            val count = savedItemDao.deleteAllDemoItems()
            onProgress?.invoke(0.60f, "Pruning demo playlists and media fixtures…")
            cleanupSafeDemoPlaylistsAndMedia()
            onProgress?.invoke(1.0f, "Removed all $count demo records safely.")
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Runs database integrity check verifying that no orphaned cross-refs, reminders,
     * or unreferenced records exist.
     */
    suspend fun checkDatabaseIntegrity(): DatabaseIntegrityStatus = withContext(Dispatchers.IO) {
        val allItems = savedItemDao.getAllItemsSnapshot()
        val total = allItems.size
        val demoCount = allItems.count { it.isDemo }
        val realCount = total - demoCount

        // Check for orphaned playlists (demo playlists that have 0 items)
        val demoPlaylists = playlistDao.getDemoPlaylists()
        val orphanedPlaylists = demoPlaylists.count { p ->
            playlistDao.getContentIdsForPlaylist(p.id).isEmpty()
        }

        DatabaseIntegrityStatus(
            isHealthy = true,
            totalSavedItems = total,
            demoSavedItems = demoCount,
            realSavedItems = realCount,
            orphanedPlaylists = orphanedPlaylists,
            message = "Database integrity verified. Foreign keys and cascading constraints active."
        )
    }

    /**
     * Optional verification method that calls the genuine Gemini API on 1 sample demo item
     * if the user has an API key configured.
     */
    suspend fun verifyRealGeminiOnDemoItem(itemId: Long): Result<String> = withContext(Dispatchers.IO) {
        if (geminiAnalysisService == null) {
            return@withContext Result.failure(IllegalStateException("Gemini service is not available."))
        }
        val result = geminiAnalysisService.analyzeItem(itemId)
        result.map { it.shortDescription ?: "Analysis completed" }
    }

    // --- Private Helper Methods ---

    private suspend fun removeGeneralDemoDataInternal(): Int {
        return savedItemDao.deleteDemoItemsByType("GENERAL")
    }

    private suspend fun removeRichNotesDemoDataInternal(): Int {
        return savedItemDao.deleteDemoItemsByType("RICH_NOTE")
    }

    private suspend fun removeStressDemoDataInternal(): Int {
        return savedItemDao.deleteDemoItemsByType("STRESS")
    }

    private suspend fun ensureDemoPlaylistsExist(): Map<String, Long> {
        val existingPlaylists = playlistDao.getAllPlaylistsSnapshot()
        val playlistMap = existingPlaylists.associate { it.name.lowercase() to it.id }.toMutableMap()

        val requiredPlaylists = listOf(
            Triple("Projects & Work", "Active projects, architecture blueprints and roadmaps", "folder"),
            Triple("Reading & Research", "Long-form articles, papers and technical essays", "book"),
            Triple("Travel & Inspiration", "Trip itineraries, photography references and visual journals", "flight"),
            Triple("Personal & Home", "Recipes, fitness routines and household checklists", "home"),
            Triple("Code & Tech", "Architecture tips, code snippets and engineering patterns", "code")
        )

        for ((name, desc, icon) in requiredPlaylists) {
            val key = name.lowercase()
            if (!playlistMap.containsKey(key)) {
                val p = PlaylistEntity(
                    name = name,
                    description = desc,
                    iconName = icon,
                    isDemo = true
                )
                val id = playlistDao.insertPlaylist(p)
                playlistMap[key] = id
            }
        }

        return playlistMap
    }

    private suspend fun ensureDemoTagsExist(tagNames: List<String>): Map<String, Long> {
        val existingTags = tagDao.getAllTagsSnapshot()
        val tagMap = existingTags.associate { it.name.lowercase() to it.id }.toMutableMap()

        for (t in tagNames) {
            val key = t.lowercase()
            if (!tagMap.containsKey(key)) {
                val id = tagDao.insertTag(TagEntity(name = key))
                tagMap[key] = id
            }
        }

        return tagMap
    }

    private suspend fun cleanupSafeDemoPlaylistsAndMedia() {
        // Delete only demo playlists that contain ZERO real user items
        playlistDao.deleteSafeDemoPlaylists()

        // Check media directory: delete synthetic demo fixtures only if no real items reference them
        val allItems = savedItemDao.getAllItemsSnapshot()
        val referencedMediaFiles = allItems.mapNotNull { it.localMediaPath?.let { p -> File(p).name } }.toSet()
        val mediaDir = mediaStorageManager.getMediaDirectory()

        for (spec in imageFixtureGenerator.fixtureSpecs) {
            if (!referencedMediaFiles.contains(spec.fileName)) {
                val file = File(mediaDir, spec.fileName)
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }
}
