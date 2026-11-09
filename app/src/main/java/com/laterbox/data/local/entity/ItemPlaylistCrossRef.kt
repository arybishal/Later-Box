package com.laterbox.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "item_playlist_cross_ref",
    primaryKeys = ["contentId", "playlistId"],
    foreignKeys = [
        ForeignKey(
            entity = SavedItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["contentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["contentId"]),
        Index(value = ["playlistId"])
    ]
)
data class ItemPlaylistCrossRef(
    val contentId: Long,
    val playlistId: Long,
    val addedAt: Long = System.currentTimeMillis()
)
