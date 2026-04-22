package com.gustavo.brilhante.notifications

import com.gustavo.brilhante.model.Task

interface NotificationScheduler {
    fun schedule(task: Task)
    fun cancel(taskId: Long)
    fun rescheduleAll(tasks: List<Task>)
}
