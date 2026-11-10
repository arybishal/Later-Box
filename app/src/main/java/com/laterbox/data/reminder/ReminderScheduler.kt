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

    fun scheduleReminder(context: Context, reminder: ReminderEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Compute effective trigger time: dueAt minus notifyBeforeMinutes, or snoozedUntil
        val baseTime = reminder.snoozedUntil ?: reminder.dueAt
        val triggerTime = if (reminder.snoozedUntil == null && reminder.notifyBeforeMinutes > 0) {
            baseTime - (reminder.notifyBeforeMinutes * 60 * 1000L)
        } else {
            baseTime
        }

        // Only schedule future alarms
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_EVENT_TITLE, reminder.eventTitle ?: reminder.note ?: "Memory Reminder")
            putExtra(EXTRA_EVENT_TYPE, reminder.eventType)
            putExtra(EXTRA_CONTENT_ID, reminder.contentId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
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
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    suspend fun rescheduleAllActiveReminders(context: Context) = withContext(Dispatchers.IO) {
        val db = LaterBoxApp.instance.database
        val activeReminders = db.reminderDao().getAllRemindersSnapshot().filter { !it.isCompleted }
        for (reminder in activeReminders) {
            scheduleReminder(context, reminder)
        }
    }

    fun computeNextOccurrence(currentDueAt: Long, recurrenceRule: String?): Long {
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
