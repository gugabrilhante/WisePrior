package com.gustavo.brilhante.data.mapper

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.storage.entity.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskMapperTest {

    private val sampleEntity = TaskEntity(
        id = 1L,
        title = "Buy milk",
        notes = "2% fat",
        url = "https://example.com",
        dueDate = 1_700_000_000_000L,
        hasTime = true,
        isUrgent = true,
        priority = "HIGH",
        tagIds = listOf(101L, 102L),
        isFlagged = true,
        isCompleted = true,
        recurrenceUnit = "WEEKS",
        recurrenceInterval = 1,
        createdAt = 1_699_000_000_000L
    )

    private val sampleTask = Task(
        id = 1L,
        title = "Buy milk",
        notes = "2% fat",
        url = "https://example.com",
        dueDate = 1_700_000_000_000L,
        hasTime = true,
        isUrgent = true,
        priority = Priority.HIGH,
        tagIds = listOf(101L, 102L),
        isFlagged = true,
        isCompleted = true,
        recurrenceRule = RecurrenceRule(RecurrenceUnit.WEEKS, 1),
        createdAt = 1_699_000_000_000L
    )

    @Test
    fun `TaskEntity toModel maps all fields correctly`() {
        val model = sampleEntity.toModel()

        assertEquals(sampleTask, model)
        assertEquals(sampleEntity.tagIds, model.tagIds)
        assertEquals(sampleEntity.isCompleted, model.isCompleted)
    }

    @Test
    fun `Task toEntity maps all fields correctly`() {
        val entity = sampleTask.toEntity()

        assertEquals(sampleEntity, entity)
        assertEquals(sampleTask.tagIds, entity.tagIds)
        assertEquals(sampleTask.isCompleted, entity.isCompleted)
    }

    @Test
    fun `toModel and toEntity are inverse operations`() {
        val roundTripped = sampleTask.toEntity().toModel()
        assertEquals(sampleTask, roundTripped)
    }

    @Test
    fun `toModel falls back to Priority NONE for unknown priority string`() {
        val entity = sampleEntity.copy(priority = "UNKNOWN_PRIORITY")
        val model = entity.toModel()
        assertEquals(Priority.NONE, model.priority)
    }

    @Test
    fun `toModel falls back to RecurrenceUnit NONE for unknown unit string`() {
        val entity = sampleEntity.copy(recurrenceUnit = "UNKNOWN_UNIT")
        val model = entity.toModel()
        assertEquals(RecurrenceUnit.NONE, model.recurrenceRule.unit)
    }

    @Test
    fun `toModel handles null dueDate`() {
        val entity = sampleEntity.copy(dueDate = null)
        val model = entity.toModel()
        assertEquals(null, model.dueDate)
    }

    @Test
    fun `toEntity stores recurrenceUnit as its name`() {
        val task = sampleTask.copy(recurrenceRule = RecurrenceRule(RecurrenceUnit.MONTHS, 3))
        val entity = task.toEntity()
        assertEquals("MONTHS", entity.recurrenceUnit)
        assertEquals(3, entity.recurrenceInterval)
    }

    @Test
    fun `toModel coerces interval to at least 1`() {
        val entity = sampleEntity.copy(recurrenceInterval = 0)
        val model = entity.toModel()
        assertEquals(1, model.recurrenceRule.interval)
    }

    @Test
    fun `toEntity stores priority as its name`() {
        val task = sampleTask.copy(priority = Priority.MEDIUM)
        val entity = task.toEntity()
        assertEquals("MEDIUM", entity.priority)
    }

    @Test
    fun `toModel maps custom interval correctly`() {
        val entity = sampleEntity.copy(recurrenceUnit = "HOURS", recurrenceInterval = 8)
        val model = entity.toModel()
        assertEquals(RecurrenceRule(RecurrenceUnit.HOURS, 8), model.recurrenceRule)
    }
}
