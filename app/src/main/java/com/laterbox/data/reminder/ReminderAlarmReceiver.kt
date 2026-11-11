package com.laterbox.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.laterbox.MainActivity
import com.laterbox.R
import com.laterbox.LaterBoxApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "later_box_reminders"
        const val CHANNEL_NAME = "Memory Reminders & Events"
        const val ACTION_MARK_DONE = "com.laterbox.reminder.MARK_DONE"
        const val ACTION_SNOOZE_1H = "com.laterbox.reminder.SNOOZE_1H"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val notificationId = intent.getIntExtra(ReminderScheduler.EXTRA_NOTIFICATION_ID, reminderId.toInt())
        val daysBefore = intent.getIntExtra(ReminderScheduler.EXTRA_DAYS_BEFORE, 0)

        when (intent.action) {
            ACTION_MARK_DONE -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(notificationId)
                notificationManager?.cancel(reminderId.toInt())

                // If this was an approaching reminder (daysBefore > 0), dismiss only this alert
                if (daysBefore > 0) return

                CoroutineScope(Dispatchers.IO).launch {
                    val db = LaterBoxApp.instance.database
                    val reminder = db.reminderDao().getAllRemindersSnapshot().find { it.id == reminderId }
                    if (reminder != null) {
                        if (!reminder.recurrenceRule.isNullOrBlank() && reminder.recurrenceRule != "NONE") {
                            val nextTime = ReminderScheduler.computeNextOccurrence(
                                currentDueAt = reminder.dueAt,
                                recurrenceRule = reminder.recurrenceRule,
                                originalDate = reminder.originalDate
                            )
                            val updated = reminder.copy(dueAt = nextTime, isCompleted = false, snoozedUntil = null)
                            db.reminderDao().updateReminder(updated)
                            ReminderScheduler.scheduleReminder(context, updated)
                        } else {
                            db.reminderDao().markCompleted(reminderId)
                        }
                    }
                }
                return
            }

            ACTION_SNOOZE_1H -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(notificationId)
                notificationManager?.cancel(reminderId.toInt())

                CoroutineScope(Dispatchers.IO).launch {
                    val db = LaterBoxApp.instance.database
                    val snoozeTime = System.currentTimeMillis() + (60 * 60 * 1000L) // 1 hour
                    db.reminderDao().snoozeReminder(reminderId, snoozeTime)
                    val reminder = db.reminderDao().getAllRemindersSnapshot().find { it.id == reminderId }
                    if (reminder != null) {
                        ReminderScheduler.scheduleReminder(context, reminder.copy(snoozedUntil = snoozeTime))
                    }
                }
                return
            }
        }

        val eventTitle = intent.getStringExtra(ReminderScheduler.EXTRA_EVENT_TITLE) ?: "Memory Reminder"
        val eventType = intent.getStringExtra(ReminderScheduler.EXTRA_EVENT_TYPE) ?: "REMINDER"
        val contentId = intent.getLongExtra(ReminderScheduler.EXTRA_CONTENT_ID, -1L)
        val personName = intent.getStringExtra(ReminderScheduler.EXTRA_PERSON_NAME)
        val secondPersonName = intent.getStringExtra(ReminderScheduler.EXTRA_SECOND_PERSON_NAME)
        val originalDate = intent.getLongExtra(ReminderScheduler.EXTRA_ORIGINAL_DATE, -1L)

        ensureNotificationChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (contentId != -1L) {
                putExtra("open_item_id", contentId)
            }
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark Done action
        val markDoneIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(ReminderScheduler.EXTRA_DAYS_BEFORE, daysBefore)
        }
        val markDonePending = PendingIntent.getBroadcast(
            context,
            notificationId + 100000,
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_1H
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(ReminderScheduler.EXTRA_DAYS_BEFORE, daysBefore)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context,
            notificationId + 200000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (notificationTitle, notificationText) = buildNotificationCopy(
            eventType = eventType,
            eventTitle = eventTitle,
            daysBefore = daysBefore,
            personName = personName,
            secondPersonName = secondPersonName,
            originalDate = originalDate
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .addAction(0, "Mark Done", markDonePending)
            .addAction(0, "Snooze 1h", snoozePending)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(notificationId, notification)
    }

    private fun buildNotificationCopy(
        eventType: String,
        eventTitle: String,
        daysBefore: Int,
        personName: String?,
        secondPersonName: String?,
        originalDate: Long
    ): Pair<String, String> {
        return when (eventType) {
            "BIRTHDAY" -> {
                val turningAge = if (originalDate > 0) {
                    EventRecurrenceCalculator.calculateNextOccurrence(originalDate).turningAgeOrYears
                } else null
                val name = personName?.ifBlank { null } ?: eventTitle
                val title = when (daysBefore) {
                    0 -> if (turningAge != null) "🎂 Today is $name's Birthday! (Turning $turningAge)" else "🎂 Today is $name's Birthday!"
                    1 -> if (turningAge != null) "🎂 $name's Birthday is Tomorrow! (Turning $turningAge)" else "🎂 $name's Birthday is Tomorrow!"
                    else -> if (turningAge != null) "🎂 $name's Birthday is in $daysBefore days (Turning $turningAge)" else "🎂 $name's Birthday is in $daysBefore days"
                }
                val text = when (daysBefore) {
                    0 -> "Don't forget to wish them a happy birthday! Tap to open saved memories."
                    1 -> "Get ready to celebrate! Tap to view saved ideas and memories."
                    else -> "Plan something special in advance. Tap to view saved ideas."
                }
                Pair(title, text)
            }
            "ANNIVERSARY" -> {
                val years = if (originalDate > 0) {
                    EventRecurrenceCalculator.calculateNextOccurrence(originalDate).turningAgeOrYears
                } else null
                val names = when {
                    !personName.isNullOrBlank() && !secondPersonName.isNullOrBlank() -> "$personName & $secondPersonName"
                    !personName.isNullOrBlank() -> personName
                    else -> eventTitle
                }
                val yearSuffix = if (years != null) " ($years Years)" else ""
                val title = when (daysBefore) {
                    0 -> if (years != null) "💍 Happy Anniversary, $names! ($years Years)" else "💍 Today is $names's Anniversary!"
                    1 -> "💍 $names's Anniversary is Tomorrow!$yearSuffix"
                    else -> "💍 $names's Anniversary is in $daysBefore days$yearSuffix"
                }
                val text = when (daysBefore) {
                    0 -> if (years != null) "Celebrating $years wonderful years today! Tap to open saved memories." else "Celebrating today! Tap to open saved memories."
                    1 -> "Celebration is tomorrow! Tap to open saved memories and plans."
                    else -> "Coming up in $daysBefore days. Tap to open saved memories and plans."
                }
                Pair(title, text)
            }
            "EVENT" -> {
                val title = when (daysBefore) {
                    0 -> "📅 Today: $eventTitle"
                    1 -> "📅 Tomorrow: $eventTitle"
                    else -> "📅 In $daysBefore days: $eventTitle"
                }
                Pair(title, "Tap to open your saved event memory")
            }
            else -> {
                val title = when (daysBefore) {
                    0 -> "⏰ Reminder: $eventTitle"
                    1 -> "⏰ Tomorrow: $eventTitle"
                    else -> "⏰ In $daysBefore days: $eventTitle"
                }
                Pair(title, "Tap to open your saved memory")
            }
        }
    }

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for birthdays, anniversaries, and scheduled reminders in Later Box"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
