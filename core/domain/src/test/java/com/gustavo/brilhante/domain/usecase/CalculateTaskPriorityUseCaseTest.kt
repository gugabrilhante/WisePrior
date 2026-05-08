package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.time.ClockProvider
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateTaskPriorityUseCaseTest {

    private val DAY_MS = 24L * 60 * 60 * 1000

    // Fixed "now" at noon of UTC day 1000 — avoids midnight-boundary flakiness
    private val NOW = DAY_MS * 1000L + DAY_MS / 2

    private val useCase = CalculateTaskPriorityUseCase(ClockProvider { NOW })

    // ── Completed ─────────────────────────────────────────────────────────────

    @Test
    fun `given completed task, score is -999`() {
        val task = Task(title = "Task", isCompleted = true, createdAt = 1000L)
        assertEquals(-999, useCase(task))
    }

    @Test
    fun `given completed task with all attributes set, score is still -999`() {
        val task = Task(
            title = "Task",
            isCompleted = true,
            isUrgent = true,
            isFlagged = true,
            priority = Priority.HIGH,
            dueDate = NOW - DAY_MS,
            createdAt = 1000L
        )
        assertEquals(-999, useCase(task))
    }

    // ── No metadata ───────────────────────────────────────────────────────────

    @Test
    fun `given task with no attributes, score is 0`() {
        val task = Task(title = "Task", createdAt = 1000L)
        assertEquals(0, useCase(task))
    }

    // ── Urgency / Flag / Priority ─────────────────────────────────────────────

    @Test
    fun `given urgent task, adds 100 to score`() {
        val task = Task(title = "Task", isUrgent = true, createdAt = 1000L)
        assertEquals(100, useCase(task))
    }

    @Test
    fun `given flagged task, adds 80 to score`() {
        val task = Task(title = "Task", isFlagged = true, createdAt = 1000L)
        assertEquals(80, useCase(task))
    }

    @Test
    fun `given HIGH priority, adds 40 to score`() {
        val task = Task(title = "Task", priority = Priority.HIGH, createdAt = 1000L)
        assertEquals(40, useCase(task))
    }

    @Test
    fun `given MEDIUM priority, adds 20 to score`() {
        val task = Task(title = "Task", priority = Priority.MEDIUM, createdAt = 1000L)
        assertEquals(20, useCase(task))
    }

    @Test
    fun `given LOW priority, adds 0 to score`() {
        val task = Task(title = "Task", priority = Priority.LOW, createdAt = 1000L)
        assertEquals(0, useCase(task))
    }

    @Test
    fun `given NONE priority, adds 0 to score`() {
        val task = Task(title = "Task", priority = Priority.NONE, createdAt = 1000L)
        assertEquals(0, useCase(task))
    }

    // ── Recurrence ────────────────────────────────────────────────────────────

    @Test
    fun `given recurring task, adds 10 to score`() {
        val task = Task(title = "Task", recurrenceRule = RecurrenceRule(RecurrenceUnit.DAYS, 1), createdAt = 1000L)
        assertEquals(10, useCase(task))
    }

    @Test
    fun `given non-recurring task, recurrence contributes 0`() {
        val task = Task(title = "Task", recurrenceRule = RecurrenceRule.NONE, createdAt = 1000L)
        assertEquals(0, useCase(task))
    }

    // ── Due date: overdue ─────────────────────────────────────────────────────

    @Test
    fun `given task due yesterday, adds 120 to score`() {
        val task = Task(title = "Task", dueDate = NOW - DAY_MS, createdAt = 1000L)
        assertEquals(120, useCase(task))
    }

    @Test
    fun `given task due 1ms before now (same UTC day), adds 120 to score`() {
        // Overdue even if it was due earlier the same UTC day
        val task = Task(title = "Task", dueDate = NOW - 1, createdAt = 1000L)
        assertEquals(120, useCase(task))
    }

    // ── Due date: today (not overdue) ─────────────────────────────────────────

    @Test
    fun `given task due exactly now, adds 70 to score`() {
        // due == now: not < now, and isSameDay is true
        val task = Task(title = "Task", dueDate = NOW, createdAt = 1000L)
        assertEquals(70, useCase(task))
    }

    @Test
    fun `given task due later today, adds 70 to score`() {
        // Still on the same UTC day, but after now
        val endOfToday = (NOW / DAY_MS + 1) * DAY_MS - 1
        val task = Task(title = "Task", dueDate = endOfToday, createdAt = 1000L)
        assertEquals(70, useCase(task))
    }

    // ── Due date: tomorrow / within 24h ───────────────────────────────────────

    @Test
    fun `given task due at start of next UTC day (within 24h window), adds 50 to score`() {
        val startOfTomorrow = (NOW / DAY_MS + 1) * DAY_MS
        val task = Task(title = "Task", dueDate = startOfTomorrow, createdAt = 1000L)
        assertEquals(50, useCase(task))
    }

    @Test
    fun `given task due exactly 24h from now (boundary), adds 50 to score`() {
        val task = Task(title = "Task", dueDate = NOW + DAY_MS, createdAt = 1000L)
        assertEquals(50, useCase(task))
    }

    // ── Due date: far future ──────────────────────────────────────────────────

    @Test
    fun `given task due more than 24h from now, due date contributes 0 to score`() {
        val task = Task(title = "Task", dueDate = NOW + DAY_MS + 1, createdAt = 1000L)
        assertEquals(0, useCase(task))
    }

    @Test
    fun `given task due two days from now, due date contributes 0 to score`() {
        val task = Task(title = "Task", dueDate = NOW + 2 * DAY_MS, createdAt = 1000L)
        assertEquals(0, useCase(task))
    }

    // ── No due date ───────────────────────────────────────────────────────────

    @Test
    fun `given task with no due date, due date contributes 0 to score`() {
        val task = Task(title = "Task", dueDate = null, createdAt = 1000L)
        assertEquals(0, useCase(task))
    }

    // ── Clock independence ────────────────────────────────────────────────────

    @Test
    fun `given a different fixed clock, score is deterministic for that instant`() {
        val differentNow = 999_999_999_999L
        val fixedUseCase = CalculateTaskPriorityUseCase(ClockProvider { differentNow })
        val task = Task(title = "Task", dueDate = differentNow - 1, createdAt = 1000L) // 1ms overdue
        assertEquals(120, fixedUseCase(task))
    }

    // ── Cumulative scoring ────────────────────────────────────────────────────

    @Test
    fun `given task with multiple attributes, score is cumulative`() {
        val task = Task(
            title = "Task",
            isUrgent = true,                                          // +100
            isFlagged = true,                                         // +80
            priority = Priority.HIGH,                                 // +40
            dueDate = NOW - DAY_MS,                                   // +120 overdue
            recurrenceRule = RecurrenceRule(RecurrenceUnit.WEEKS, 1),  // +10
            createdAt = 1000L
        )
        assertEquals(350, useCase(task))
    }

    @Test
    fun `given urgent and flagged task due today, score combines all three`() {
        val task = Task(
            title = "Task",
            isUrgent = true, // +100
            isFlagged = true, // +80
            dueDate = NOW,    // +70
            createdAt = 1000L
        )
        assertEquals(250, useCase(task))
    }

    @Test
    fun `given HIGH priority task due tomorrow with recurrence, score accumulates correctly`() {
        val task = Task(
            title = "Task",
            priority = Priority.HIGH,                                // +40
            dueDate = NOW + DAY_MS,                                  // +50 within 24h
            recurrenceRule = RecurrenceRule(RecurrenceUnit.MONTHS, 1), // +10
            createdAt = 1000L
        )
        assertEquals(100, useCase(task))
    }
}
