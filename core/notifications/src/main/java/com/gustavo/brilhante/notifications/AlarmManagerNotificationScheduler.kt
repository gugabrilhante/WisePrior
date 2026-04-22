package com.gustavo.brilhante.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gustavo.brilhante.model.RecurrenceType
import com.gustavo.brilhante.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmManagerNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(task: Task) {
        val dueDate = task.dueDate ?: return
        if (dueDate <= System.currentTimeMillis()) return

        scheduleAlarm(
            taskId = task.id,
            title = task.title,
            notes = task.notes,
            dueDate = dueDate,
            hasTime = task.hasTime,
            recurrenceType = task.recurrenceType
        )
    }

    fun scheduleAlarm(
        taskId: Long,
        title: String,
        notes: String,
        dueDate: Long,
        hasTime: Boolean,
        recurrenceType: RecurrenceType
    ) {
        val pendingIntent = buildPendingIntent(taskId, title, notes, dueDate, hasTime, recurrenceType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Fallback to inexact alarm if permission not granted
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
        }
    }

    override fun cancel(taskId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    override fun rescheduleAll(tasks: List<Task>) {
        tasks.forEach { task ->
            val dueDate = task.dueDate ?: return@forEach
            if (dueDate > System.currentTimeMillis()) schedule(task)
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
            putExtra("extra_recurrence", recurrenceType.name)
            putExtra("extra_due_date", dueDate)
            putExtra("extra_has_time", hasTime)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
