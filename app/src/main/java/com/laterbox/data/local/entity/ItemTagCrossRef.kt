package com.laterbox.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "item_tag_cross_ref",
    primaryKeys = ["contentId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = SavedItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["contentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["contentId"]),
        Index(value = ["tagId"])
    ]
)
data class ItemTagCrossRef(
    val contentId: Long,
    val tagId: Long,
    val addedAt: Long = System.currentTimeMillis()
)
