package com.laterbox.data.demo

data class DemoGenerationStats(
    val generalCount: Int = 0,
    val richNotesCount: Int = 0,
    val stressCount: Int = 0,
    val playlistsCreated: Int = 0,
    val tagsCreated: Int = 0,
    val remindersCreated: Int = 0,
    val mediaFilesCreated: Int = 0,
    val aiAnalysesCreated: Int = 0,
    val durationMs: Long = 0L
) {
    val totalItems: Int get() = generalCount + richNotesCount + stressCount
}

data class DemoDatabaseCounts(
    val totalDemoItems: Int = 0,
    val generalDemoItems: Int = 0,
    val richNotesDemoItems: Int = 0,
    val stressDemoItems: Int = 0,
    val realUserItems: Int = 0,
    val demoPlaylists: Int = 0,
    val demoReminders: Int = 0,
    val demoMediaFiles: Int = 0
)

data class DatabaseIntegrityStatus(
    val isHealthy: Boolean = true,
    val totalSavedItems: Int = 0,
    val demoSavedItems: Int = 0,
    val realSavedItems: Int = 0,
    val orphanedPlaylists: Int = 0,
    val orphanedReminders: Int = 0,
    val unreferencedMediaFiles: Int = 0,
    val message: String = "Database integrity verified"
)
