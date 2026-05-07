package com.gustavo.brilhante.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.gustavo.brilhante.domain.logging.Logger
import com.gustavo.brilhante.domain.system.AndroidVersionProvider
import com.gustavo.brilhante.domain.time.CalendarProvider
import com.gustavo.brilhante.domain.time.ClockProvider
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
    @ApplicationContext private val context: Context,
    private val clockProvider: ClockProvider,
    private val calendarProvider: CalendarProvider,
    private val versionProvider: AndroidVersionProvider,
    private val logger: Logger,
    private val intentFactory: AlarmIntentFactory
) : NotificationScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(task: Task) {
        val rawDue = task.dueDate ?: return
        val now = clockProvider.currentTimeMillis()

        val scheduledTime = if (rawDue > now) {
            rawDue
        } else if (task.recurrenceRule.isRecurring) {
            advanceToFuture(rawDue, task.recurrenceRule, now)
        } else {
            logger.d(TAG, "Skipping past non-recurring task ${task.id}")
            return
        }

        val pendingIntent = intentFactory.createPendingIntent(
            taskId = task.id,
            title = task.title,
            notes = task.notes,
            dueDate = scheduledTime,
            hasTime = task.hasTime,
            recurrenceRule = task.recurrenceRule
        )

        scheduleExact(scheduledTime, pendingIntent)
        logger.d(TAG, "Scheduled task ${task.id} at $scheduledTime (recurrence=${task.recurrenceRule})")
    }

    override fun cancel(taskId: Long) {
        val pendingIntent = intentFactory.getExistingPendingIntent(taskId) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        logger.d(TAG, "Cancelled alarm for task $taskId")
    }

    override fun rescheduleAll(tasks: List<Task>) {
        val now = clockProvider.currentTimeMillis()
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
        logger.d(TAG, "Rescheduled $scheduled alarms after reboot")
    }

    fun scheduleFromReceiver(
        taskId: Long,
        title: String,
        notes: String,
        dueDate: Long,
        hasTime: Boolean,
        recurrenceRule: RecurrenceRule
    ) {
        val pendingIntent = intentFactory.createPendingIntent(
            taskId, title, notes, dueDate, hasTime, recurrenceRule
        )
        scheduleExact(dueDate, pendingIntent)
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private fun scheduleExact(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val canScheduleExact = if (versionProvider.sdkInt >= Build.VERSION_CODES.S) {
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
            logger.w(TAG, "SCHEDULE_EXACT_ALARM not granted — using inexact fallback")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun advanceToFuture(dueDate: Long, rule: RecurrenceRule, now: Long): Long {
        var next = dueDate
        while (next <= now) {
            next = nextOccurrence(next, rule)
        }
        return next
    }

    fun nextOccurrence(from: Long, rule: RecurrenceRule): Long {
        if (!rule.isRecurring) return from
        val cal = calendarProvider.getInstance().apply { timeInMillis = from }
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
