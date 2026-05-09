package com.gustavo.brilhante.notifications

import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.Task

interface NotificationScheduler {
    fun schedule(task: Task)
    fun cancel(taskId: Long)
    fun rescheduleAll(tasks: List<Task>)
    fun nextOccurrence(from: Long, rule: RecurrenceRule): Long
    fun scheduleFromReceiver(
        taskId: Long,
        title: String,
        notes: String,
        dueDate: Long,
        hasTime: Boolean,
        recurrenceRule: RecurrenceRule
    )
}
