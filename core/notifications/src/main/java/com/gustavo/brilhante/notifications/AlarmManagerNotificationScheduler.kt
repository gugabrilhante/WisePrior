package com.gustavo.brilhante.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.gustavo.brilhante.model.RecurrenceType
import com.gustavo.brilhante.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AlarmScheduler"

@Singleton
class AlarmManagerNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    /**
     * Schedules an exact alarm for the given task.
     *
     * If the due date is in the past and the task is NOT recurring, the alarm is skipped.
     * If the task IS recurring and the due date is in the past, the next future occurrence is found
     * and scheduled instead.
     */
    override fun schedule(task: Task) {
        val rawDue = task.dueDate ?: return
        val now = System.currentTimeMillis()

        val scheduledTime = if (rawDue > now) {
            rawDue
        } else if (task.recurrenceType != RecurrenceType.NONE) {
            advanceToFuture(rawDue, task.recurrenceType, now)
        } else {
            Log.d(TAG, "Skipping past non-recurring task ${task.id}")
            return
        }

        val pendingIntent = buildPendingIntent(
            taskId = task.id,
            title = task.title,
            notes = task.notes,
            dueDate = scheduledTime,
            hasTime = task.hasTime,
            recurrenceType = task.recurrenceType
        )

        scheduleExact(scheduledTime, pendingIntent)
        Log.d(TAG, "Scheduled task ${task.id} at $scheduledTime (recurrence=${task.recurrenceType})")
    }

    override fun cancel(taskId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.requestCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Cancelled alarm for task $taskId")
    }

    /**
     * Called after device reboot. Reschedules all future tasks.
     * For recurring tasks whose due date is in the past (device was off), advances to the
     * next future occurrence so the reminder isn't permanently lost.
     */
    override fun rescheduleAll(tasks: List<Task>) {
        val now = System.currentTimeMillis()
        var scheduled = 0
        tasks.forEach { task ->
            val dueDate = task.dueDate ?: return@forEach
            when {
                dueDate > now -> {
                    schedule(task)
                    scheduled++
                }
                task.recurrenceType != RecurrenceType.NONE -> {
                    // Advance to next future occurrence — device was off for a while
                    schedule(task) // schedule() handles advancement internally
                    scheduled++
                }
                // Past non-recurring tasks: already missed, skip
            }
        }
        Log.d(TAG, "Rescheduled $scheduled alarms after reboot")
    }

    /**
     * Called from [AlarmReceiver] to schedule the next occurrence without constructing a full
     * [Task] domain object — avoids coupling the receiver to the domain layer.
     */
    fun scheduleFromReceiver(
        taskId: Long,
        title: String,
        notes: String,
        dueDate: Long,
        hasTime: Boolean,
        recurrenceType: RecurrenceType
    ) {
        val pendingIntent = buildPendingIntent(taskId, title, notes, dueDate, hasTime, recurrenceType)
        scheduleExact(dueDate, pendingIntent)
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private fun scheduleExact(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExact) {
            // setExactAndAllowWhileIdle fires even in Doze mode — required for reminders
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            // Fallback: inexact, still fires in Doze, but ±minutes window
            Log.w(TAG, "SCHEDULE_EXACT_ALARM not granted — using inexact fallback")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun buildPendingIntent(
        taskId: Long,
        title: String,
        notes: String,
        dueDate: Long,
        hasTime: Boolean,
        recurrenceType: RecurrenceType
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_TASK_NOTES, notes)
            putExtra(EXTRA_DUE_DATE, dueDate)
            putExtra(EXTRA_HAS_TIME, hasTime)
            putExtra(EXTRA_RECURRENCE, recurrenceType.name)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.requestCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Advances [dueDate] by the recurrence interval until it is in the future.
     * This handles cases where the device was off across multiple recurrence periods.
     */
    private fun advanceToFuture(dueDate: Long, type: RecurrenceType, now: Long): Long {
        var next = dueDate
        while (next <= now) {
            next = nextOccurrence(next, type)
        }
        return next
    }

    companion object {
        fun nextOccurrence(from: Long, type: RecurrenceType): Long {
            val cal = Calendar.getInstance().apply { timeInMillis = from }
            when (type) {
                RecurrenceType.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                RecurrenceType.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                RecurrenceType.MONTHLY -> cal.add(Calendar.MONTH, 1)
                RecurrenceType.NONE -> Unit
            }
            return cal.timeInMillis
        }
    }
}

// Unique PendingIntent request code per task — safe for IDs up to Int.MAX_VALUE
private fun Long.requestCode(): Int = (this and 0x7FFFFFFF).toInt()
