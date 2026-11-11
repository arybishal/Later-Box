package com.laterbox.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.ReminderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

object ReminderScheduler {

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_EVENT_TITLE = "extra_event_title"
    const val EXTRA_EVENT_TYPE = "extra_event_type"
    const val EXTRA_CONTENT_ID = "extra_content_id"
    const val EXTRA_DAYS_BEFORE = "extra_days_before"
    const val EXTRA_PERSON_NAME = "extra_person_name"
    const val EXTRA_SECOND_PERSON_NAME = "extra_second_person_name"
    const val EXTRA_ORIGINAL_DATE = "extra_original_date"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    fun scheduleReminder(context: Context, reminder: ReminderEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Compute effective base target time
        val targetTime = if (reminder.originalDate != null &&
            (reminder.eventType == "BIRTHDAY" || reminder.eventType == "ANNIVERSARY")
        ) {
            EventRecurrenceCalculator.calculateNextOccurrence(reminder.originalDate).nextOccurrenceEpochMs
        } else {
            reminder.dueAt
        }

        // If snoozed, schedule only the single snoozed alert
        if (reminder.snoozedUntil != null) {
            if (reminder.snoozedUntil > System.currentTimeMillis()) {
                scheduleSingleAlarm(
                    context = context,
                    alarmManager = alarmManager,
                    reminder = reminder,
                    triggerTime = reminder.snoozedUntil,
                    offsetDays = 0,
                    targetTime = targetTime
                )
            }
            return
        }

        // Multiple approaching notifications support
        val offsets = reminder.getReminderOffsetsDays()

        if (reminder.eventType == "BIRTHDAY" || reminder.eventType == "ANNIVERSARY") {
            for (offset in offsets) {
                val triggerTime = targetTime - (offset * 24L * 60L * 60L * 1000L)
                if (triggerTime > System.currentTimeMillis()) {
                    scheduleSingleAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        reminder = reminder,
                        triggerTime = triggerTime,
                        offsetDays = offset,
                        targetTime = targetTime
                    )
                }
            }
        } else {
            // General Reminders and Events
            for (offset in offsets) {
                val triggerTime = if (offset == 0) {
                    if (reminder.notifyBeforeMinutes > 0) {
                        targetTime - (reminder.notifyBeforeMinutes * 60 * 1000L)
                    } else {
                        targetTime
                    }
                } else {
                    targetTime - (offset * 24L * 60L * 60L * 1000L)
                }

                if (triggerTime > System.currentTimeMillis()) {
                    scheduleSingleAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        reminder = reminder,
                        triggerTime = triggerTime,
                        offsetDays = offset,
                        targetTime = targetTime
                    )
                }
            }
        }
    }

    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        reminder: ReminderEntity,
        triggerTime: Long,
        offsetDays: Int,
        targetTime: Long
    ) {
        val reqCode = (reminder.id * 100 + offsetDays).toInt()

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_EVENT_TITLE, reminder.eventTitle ?: reminder.note ?: "Memory Reminder")
            putExtra(EXTRA_EVENT_TYPE, reminder.eventType)
            putExtra(EXTRA_CONTENT_ID, reminder.contentId)
            putExtra(EXTRA_DAYS_BEFORE, offsetDays)
            putExtra(EXTRA_PERSON_NAME, reminder.personName)
            putExtra(EXTRA_SECOND_PERSON_NAME, reminder.secondPersonName)
            putExtra(EXTRA_ORIGINAL_DATE, reminder.originalDate ?: -1L)
            putExtra(EXTRA_NOTIFICATION_ID, reqCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Cancel all standard approaching notification offsets
        val standardOffsets = listOf(0, 1, 2, 3, 7, 14, 30)
        for (offset in standardOffsets) {
            val reqCode = (reminderId * 100 + offset).toInt()
            val intent = Intent(context, ReminderAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reqCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }

        // Cancel legacy single-alarm requestCode
        val legacyIntent = Intent(context, ReminderAlarmReceiver::class.java)
        val legacyPending = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            legacyIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (legacyPending != null) {
            alarmManager.cancel(legacyPending)
            legacyPending.cancel()
        }
    }

    suspend fun rescheduleAllActiveReminders(context: Context) = withContext(Dispatchers.IO) {
        val db = LaterBoxApp.instance.database
        val activeReminders = db.reminderDao().getAllRemindersSnapshot().filter { !it.isCompleted }
        for (reminder in activeReminders) {
            scheduleReminder(context, reminder)
        }
    }

    fun computeNextOccurrence(currentDueAt: Long, recurrenceRule: String?, originalDate: Long? = null): Long {
        if (originalDate != null && recurrenceRule?.uppercase() == "YEARLY") {
            val nextResult = EventRecurrenceCalculator.calculateNextOccurrence(
                originalDateMs = originalDate,
                referenceTimeMs = System.currentTimeMillis() + 86400000L // from tomorrow onwards
            )
            return nextResult.nextOccurrenceEpochMs
        }

        val cal = Calendar.getInstance().apply { timeInMillis = currentDueAt }
        return when (recurrenceRule?.uppercase()) {
            "DAILY" -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.timeInMillis
            }
            "WEEKLY" -> {
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                cal.timeInMillis
            }
            "MONTHLY" -> {
                cal.add(Calendar.MONTH, 1)
                cal.timeInMillis
            }
            "YEARLY" -> {
                cal.add(Calendar.YEAR, 1)
                cal.timeInMillis
            }
            else -> currentDueAt
        }
    }
}
