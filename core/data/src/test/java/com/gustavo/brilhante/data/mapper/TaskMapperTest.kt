package com.gustavo.brilhante.data.mapper

import com.gustavo.brilhante.model.ChecklistItem
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.storage.entity.ChecklistItemEntity
import com.gustavo.brilhante.storage.entity.TaskEntity
import com.gustavo.brilhante.storage.entity.TaskWithChecklistItems
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
        createdAt = 1_699_000_000_000L,
        checklistItems = emptyList()
    )

    private fun withNoChecklist() = TaskWithChecklistItems(sampleEntity, emptyList())

    @Test
    fun `TaskWithChecklistItems toModel maps all fields correctly`() {
        val model = withNoChecklist().toModel()

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
    fun `toChecklistEntities maps all checklist items`() {
        val task = sampleTask.copy(
            checklistItems = listOf(
                ChecklistItem(id = 0L, text = "Item 1", isChecked = false),
                ChecklistItem(id = 0L, text = "Item 2", isChecked = true)
            )
        )

        val entities = task.toChecklistEntities()

        assertEquals(2, entities.size)
        assertEquals("Item 1", entities[0].text)
        assertEquals(false, entities[0].isChecked)
        assertEquals("Item 2", entities[1].text)
        assertEquals(true, entities[1].isChecked)
        entities.forEach { assertEquals(task.id, it.taskId) }
    }

    @Test
    fun `toModel falls back to Priority NONE for unknown priority string`() {
        val entity = withNoChecklist().copy(task = sampleEntity.copy(priority = "UNKNOWN_PRIORITY"))
        val model = entity.toModel()
        assertEquals(Priority.NONE, model.priority)
    }

    @Test
    fun `toModel falls back to RecurrenceUnit NONE for unknown unit string`() {
        val entity = withNoChecklist().copy(task = sampleEntity.copy(recurrenceUnit = "UNKNOWN_UNIT"))
        val model = entity.toModel()
        assertEquals(RecurrenceUnit.NONE, model.recurrenceRule.unit)
    }

    @Test
    fun `toModel handles null dueDate`() {
        val entity = withNoChecklist().copy(task = sampleEntity.copy(dueDate = null))
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
        val entity = withNoChecklist().copy(task = sampleEntity.copy(recurrenceInterval = 0))
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
        val entity = withNoChecklist().copy(task = sampleEntity.copy(recurrenceUnit = "HOURS", recurrenceInterval = 8))
        val model = entity.toModel()
        assertEquals(RecurrenceRule(RecurrenceUnit.HOURS, 8), model.recurrenceRule)
    }

    @Test
    fun `toModel includes checklist items sorted by id`() {
        val checklistEntities = listOf(
            ChecklistItemEntity(id = 2L, taskId = 1L, text = "Second", isChecked = false),
            ChecklistItemEntity(id = 1L, taskId = 1L, text = "First", isChecked = true)
        )
        val entity = TaskWithChecklistItems(sampleEntity, checklistEntities)

        val model = entity.toModel()

        assertEquals(2, model.checklistItems.size)
        assertEquals("First", model.checklistItems[0].text)
        assertEquals(true, model.checklistItems[0].isChecked)
        assertEquals("Second", model.checklistItems[1].text)
    }
}
