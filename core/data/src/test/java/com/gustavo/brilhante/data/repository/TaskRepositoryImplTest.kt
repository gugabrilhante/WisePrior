package com.gustavo.brilhante.data.repository

import app.cash.turbine.test
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceType
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.storage.datasources.TaskDataSource
import com.gustavo.brilhante.storage.entity.TaskEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
        tags = emptyList(),
        isFlagged = false,
        recurrenceType = "NONE",
        createdAt = 1_000L
    )

    private val task = Task(
        id = 1L,
        title = "Task",
        priority = Priority.NONE,
        recurrenceType = RecurrenceType.NONE,
        createdAt = 1_000L
    )

    @Test
    fun `getTasks maps entities to domain models`() = runTest {
        every { dataSource.allTasks } returns flowOf(listOf(entity))

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
        coEvery { dataSource.getTaskById(1L) } returns entity

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
    fun `addTask inserts entity into data source`() = runTest {
        coEvery { dataSource.insertTask(any()) } returns Unit

        repository.addTask(task)

        coVerify(exactly = 1) { dataSource.insertTask(any()) }
    }

    @Test
    fun `updateTask updates entity in data source`() = runTest {
        coEvery { dataSource.updateTask(any()) } returns Unit

        repository.updateTask(task)

        coVerify(exactly = 1) { dataSource.updateTask(any()) }
    }

    @Test
    fun `deleteTask deletes entity from data source`() = runTest {
        coEvery { dataSource.deleteTask(any()) } returns Unit

        repository.deleteTask(task)

        coVerify(exactly = 1) { dataSource.deleteTask(any()) }
    }
}
