package com.laterbox.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = SavedItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["contentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["contentId"]),
        Index(value = ["dueAt"]),
        Index(value = ["isCompleted"]),
        Index(value = ["eventType"])
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contentId: Long,
    val dueAt: Long,
    val isCompleted: Boolean = false,
    val snoozedUntil: Long? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val eventTitle: String? = null,
    val eventType: String = "REMINDER",
    val personName: String? = null,
    val recurrenceRule: String? = null,
    val notifyBeforeMinutes: Int = 0
)
