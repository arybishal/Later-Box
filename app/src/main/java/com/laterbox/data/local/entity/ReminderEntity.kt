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
    val notifyBeforeMinutes: Int = 0,
    val originalDate: Long? = null,
    val relationship: String? = null,
    val secondPersonName: String? = null,
    val reminderOffsetsDaysJson: String? = null
) {
    fun getReminderOffsetsDays(): List<Int> {
        val json = reminderOffsetsDaysJson
        if (json.isNullOrBlank()) {
            return if (eventType == "BIRTHDAY" || eventType == "ANNIVERSARY") {
                listOf(30, 7, 3, 1, 0)
            } else {
                listOf(0)
            }
        }
        return try {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).map { arr.getInt(it) }
        } catch (_: Exception) {
            listOf(0)
        }
    }
}
