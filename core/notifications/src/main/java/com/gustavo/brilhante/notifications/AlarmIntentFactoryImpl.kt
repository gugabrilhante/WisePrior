package com.gustavo.brilhante.notifications

import android.app.PendingIntent
import android.content.Context
import com.gustavo.brilhante.model.RecurrenceRule
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmIntentFactoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intentBuilder: AlarmIntentBuilder
) : AlarmIntentFactory {

    override fun createPendingIntent(
        taskId: Long,
        title: String,
        notes: String,
        dueDate: Long,
        hasTime: Boolean,
        recurrenceRule: RecurrenceRule
    ): PendingIntent {
        val intent = intentBuilder.buildAlarmIntent(
            taskId,
            title,
            notes,
            dueDate,
            hasTime,
            recurrenceRule
        )
        return PendingIntent.getBroadcast(
            context,
            taskId.requestCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getExistingPendingIntent(taskId: Long): PendingIntent? {
        val intent = intentBuilder.buildBaseAlarmIntent()
        return PendingIntent.getBroadcast(
            context,
            taskId.requestCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun createShowDetailsPendingIntent(taskId: Long): PendingIntent {
        val intent = intentBuilder.buildShowDetailsIntent(taskId)
        
        return PendingIntent.getActivity(
            context,
            taskId.requestCode() + 1_000_000, // Offset to avoid conflict with broadcast PI
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun Long.requestCode(): Int = (this and 0x7FFFFFFF).toInt()
}
