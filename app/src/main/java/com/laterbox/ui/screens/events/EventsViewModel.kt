package com.laterbox.ui.screens.events

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.ReminderEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import com.laterbox.data.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventsViewModel(application: Application) : AndroidViewModel(application) {

    private val reminderDao = LaterBoxApp.instance.database.reminderDao()
    private val contentRepository = LaterBoxApp.instance.contentRepository

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val allReminders: StateFlow<List<ReminderEntity>> = reminderDao.getAllUpcomingReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredReminders: StateFlow<List<ReminderEntity>> = combine(allReminders, _selectedFilter) { list, filter ->
        when (filter) {
            "BIRTHDAY" -> list.filter { it.eventType == "BIRTHDAY" }
            "ANNIVERSARY" -> list.filter { it.eventType == "ANNIVERSARY" }
            "REMINDER" -> list.filter { it.eventType == "REMINDER" }
            "EVENT" -> list.filter { it.eventType == "EVENT" }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun createEvent(
        title: String,
        dueAt: Long,
        eventType: String,
        personName: String?,
        recurrenceRule: String?,
        notifyBeforeMinutes: Int,
        note: String?,
        originalDate: Long? = null,
        relationship: String? = null,
        secondPersonName: String? = null,
        reminderOffsetsDaysJson: String? = null
    ) {
        viewModelScope.launch {
            // First create a unified memory in saved_items so this event is searchable!
            val memoryDescription = when {
                !relationship.isNullOrBlank() && !personName.isNullOrBlank() -> "$personName ($relationship)"
                !personName.isNullOrBlank() && !secondPersonName.isNullOrBlank() -> "$personName & $secondPersonName"
                !personName.isNullOrBlank() -> "Associated with $personName"
                else -> note
            }

            val memoryItem = SavedItemEntity(
                url = "local://event/${System.currentTimeMillis()}",
                title = title,
                description = memoryDescription,
                notes = note,
                contentType = ContentType.NOTE,
                captureMethod = "EVENT"
            )
            val contentId = contentRepository.saveItem(memoryItem)

            val reminder = ReminderEntity(
                contentId = contentId,
                dueAt = dueAt,
                eventTitle = title,
                eventType = eventType,
                personName = personName,
                relationship = relationship,
                secondPersonName = secondPersonName,
                originalDate = originalDate,
                reminderOffsetsDaysJson = reminderOffsetsDaysJson,
                recurrenceRule = recurrenceRule,
                notifyBeforeMinutes = notifyBeforeMinutes,
                note = note
            )
            val reminderId = reminderDao.insertReminder(reminder)
            val fullReminder = reminder.copy(id = reminderId)

            ReminderScheduler.scheduleReminder(getApplication(), fullReminder)
        }
    }

    fun markCompleted(reminder: ReminderEntity) {
        viewModelScope.launch {
            if (!reminder.recurrenceRule.isNullOrBlank() && reminder.recurrenceRule != "NONE") {
                val nextTime = ReminderScheduler.computeNextOccurrence(
                    currentDueAt = reminder.dueAt,
                    recurrenceRule = reminder.recurrenceRule,
                    originalDate = reminder.originalDate
                )
                val updated = reminder.copy(dueAt = nextTime, isCompleted = false, snoozedUntil = null)
                reminderDao.updateReminder(updated)
                ReminderScheduler.scheduleReminder(getApplication(), updated)
            } else {
                reminderDao.markCompleted(reminder.id)
                ReminderScheduler.cancelReminder(getApplication(), reminder.id)
            }
        }
    }

    fun snoozeReminder(reminder: ReminderEntity, minutes: Int = 60) {
        viewModelScope.launch {
            val snoozeUntil = System.currentTimeMillis() + (minutes * 60 * 1000L)
            reminderDao.snoozeReminder(reminder.id, snoozeUntil)
            ReminderScheduler.scheduleReminder(getApplication(), reminder.copy(snoozedUntil = snoozeUntil))
        }
    }

    fun deleteEvent(reminderId: Long) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(getApplication(), reminderId)
            reminderDao.deleteReminder(reminderId)
        }
    }
}
