package com.gustavo.brilhante.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gustavo.brilhante.domain.logging.Logger
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "AlarmReceiver"

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var scheduler: NotificationScheduler
    @Inject lateinit var logger: Logger

    override fun onReceive(context: Context, intent: Intent) {
        performReceive(intent)
    }

    internal fun performReceive(intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: run {
            logger.w(TAG, "Missing title in alarm intent for task $taskId")
            return
        }
        val notes = intent.getStringExtra(EXTRA_TASK_NOTES) ?: ""
        val dueDate = intent.getLongExtra(EXTRA_DUE_DATE, -1L)
        val hasTime = intent.getBooleanExtra(EXTRA_HAS_TIME, false)

        if (taskId < 0L || dueDate < 0L) {
            logger.w(TAG, "Invalid alarm intent — taskId=$taskId dueDate=$dueDate")
            return
        }

        logger.d(TAG, "Alarm fired for task $taskId: $title")

        notificationHelper.showNotification(taskId, title, notes)

        val unitRaw = intent.getStringExtra(EXTRA_RECURRENCE_UNIT) ?: RecurrenceUnit.NONE.name
        val interval = intent.getIntExtra(EXTRA_RECURRENCE_INTERVAL, 1).coerceAtLeast(1)
        val unit = runCatching { RecurrenceUnit.valueOf(unitRaw) }.getOrDefault(RecurrenceUnit.NONE)
        val recurrenceRule = RecurrenceRule(unit, interval)

        if (recurrenceRule.isRecurring) {
            val nextDue = scheduler.nextOccurrence(dueDate, recurrenceRule)
            scheduler.scheduleFromReceiver(
                taskId = taskId,
                title = title,
                notes = notes,
                dueDate = nextDue,
                hasTime = hasTime,
                recurrenceRule = recurrenceRule
            )
            logger.d(TAG, "Rescheduled recurring task $taskId for $nextDue")
        }
    }
}
