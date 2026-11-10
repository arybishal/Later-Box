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

        when (intent.action) {
            ACTION_MARK_DONE -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(reminderId.toInt())
                CoroutineScope(Dispatchers.IO).launch {
                    val db = LaterBoxApp.instance.database
                    val reminder = db.reminderDao().getAllRemindersSnapshot().find { it.id == reminderId }
                    if (reminder != null) {
                        if (!reminder.recurrenceRule.isNullOrBlank() && reminder.recurrenceRule != "NONE") {
                            val nextTime = ReminderScheduler.computeNextOccurrence(reminder.dueAt, reminder.recurrenceRule)
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

        ensureNotificationChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (contentId != -1L) {
                putExtra("open_item_id", contentId)
            }
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark Done action
        val markDoneIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
        }
        val markDonePending = PendingIntent.getBroadcast(
            context,
            (reminderId + 100000).toInt(),
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_1H
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context,
            (reminderId + 200000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emojiPrefix = when (eventType) {
            "BIRTHDAY" -> "🎂 Birthday: "
            "ANNIVERSARY" -> "💍 Anniversary: "
            "EVENT" -> "📅 Event: "
            else -> "⏰ Reminder: "
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$emojiPrefix$eventTitle")
            .setContentText("Tap to open your saved memory")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .addAction(0, "Mark Done", markDonePending)
            .addAction(0, "Snooze 1h", snoozePending)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(reminderId.toInt(), notification)
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
