package com.gustavo.brilhante.data.repository

import app.cash.turbine.test
import com.gustavo.brilhante.model.ChecklistItem
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.storage.datasources.TaskDataSource
import com.gustavo.brilhante.storage.entity.ChecklistItemEntity
import com.gustavo.brilhante.storage.entity.TaskEntity
import com.gustavo.brilhante.storage.entity.TaskWithChecklistItems
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskRepositoryImplTest {

    private val dataSource: TaskDataSource = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = TaskRepositoryImpl(dataSource, testDispatcher)

    private val entity = TaskEntity(
        id = 1L,
        title = "Task",
        notes = "",
        url = "",
        dueDate = null,
        hasTime = false,
        isUrgent = false,
        priority = "NONE",
        tagIds = emptyList(),
        isFlagged = false,
        isCompleted = false,
        recurrenceUnit = "NONE",
        recurrenceInterval = 1,
        createdAt = 1_000L,
    )

    private val taskWithNoChecklist = TaskWithChecklistItems(entity, emptyList())

    private val task = Task(
        id = 1L,
        title = "Task",
        priority = Priority.NONE,
        tagIds = emptyList(),
        isCompleted = false,
        recurrenceRule = RecurrenceRule.NONE,
        createdAt = 1_000L,
    )

    @Test
    fun `getTasks maps entities to domain models`() = runTest {
        every { dataSource.allTasks } returns flowOf(listOf(taskWithNoChecklist))

        repository.getTasks().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(task, items.first())
            awaitComplete()
        }
    }

    @Test
    fun `getTasks returns empty list when data source has no tasks`() = runTest {
        every { dataSource.allTasks } returns flowOf(emptyList())

        repository.getTasks().test {
            assertEquals(emptyList<Task>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getTaskById returns mapped task when found`() = runTest {
        coEvery { dataSource.getTaskById(1L) } returns taskWithNoChecklist

        val result = repository.getTaskById(1L)

        assertEquals(task, result)
    }

    @Test
    fun `getTaskById returns null when task not found`() = runTest {
        coEvery { dataSource.getTaskById(99L) } returns null

        val result = repository.getTaskById(99L)

        assertNull(result)
    }

    @Test
    fun `addTask inserts entity and checklist items into data source`() = runTest {
        val entitySlot = slot<TaskEntity>()
        val checklistSlot = slot<List<ChecklistItemEntity>>()
        coEvery { dataSource.insertTask(capture(entitySlot), capture(checklistSlot)) } returns Unit

        repository.addTask(task)

        coVerify(exactly = 1) { dataSource.insertTask(any(), any()) }
        assertEquals(task.id, entitySlot.captured.id)
        assertEquals(task.title, entitySlot.captured.title)
        assertEquals(task.priority.name, entitySlot.captured.priority)
        assertEquals(emptyList<ChecklistItemEntity>(), checklistSlot.captured)
    }

    @Test
    fun `addTask passes checklist items to data source`() = runTest {
        val checklistSlot = slot<List<ChecklistItemEntity>>()
        coEvery { dataSource.insertTask(any(), capture(checklistSlot)) } returns Unit
        val taskWithChecklist = task.copy(
            checklistItems = listOf(
                ChecklistItem(id = 0L, text = "Milk", isChecked = false),
                ChecklistItem(id = 0L, text = "Eggs", isChecked = true)
            )
        )

        repository.addTask(taskWithChecklist)

        assertEquals(2, checklistSlot.captured.size)
        assertEquals("Milk", checklistSlot.captured[0].text)
        assertEquals("Eggs", checklistSlot.captured[1].text)
    }

    @Test
    fun `updateTask updates entity and checklist items in data source`() = runTest {
        val entitySlot = slot<TaskEntity>()
        val checklistSlot = slot<List<ChecklistItemEntity>>()
        coEvery { dataSource.updateTask(capture(entitySlot), capture(checklistSlot)) } returns Unit

        repository.updateTask(task)

        coVerify(exactly = 1) { dataSource.updateTask(any(), any()) }
        assertEquals(task.id, entitySlot.captured.id)
        assertEquals(task.title, entitySlot.captured.title)
    }

    @Test
    fun `deleteTask deletes entity from data source`() = runTest {
        val entitySlot = slot<TaskEntity>()
        coEvery { dataSource.deleteTask(capture(entitySlot)) } returns Unit

        repository.deleteTask(task)

        coVerify(exactly = 1) { dataSource.deleteTask(any()) }
        assertEquals(task.id, entitySlot.captured.id)
        assertEquals(task.title, entitySlot.captured.title)
    }

    @Test
    fun `getTasks maps checklist items to domain model`() = runTest {
        val checklistEntity = ChecklistItemEntity(id = 1L, taskId = 1L, text = "Bread", isChecked = true)
        val taskWithChecklist = TaskWithChecklistItems(entity, listOf(checklistEntity))
        every { dataSource.allTasks } returns flowOf(listOf(taskWithChecklist))

        repository.getTasks().test {
            val items = awaitItem()
            val checklist = items.first().checklistItems
            assertEquals(1, checklist.size)
            assertEquals("Bread", checklist.first().text)
            assertEquals(true, checklist.first().isChecked)
            awaitComplete()
        }
    }
}
