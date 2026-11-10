package com.laterbox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.laterbox.data.local.entity.ReminderEntity
import com.laterbox.data.local.entity.SavedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)

    @Query("DELETE FROM reminders WHERE contentId = :contentId")
    suspend fun deleteReminderForContent(contentId: Long)

    @Query("SELECT * FROM reminders WHERE contentId = :contentId AND isCompleted = 0 ORDER BY dueAt ASC LIMIT 1")
    suspend fun getActiveReminderForContent(contentId: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE contentId = :contentId ORDER BY dueAt DESC LIMIT 1")
    fun getReminderFlowForContent(contentId: Long): Flow<ReminderEntity?>

    @Query("""
        SELECT * FROM reminders 
        WHERE isCompleted = 0 AND (dueAt <= :currentTime OR (snoozedUntil IS NOT NULL AND snoozedUntil <= :currentTime))
        ORDER BY dueAt ASC
    """)
    suspend fun getDueReminders(currentTime: Long = System.currentTimeMillis()): List<ReminderEntity>

    @Query("UPDATE reminders SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Query("UPDATE reminders SET snoozedUntil = :snoozeUntil WHERE id = :id")
    suspend fun snoozeReminder(id: Long, snoozeUntil: Long)

    @Query("""
        SELECT s.* FROM saved_items s
        INNER JOIN reminders r ON s.id = r.contentId
        WHERE s.isDeleted = 0 
          AND r.isCompleted = 0
          AND (r.dueAt <= :currentTime OR (r.snoozedUntil IS NOT NULL AND r.snoozedUntil <= :currentTime))
        ORDER BY r.dueAt ASC
    """)
    fun getItemsWithDueReminders(currentTime: Long = System.currentTimeMillis()): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT s.* FROM saved_items s
        INNER JOIN reminders r ON s.id = r.contentId
        WHERE s.isDeleted = 0 AND r.isCompleted = 0
        ORDER BY r.dueAt ASC
    """)
    fun getAllItemsWithPendingReminders(): Flow<List<SavedItemEntity>>

    @Query("""
        SELECT * FROM reminders 
        WHERE isCompleted = 0 
        ORDER BY dueAt ASC
    """)
    fun getAllUpcomingReminders(): Flow<List<ReminderEntity>>

    @Query("""
        SELECT * FROM reminders 
        WHERE isCompleted = 0 AND eventType = :eventType
        ORDER BY dueAt ASC
    """)
    fun getRemindersByType(eventType: String): Flow<List<ReminderEntity>>

    @Query("""
        SELECT * FROM reminders 
        WHERE isCompleted = 0 AND eventType IN ('BIRTHDAY', 'ANNIVERSARY')
        ORDER BY dueAt ASC
    """)
    fun getSpecialOccasions(): Flow<List<ReminderEntity>>

    @Query("""
        SELECT * FROM reminders 
        WHERE isCompleted = 0 
          AND (
            eventTitle LIKE '%' || :query || '%'
            OR personName LIKE '%' || :query || '%'
            OR eventType LIKE '%' || :query || '%'
          )
        ORDER BY dueAt ASC
    """)
    fun searchReminders(query: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders")
    suspend fun getAllRemindersSnapshot(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>): List<Long>

    @Query("DELETE FROM reminders")
    suspend fun clearAllReminders()
}
