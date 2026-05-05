package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Task
import java.util.Calendar
import javax.inject.Inject

class CalculateTaskPriorityUseCase @Inject constructor() {

    operator fun invoke(task: Task, now: Long = System.currentTimeMillis()): Int {
        if (task.isCompleted) return -999

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

    private fun isSameDay(a: Long, b: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = a
        val dayA = cal.get(Calendar.DAY_OF_YEAR)
        val yearA = cal.get(Calendar.YEAR)
        cal.timeInMillis = b
        return cal.get(Calendar.DAY_OF_YEAR) == dayA && cal.get(Calendar.YEAR) == yearA
    }
}
