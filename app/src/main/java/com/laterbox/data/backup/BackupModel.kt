package com.laterbox.data.backup

enum class ImportMode {
    REPLACE, // Restore as primary library, replacing existing data
    MERGE    // Merge with existing library, avoiding duplicates and preserving associations
}

data class BackupManifest(
    val schemaVersion: Int = 1,
    val appVersion: String,
    val appVersionCode: Int,
    val createdAt: Long,
    val totalItems: Int,
    val totalPlaylists: Int,
    val totalMediaFiles: Int,
    val estimatedBytes: Long,
    val sha256Checksum: String
)

data class BackupSummary(
    val totalItems: Int,
    val totalPlaylists: Int,
    val totalMediaFiles: Int,
    val estimatedSizeBytes: Long
)

data class ImportResult(
    val itemsRestored: Int,
    val itemsSkipped: Int,
    val playlistsRestored: Int,
    val mediaFilesRestored: Int,
    val mode: ImportMode,
    val warnings: List<String> = emptyList()
)
