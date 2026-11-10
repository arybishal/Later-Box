package com.laterbox.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.laterbox.data.model.ContentType
import com.laterbox.data.model.DownloadStatus

@Entity(
    tableName = "saved_items",
    indices = [
        Index(value = ["url"]),
        Index(value = ["title"]),
        Index(value = ["sourceDomain"]),
        Index(value = ["contentType"]),
        Index(value = ["savedAt"]),
        Index(value = ["lastOpenedAt"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isArchived"]),
        Index(value = ["isDeleted"]),
        Index(value = ["isSnap"])
    ]
)
data class SavedItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String,
    val description: String? = null,
    val sourceDomain: String? = null,
    val sourceApp: String? = null,
    val thumbnailUrl: String? = null,
    val localMediaPath: String? = null,
    val contentType: ContentType = ContentType.LINK,
    val mimeType: String? = null,
    val savedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val notes: String? = null,
    val readProgress: Float = 0f,
    val mediaSize: Long = 0L,
    val downloadStatus: DownloadStatus = DownloadStatus.NONE,
    val isSnap: Boolean = false,
    val aiTitle: String? = null,
    val aiDescription: String? = null,
    val captureMethod: String? = null,
    val extractedText: String? = null,
    val transcript: String? = null,
    val sourcePackage: String? = null
)
