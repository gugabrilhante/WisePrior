package com.gustavo.brilhante.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent  // needed for onReceive parameter type
import android.util.Log
import com.gustavo.brilhante.model.RecurrenceType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "AlarmReceiver"

/**
 * Receives exact alarms from [AlarmManager] and:
 *  1. Shows the notification via [NotificationHelper]
 *  2. For recurring tasks, schedules the next occurrence via [AlarmManagerNotificationScheduler]
 *
 * All work is synchronous (AlarmManager scheduling is non-blocking), so [goAsync] is not needed.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper

    // Inject the concrete class to access scheduleNextOccurrence without exposing it on the interface
    @Inject lateinit var scheduler: AlarmManagerNotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: run {
            Log.w(TAG, "Missing title in alarm intent for task $taskId")
            return
        }
        val notes = intent.getStringExtra(EXTRA_TASK_NOTES) ?: ""
        val dueDate = intent.getLongExtra(EXTRA_DUE_DATE, -1L)
        val hasTime = intent.getBooleanExtra(EXTRA_HAS_TIME, false)
        val recurrenceRaw = intent.getStringExtra(EXTRA_RECURRENCE) ?: RecurrenceType.NONE.name

        if (taskId < 0L || dueDate < 0L) {
            Log.w(TAG, "Invalid alarm intent — taskId=$taskId dueDate=$dueDate")
            return
        }

        Log.d(TAG, "Alarm fired for task $taskId: $title")

        // Step 1: show the notification
        notificationHelper.showNotification(taskId, title, notes)

        // Step 2: reschedule next occurrence for recurring tasks
        val recurrenceType = runCatching { RecurrenceType.valueOf(recurrenceRaw) }
            .getOrDefault(RecurrenceType.NONE)

        if (recurrenceType != RecurrenceType.NONE) {
            // Advance from the ORIGINAL dueDate to keep time-of-day consistent (no drift)
            val nextDue = AlarmManagerNotificationScheduler.nextOccurrence(dueDate, recurrenceType)
            scheduler.scheduleFromReceiver(
                taskId = taskId,
                title = title,
                notes = notes,
                dueDate = nextDue,
                hasTime = hasTime,
                recurrenceType = recurrenceType
            )
            Log.d(TAG, "Rescheduled recurring task $taskId for $nextDue")
        }
    }
}
