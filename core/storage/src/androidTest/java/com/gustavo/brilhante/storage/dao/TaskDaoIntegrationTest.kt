package com.gustavo.brilhante.storage.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.gustavo.brilhante.storage.database.AppDatabase
import com.gustavo.brilhante.storage.entity.TaskEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for TaskDao using an in-memory Room database.
 * Validates real SQL behaviour: inserts, queries, updates, deletes, and Flow reactivity.
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.taskDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun buildEntity(
        id: Long = 0,
        title: String = "Task",
        createdAt: Long = System.currentTimeMillis()
    ) = TaskEntity(
        id = id,
        title = title,
        notes = "",
        url = "",
        dueDate = null,
        hasTime = false,
        isUrgent = false,
        priority = "NONE",
        tagIds = emptyList(),
        isFlagged = false,
        isCompleted = false,
        recurrenceType = "NONE",
        createdAt = createdAt
    )

    // ── getAllTasks ────────────────────────────────────────────────────────────

    @Test
    fun givenEmptyDatabase_whenGetAllTasksObserved_thenEmitsEmptyList() = runTest {
        dao.getAllTasks().test {
            assertEquals(emptyList<TaskEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenInsertedTask_whenGetAllTasksObserved_thenEmitsListContainingThatTask() = runTest {
        val insertedId = dao.insertTask(buildEntity(title = "Buy milk"))

        dao.getAllTasks().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Buy milk", items.first().title)
            assertEquals(insertedId, items.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenTasksWithDifferentCreatedAt_whenGetAllTasksObserved_thenOrderedByCreatedAtDesc() = runTest {
        dao.insertTask(buildEntity(title = "Older", createdAt = 1_000L))
        dao.insertTask(buildEntity(title = "Newer", createdAt = 2_000L))

        dao.getAllTasks().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals("Newer", items[0].title)
            assertEquals("Older", items[1].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenObservationStarted_whenTaskInsertedAfterwards_thenFlowEmitsUpdatedList() = runTest {
        dao.getAllTasks().test {
            assertEquals(emptyList<TaskEntity>(), awaitItem())

            dao.insertTask(buildEntity(title = "Reactive task"))

            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("Reactive task", updated.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── getTaskById ───────────────────────────────────────────────────────────

    @Test
    fun givenInsertedTask_whenGetTaskByIdCalledWithItsId_thenReturnsThatTask() = runTest {
        val insertedId = dao.insertTask(buildEntity(title = "Finish report"))

        val result = dao.getTaskById(insertedId)

        assertNotNull(result)
        assertEquals("Finish report", result!!.title)
        assertEquals(insertedId, result.id)
    }

    @Test
    fun givenEmptyDatabase_whenGetTaskByIdCalled_thenReturnsNull() = runTest {
        assertNull(dao.getTaskById(99L))
    }

    // ── updateTask ────────────────────────────────────────────────────────────

    @Test
    fun givenInsertedTask_whenUpdated_thenGetAllTasksReflectsTheNewFieldValues() = runTest {
        val insertedId = dao.insertTask(buildEntity(title = "Original"))

        dao.updateTask(buildEntity(id = insertedId, title = "Updated"))

        dao.getAllTasks().test {
            assertEquals("Updated", awaitItem().first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── deleteTask ────────────────────────────────────────────────────────────

    @Test
    fun givenInsertedTask_whenDeleted_thenGetAllTasksEmitsEmptyList() = runTest {
        val insertedId = dao.insertTask(buildEntity(title = "To delete"))

        dao.deleteTask(buildEntity(id = insertedId, title = "To delete"))

        dao.getAllTasks().test {
            assertEquals(emptyList<TaskEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
