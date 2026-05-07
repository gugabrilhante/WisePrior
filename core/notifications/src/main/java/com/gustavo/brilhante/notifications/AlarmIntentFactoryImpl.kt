package com.gustavo.brilhante.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.gustavo.brilhante.model.RecurrenceRule
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmIntentFactoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmIntentFactory {

    override fun createPendingIntent(
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

    override fun getExistingPendingIntent(taskId: Long): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            taskId.requestCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun Long.requestCode(): Int = (this and 0x7FFFFFFF).toInt()
}
