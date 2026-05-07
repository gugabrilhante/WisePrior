package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.time.ClockProvider
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Task
import javax.inject.Inject

class CalculateTaskPriorityUseCase @Inject constructor(
    private val clockProvider: ClockProvider
) {

    operator fun invoke(task: Task): Int {
        if (task.isCompleted) return -999

        val now = clockProvider.currentTimeMillis()
        var score = 0
        val oneDayMs = 24L * 60 * 60 * 1000

        task.dueDate?.let { due ->
            when {
                due < now -> score += 120
                isSameDay(due, now) -> score += 70
                due <= now + oneDayMs -> score += 50
            }
        }

        if (task.isUrgent) score += 100
        if (task.isFlagged) score += 80

        when (task.priority) {
            Priority.HIGH -> score += 40
            Priority.MEDIUM -> score += 20
            else -> Unit
        }

        if (task.recurrenceRule.isRecurring) score += 10

        return score
    }

    // Compares timestamps by UTC day boundary (timezone handling is a presentation concern).
    private fun isSameDay(a: Long, b: Long): Boolean {
        val dayInMs = 24L * 60 * 60 * 1000
        return a / dayInMs == b / dayInMs
    }
}
