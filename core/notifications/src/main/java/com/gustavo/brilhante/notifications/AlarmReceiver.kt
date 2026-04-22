package com.gustavo.brilhante.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gustavo.brilhante.model.RecurrenceType
import com.gustavo.brilhante.model.Task
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: return
        val notes = intent.getStringExtra(EXTRA_TASK_NOTES) ?: ""
        val recurrence = intent.getStringExtra("extra_recurrence") ?: RecurrenceType.NONE.name
        val dueDate = intent.getLongExtra("extra_due_date", -1L)
        val hasTime = intent.getBooleanExtra("extra_has_time", false)

        if (taskId < 0L) return

        notificationHelper.showNotification(taskId, title, notes)

        val recurrenceType = runCatching { RecurrenceType.valueOf(recurrence) }
            .getOrDefault(RecurrenceType.NONE)

        if (recurrenceType != RecurrenceType.NONE && dueDate > 0L) {
            val nextMillis = nextOccurrence(dueDate, recurrenceType)
            val nextTask = Task(
                id = taskId,
                title = title,
                notes = notes,
                dueDate = nextMillis,
                hasTime = hasTime,
                recurrenceType = recurrenceType
            )
            notificationScheduler.schedule(nextTask)
        }
    }

    private fun nextOccurrence(from: Long, type: RecurrenceType): Long {
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
