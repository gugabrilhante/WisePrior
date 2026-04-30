package com.gustavo.brilhante.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.gustavo.brilhante.data.repository.TaskRepositoryImpl
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceType
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.storage.database.AppDatabase
import com.gustavo.brilhante.storage.datasources.TaskDataSource
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for TaskRepositoryImpl wired against a real in-memory Room database.
 * Covers the full data flow: TaskRepositoryImpl → TaskDataSource → TaskDao → SQLite.
 */
@RunWith(AndroidJUnit4::class)
class TaskRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dataSource = TaskDataSource(database.taskDao())
        repository = TaskRepositoryImpl(dataSource, UnconfinedTestDispatcher())
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun buildTask(
        title: String = "Task",
        createdAt: Long = 1_700_000_000_000L
    ) = Task(
        id = 0,
        title = title,
        notes = "Some notes",
        url = "",
        dueDate = null,
        hasTime = false,
        isUrgent = false,
        priority = Priority.NONE,
        tagIds = emptyList(),
        isFlagged = false,
        isCompleted = false,
        recurrenceType = RecurrenceType.NONE,
        createdAt = createdAt
    )

    /** Adds a task and returns it back with its auto-generated id. */
    private suspend fun addAndGet(task: Task): Task {
        repository.addTask(task)
        var inserted: Task? = null
        repository.getTasks().test {
            inserted = awaitItem().first()
            cancelAndIgnoreRemainingEvents()
        }
        return inserted!!
    }

    // ── add → getTasks ────────────────────────────────────────────────────────

    @Test
    fun givenNewTask_whenAddTaskThenGetTasksObserved_thenTaskAppearsInFlow() = runTest {
        repository.addTask(buildTask(title = "Buy groceries"))

        repository.getTasks().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Buy groceries", items.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenMultipleTasksAdded_whenGetTasksObserved_thenAllTasksAppearOrderedByCreatedAtDesc() = runTest {
        repository.addTask(buildTask(title = "Task A", createdAt = 1_000L))
        repository.addTask(buildTask(title = "Task B", createdAt = 2_000L))
        repository.addTask(buildTask(title = "Task C", createdAt = 3_000L))

        repository.getTasks().test {
            val items = awaitItem()
            assertEquals(3, items.size)
            assertEquals("Task C", items[0].title)
            assertEquals("Task B", items[1].title)
            assertEquals("Task A", items[2].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── add → getTaskById ─────────────────────────────────────────────────────

    @Test
    fun givenAddedTask_whenGetTaskByIdCalledWithItsId_thenReturnsTaskMatchingOriginal() = runTest {
        val inserted = addAndGet(buildTask(title = "Read book"))

        val retrieved = repository.getTaskById(inserted.id)

        assertNotNull(retrieved)
        assertEquals("Read book", retrieved!!.title)
        assertEquals(inserted.id, retrieved.id)
    }

    @Test
    fun givenEmptyRepository_whenGetTaskByIdCalled_thenReturnsNull() = runTest {
        assertNull(repository.getTaskById(999L))
    }

    // ── add → update → getTasks ───────────────────────────────────────────────

    @Test
    fun givenAddedTask_whenUpdateTaskThenGetTasksObserved_thenFlowReflectsUpdatedFields() = runTest {
        val inserted = addAndGet(buildTask(title = "Original title"))
        val updated = inserted.copy(title = "Updated title", priority = Priority.HIGH)

        repository.updateTask(updated)

        repository.getTasks().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Updated title", items.first().title)
            assertEquals(Priority.HIGH, items.first().priority)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── add → delete → getTasks ───────────────────────────────────────────────

    @Test
    fun givenAddedTask_whenDeleteTaskThenGetTasksObserved_thenFlowIsEmpty() = runTest {
        val inserted = addAndGet(buildTask(title = "To delete"))

        repository.deleteTask(inserted)

        repository.getTasks().test {
            assertEquals(emptyList<Task>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── domain model round-trip ───────────────────────────────────────────────

    @Test
    fun givenTaskWithAllFields_whenAddThenRetrieve_thenAllDomainFieldsSurviveTheRoundTrip() = runTest {
        val original = buildTask(title = "Full task").copy(
            notes = "My notes",
            isUrgent = true,
            priority = Priority.MEDIUM,
            isFlagged = true,
            recurrenceType = RecurrenceType.WEEKLY
        )
        val inserted = addAndGet(original)

        val retrieved = repository.getTaskById(inserted.id)!!

        assertEquals("Full task",           retrieved.title)
        assertEquals("My notes",            retrieved.notes)
        assertEquals(true,                  retrieved.isUrgent)
        assertEquals(Priority.MEDIUM,       retrieved.priority)
        assertEquals(true,                  retrieved.isFlagged)
        assertEquals(RecurrenceType.WEEKLY, retrieved.recurrenceType)
    }
}
