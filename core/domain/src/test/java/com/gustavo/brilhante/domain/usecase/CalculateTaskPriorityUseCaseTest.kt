package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateTaskPriorityUseCaseTest {

    private val useCase = CalculateTaskPriorityUseCase()

    private val now = System.currentTimeMillis()
    private val oneHour = 60L * 60 * 1000
    private val oneDay = 24L * oneHour

    private fun task(block: Task.() -> Task = { this }) =
        Task(id = 1, title = "Test").block()

    @Test
    fun `completed task returns -999`() {
        val score = useCase(task { copy(isCompleted = true) }, now)
        assertEquals(-999, score)
    }

    @Test
    fun `completed task with all flags still returns -999`() {
        val score = useCase(
            task { copy(isCompleted = true, isUrgent = true, isFlagged = true, priority = Priority.HIGH) },
            now
        )
        assertEquals(-999, score)
    }

    @Test
    fun `overdue task gets +120`() {
        val overdueDue = now - oneDay
        val score = useCase(task { copy(dueDate = overdueDue) }, now)
        assertEquals(120, score)
    }

    @Test
    fun `urgent task gets +100`() {
        val score = useCase(task { copy(isUrgent = true) }, now)
        assertEquals(100, score)
    }

    @Test
    fun `flagged task gets +80`() {
        val score = useCase(task { copy(isFlagged = true) }, now)
        assertEquals(80, score)
    }

    @Test
    fun `HIGH priority gets +40`() {
        val score = useCase(task { copy(priority = Priority.HIGH) }, now)
        assertEquals(40, score)
    }

    @Test
    fun `MEDIUM priority gets +20`() {
        val score = useCase(task { copy(priority = Priority.MEDIUM) }, now)
        assertEquals(20, score)
    }

    @Test
    fun `LOW priority gets 0 bonus`() {
        val score = useCase(task { copy(priority = Priority.LOW) }, now)
        assertEquals(0, score)
    }

    @Test
    fun `recurring task gets +10`() {
        val score = useCase(
            task { copy(recurrenceRule = RecurrenceRule(RecurrenceUnit.DAYS, 1)) },
            now
        )
        assertEquals(10, score)
    }

    @Test
    fun `due in next 24h gets +50`() {
        val dueSoon = now + 12 * oneHour  // 12 hours from now
        val score = useCase(task { copy(dueDate = dueSoon) }, now)
        // Note: if same day, gets +70 instead
        assertTrue(score == 70 || score == 50)
    }

    @Test
    fun `task due far in future has no due-date bonus`() {
        val farFuture = now + 10 * oneDay
        val score = useCase(task { copy(dueDate = farFuture) }, now)
        assertEquals(0, score)
    }

    @Test
    fun `urgent flagged HIGH overdue task accumulates all bonuses`() {
        val overdue = now - oneDay
        val score = useCase(
            task {
                copy(
                    dueDate = overdue,
                    isUrgent = true,
                    isFlagged = true,
                    priority = Priority.HIGH,
                    recurrenceRule = RecurrenceRule(RecurrenceUnit.DAYS, 1)
                )
            },
            now
        )
        // 120 (overdue) + 100 (urgent) + 80 (flagged) + 40 (HIGH) + 10 (recurring) = 350
        assertEquals(350, score)
    }

    @Test
    fun `smart priority sorts higher-score tasks first`() {
        val urgent = Task(id = 1, title = "Urgent", isUrgent = true)
        val normal = Task(id = 2, title = "Normal")
        val highPriority = Task(id = 3, title = "High", priority = Priority.HIGH)

        val tasks = listOf(normal, highPriority, urgent)
        val sorted = tasks.sortedByDescending { useCase(it, now) }

        assertEquals(urgent.id, sorted[0].id)
        assertEquals(highPriority.id, sorted[1].id)
        assertEquals(normal.id, sorted[2].id)
    }

    @Test
    fun `task without due date scores 0 for date components`() {
        val score = useCase(task { copy(dueDate = null) }, now)
        assertEquals(0, score)
    }
}
