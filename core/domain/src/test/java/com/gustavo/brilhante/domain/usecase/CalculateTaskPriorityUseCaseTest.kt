package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class CalculateTaskPriorityUseCaseTest {

    private val useCase = CalculateTaskPriorityUseCase()

    @Test
    fun `given completed task, score is -999`() {
        val task = Task(title = "Task", isCompleted = true)
        assertEquals(-999, useCase(task))
    }

    @Test
    fun `given fresh task with no metadata, score is 0`() {
        val task = Task(title = "Task")
        assertEquals(0, useCase(task))
    }

    @Test
    fun `given urgent task, adds 100 to score`() {
        val task = Task(title = "Task", isUrgent = true)
        assertEquals(100, useCase(task))
    }

    @Test
    fun `given flagged task, adds 80 to score`() {
        val task = Task(title = "Task", isFlagged = true)
        assertEquals(80, useCase(task))
    }

    @Test
    fun `given HIGH priority, adds 40 to score`() {
        val task = Task(title = "Task", priority = Priority.HIGH)
        assertEquals(40, useCase(task))
    }

    @Test
    fun `given MEDIUM priority, adds 20 to score`() {
        val task = Task(title = "Task", priority = Priority.MEDIUM)
        assertEquals(20, useCase(task))
    }

    @Test
    fun `given recurring task, adds 10 to score`() {
        val task = Task(title = "Task", recurrenceRule = RecurrenceRule(RecurrenceUnit.DAYS, 1))
        assertEquals(10, useCase(task))
    }

    @Test
    fun `given overdue task, adds 120 to score`() {
        val now = System.currentTimeMillis()
        val overdue = now - 1000
        val task = Task(title = "Task", dueDate = overdue)
        assertEquals(120, useCase(task, now = now))
    }

    @Test
    fun `given task due today, adds 70 to score`() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
        }.timeInMillis
        
        val task = Task(title = "Task", dueDate = now)
        assertEquals(70, useCase(task, now = now))
    }

    @Test
    fun `given task due tomorrow, adds 50 to score`() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
        }.timeInMillis
        val oneDayMs = 24L * 60 * 60 * 1000
        val tomorrow = now + oneDayMs
        
        val task = Task(title = "Task", dueDate = tomorrow)
        // Tomorrow at same time is exactly now + oneDayMs
        assertEquals(50, useCase(task, now = now))
    }

    @Test
    fun `given task due far in future, adds 0 to score for date`() {
        val now = System.currentTimeMillis()
        val farFuture = now + (2 * 24L * 60 * 60 * 1000) + 1000
        val task = Task(title = "Task", dueDate = farFuture)
        assertEquals(0, useCase(task, now = now))
    }

    @Test
    fun `given multiple attributes, score is cumulative`() {
        val now = System.currentTimeMillis()
        val overdue = now - 1000
        val task = Task(
            title = "Task",
            isUrgent = true,    // +100
            isFlagged = true,   // +80
            priority = Priority.HIGH, // +40
            dueDate = overdue,  // +120
            recurrenceRule = RecurrenceRule(RecurrenceUnit.WEEKS, 1) // +10
        )
        // Total: 100 + 80 + 40 + 120 + 10 = 350
        assertEquals(350, useCase(task, now = now))
    }
}
