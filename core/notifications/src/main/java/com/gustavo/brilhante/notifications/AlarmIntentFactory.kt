package com.gustavo.brilhante.notifications

import android.app.PendingIntent
import com.gustavo.brilhante.model.RecurrenceRule

interface AlarmIntentFactory {
    fun createPendingIntent(
        taskId: Long,
        title: String,
        notes: String,
        dueDate: Long,
        hasTime: Boolean,
        recurrenceRule: RecurrenceRule
    ): PendingIntent

    fun getExistingPendingIntent(taskId: Long): PendingIntent?

    /**
     * Creates a PendingIntent that opens the app to show task details.
     * Used for AlarmManager.setAlarmClock.
     */
    fun createShowDetailsPendingIntent(taskId: Long): PendingIntent
}
