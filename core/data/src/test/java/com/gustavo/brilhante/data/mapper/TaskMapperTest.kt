package com.gustavo.brilhante.data.mapper

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceType
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
        tags = listOf("shopping", "food"),
        isFlagged = true,
        recurrenceType = "WEEKLY",
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
        tags = listOf("shopping", "food"),
        isFlagged = true,
        recurrenceType = RecurrenceType.WEEKLY,
        createdAt = 1_699_000_000_000L
    )

    @Test
    fun `TaskEntity toModel maps all fields correctly`() {
        val model = sampleEntity.toModel()

        assertEquals(sampleTask, model)
    }

    @Test
    fun `Task toEntity maps all fields correctly`() {
        val entity = sampleTask.toEntity()

        assertEquals(sampleEntity, entity)
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
    fun `toModel falls back to RecurrenceType NONE for unknown recurrence string`() {
        val entity = sampleEntity.copy(recurrenceType = "UNKNOWN_RECURRENCE")
        val model = entity.toModel()
        assertEquals(RecurrenceType.NONE, model.recurrenceType)
    }

    @Test
    fun `toModel handles null dueDate`() {
        val entity = sampleEntity.copy(dueDate = null)
        val model = entity.toModel()
        assertEquals(null, model.dueDate)
    }

    @Test
    fun `toEntity stores priority as its name`() {
        val task = sampleTask.copy(priority = Priority.MEDIUM)
        val entity = task.toEntity()
        assertEquals("MEDIUM", entity.priority)
    }

    @Test
    fun `toEntity stores recurrenceType as its name`() {
        val task = sampleTask.copy(recurrenceType = RecurrenceType.MONTHLY)
        val entity = task.toEntity()
        assertEquals("MONTHLY", entity.recurrenceType)
    }
}
