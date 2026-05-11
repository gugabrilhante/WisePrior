package com.gustavo.brilhante.notifications

import android.content.Context
import android.content.Intent
import com.gustavo.brilhante.model.RecurrenceRule
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface AlarmIntentBuilder {
    fun buildAlarmIntent(
        taskId: Long,
        title: String,
        notes: String,
        dueDate: Long,
        hasTime: Boolean,
        recurrenceRule: RecurrenceRule
    ): Intent

    fun buildBaseAlarmIntent(): Intent

    fun buildShowDetailsIntent(taskId: Long): Intent
}

class AlarmIntentBuilderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmIntentBuilder {

    override fun buildAlarmIntent(
        taskId: Long,
        title: String,
        notes: String,
        dueDate: Long,
        hasTime: Boolean,
        recurrenceRule: RecurrenceRule
    ): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_TASK_NOTES, notes)
            putExtra(EXTRA_DUE_DATE, dueDate)
            putExtra(EXTRA_HAS_TIME, hasTime)
            putExtra(EXTRA_RECURRENCE_UNIT, recurrenceRule.unit.name)
            putExtra(EXTRA_RECURRENCE_INTERVAL, recurrenceRule.interval)
        }
    }

    override fun buildBaseAlarmIntent(): Intent {
        return Intent(context, AlarmReceiver::class.java)
    }

    override fun buildShowDetailsIntent(taskId: Long): Intent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra(EXTRA_TASK_ID, taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        } ?: Intent()
        return intent
    }
}
