package com.laterbox.data.repository

import com.laterbox.data.local.dao.ReminderDao
import com.laterbox.data.local.entity.ReminderEntity
import com.laterbox.data.local.entity.SavedItemEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val reminderDao: ReminderDao
) {

    fun getReminderFlowForContent(contentId: Long): Flow<ReminderEntity?> =
        reminderDao.getReminderFlowForContent(contentId)

    suspend fun getActiveReminderForContent(contentId: Long): ReminderEntity? =
        reminderDao.getActiveReminderForContent(contentId)

    fun getItemsWithDueReminders(currentTime: Long = System.currentTimeMillis()): Flow<List<SavedItemEntity>> =
        reminderDao.getItemsWithDueReminders(currentTime)

    fun getAllItemsWithPendingReminders(): Flow<List<SavedItemEntity>> =
        reminderDao.getAllItemsWithPendingReminders()

    suspend fun scheduleReminder(contentId: Long, dueAt: Long, note: String? = null): Long {
        reminderDao.deleteReminderForContent(contentId)
        val reminder = ReminderEntity(
            contentId = contentId,
            dueAt = dueAt,
            note = note?.trim()?.ifEmpty { null }
        )
        return reminderDao.insertReminder(reminder)
    }

    suspend fun snoozeReminder(reminderId: Long, snoozeUntil: Long) {
        reminderDao.snoozeReminder(reminderId, snoozeUntil)
    }

    suspend fun markCompleted(reminderId: Long) {
        reminderDao.markCompleted(reminderId)
    }

    suspend fun cancelReminder(reminderId: Long) {
        reminderDao.deleteReminder(reminderId)
    }

    suspend fun cancelReminderForContent(contentId: Long) {
        reminderDao.deleteReminderForContent(contentId)
    }
}
