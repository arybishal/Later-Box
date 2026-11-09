package com.laterbox.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    indices = [
        Index(value = ["name"]),
        Index(value = ["createdAt"])
    ]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val iconName: String? = "bookmark",
    val coverUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isDefaultInbox: Boolean = false
)
