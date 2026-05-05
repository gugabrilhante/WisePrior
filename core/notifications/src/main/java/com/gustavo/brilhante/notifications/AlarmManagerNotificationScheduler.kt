package com.gustavo.brilhante.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
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
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(task: Task) {
        val rawDue = task.dueDate ?: return
        val now = System.currentTimeMillis()

        val scheduledTime = if (rawDue > now) {
            rawDue
        } else if (task.recurrenceRule.isRecurring) {
            advanceToFuture(rawDue, task.recurrenceRule, now)
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
            recurrenceRule = task.recurrenceRule
        )

        scheduleExact(scheduledTime, pendingIntent)
        Log.d(TAG, "Scheduled task ${task.id} at $scheduledTime (recurrence=${task.recurrenceRule})")
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
                task.recurrenceRule.isRecurring -> {
                    schedule(task)
                    scheduled++
                }
            }
        }
        Log.d(TAG, "Rescheduled $scheduled alarms after reboot")
    }

    fun scheduleFromReceiver(
        taskId: Long,
        title: String,
        notes: String,
        dueDate: Long,
        hasTime: Boolean,
        recurrenceRule: RecurrenceRule
    ) {
        val pendingIntent = buildPendingIntent(taskId, title, notes, dueDate, hasTime, recurrenceRule)
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
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
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
        recurrenceRule: RecurrenceRule
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_TASK_NOTES, notes)
            putExtra(EXTRA_DUE_DATE, dueDate)
            putExtra(EXTRA_HAS_TIME, hasTime)
            putExtra(EXTRA_RECURRENCE_UNIT, recurrenceRule.unit.name)
            putExtra(EXTRA_RECURRENCE_INTERVAL, recurrenceRule.interval)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.requestCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun advanceToFuture(dueDate: Long, rule: RecurrenceRule, now: Long): Long {
        var next = dueDate
        while (next <= now) {
            next = nextOccurrence(next, rule)
        }
        return next
    }

    companion object {
        fun nextOccurrence(from: Long, rule: RecurrenceRule): Long {
            if (!rule.isRecurring) return from
            val cal = Calendar.getInstance().apply { timeInMillis = from }
            when (rule.unit) {
                RecurrenceUnit.HOURS -> cal.add(Calendar.HOUR_OF_DAY, rule.interval)
                RecurrenceUnit.DAYS -> cal.add(Calendar.DAY_OF_YEAR, rule.interval)
                RecurrenceUnit.WEEKS -> cal.add(Calendar.WEEK_OF_YEAR, rule.interval)
                RecurrenceUnit.MONTHS -> cal.add(Calendar.MONTH, rule.interval)
                RecurrenceUnit.NONE -> Unit
            }
            return cal.timeInMillis
        }
    }
}

private fun Long.requestCode(): Int = (this and 0x7FFFFFFF).toInt()
