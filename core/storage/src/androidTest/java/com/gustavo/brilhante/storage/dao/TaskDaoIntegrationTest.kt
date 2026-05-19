package com.gustavo.brilhante.storage.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.gustavo.brilhante.storage.database.AppDatabase
import com.gustavo.brilhante.storage.entity.ChecklistItemEntity
import com.gustavo.brilhante.storage.entity.TaskEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TaskDao
    private lateinit var checklistItemDao: ChecklistItemDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.taskDao()
        checklistItemDao = database.checklistItemDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun buildEntity(
        id: Long = 0,
        title: String = "Task",
        createdAt: Long = 1000L
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
        recurrenceUnit = "NONE",
        recurrenceInterval = 1,
        createdAt = createdAt
    )

    // ── getAllTasksWithChecklist ───────────────────────────────────────────────

    @Test
    fun givenEmptyDatabase_whenGetAllTasksObserved_thenEmitsEmptyList() = runTest {
        dao.getAllTasksWithChecklist().test {
            assertEquals(emptyList<TaskEntity>(), awaitItem().map { it.task })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenInsertedTask_whenGetAllTasksObserved_thenEmitsListContainingThatTask() = runTest {
        val insertedId = dao.insertTask(buildEntity(title = "Buy milk"))

        dao.getAllTasksWithChecklist().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Buy milk", items.first().task.title)
            assertEquals(insertedId, items.first().task.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenTasksWithDifferentCreatedAt_whenGetAllTasksObserved_thenOrderedByCreatedAtDesc() = runTest {
        dao.insertTask(buildEntity(title = "Older", createdAt = 1_000L))
        dao.insertTask(buildEntity(title = "Newer", createdAt = 2_000L))

        dao.getAllTasksWithChecklist().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals("Newer", items[0].task.title)
            assertEquals("Older", items[1].task.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenObservationStarted_whenTaskInsertedAfterwards_thenFlowEmitsUpdatedList() = runTest {
        dao.getAllTasksWithChecklist().test {
            assertEquals(emptyList<TaskEntity>(), awaitItem().map { it.task })

            dao.insertTask(buildEntity(title = "Reactive task"))

            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("Reactive task", updated.first().task.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── getTaskWithChecklistById ──────────────────────────────────────────────

    @Test
    fun givenInsertedTask_whenGetTaskByIdCalledWithItsId_thenReturnsThatTask() = runTest {
        val insertedId = dao.insertTask(buildEntity(title = "Finish report"))

        val result = dao.getTaskWithChecklistById(insertedId)

        assertNotNull(result)
        assertEquals("Finish report", result!!.task.title)
        assertEquals(insertedId, result.task.id)
    }

    @Test
    fun givenEmptyDatabase_whenGetTaskByIdCalled_thenReturnsNull() = runTest {
        assertNull(dao.getTaskWithChecklistById(99L))
    }

    // ── updateTask ────────────────────────────────────────────────────────────

    @Test
    fun givenInsertedTask_whenUpdated_thenGetAllTasksReflectsTheNewFieldValues() = runTest {
        val insertedId = dao.insertTask(buildEntity(title = "Original"))

        dao.updateTask(buildEntity(id = insertedId, title = "Updated"))

        dao.getAllTasksWithChecklist().test {
            assertEquals("Updated", awaitItem().first().task.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── deleteTask ────────────────────────────────────────────────────────────

    @Test
    fun givenInsertedTask_whenDeleted_thenGetAllTasksEmitsEmptyList() = runTest {
        val insertedId = dao.insertTask(buildEntity(title = "To delete"))

        dao.deleteTask(buildEntity(id = insertedId, title = "To delete"))

        dao.getAllTasksWithChecklist().test {
            assertEquals(emptyList<TaskEntity>(), awaitItem().map { it.task })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── getAllTasksWithChecklist — checklist included ──────────────────────────

    @Test
    fun givenTaskWithChecklistItems_whenGetAllTasksWithChecklist_thenItemsAreIncluded() = runTest {
        val taskId = dao.insertTask(buildEntity(title = "Shopping"))
        checklistItemDao.insertAll(listOf(
            ChecklistItemEntity(taskId = taskId, text = "Milk", isChecked = false),
            ChecklistItemEntity(taskId = taskId, text = "Eggs", isChecked = true)
        ))

        dao.getAllTasksWithChecklist().test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            val checklist = tasks.first().checklistItems.sortedBy { it.id }
            assertEquals(2, checklist.size)
            assertEquals("Milk", checklist[0].text)
            assertEquals(false, checklist[0].isChecked)
            assertEquals("Eggs", checklist[1].text)
            assertEquals(true, checklist[1].isChecked)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenTaskWithNoChecklistItems_whenGetAllTasksWithChecklist_thenEmptyListReturned() = runTest {
        dao.insertTask(buildEntity(title = "Simple task"))

        dao.getAllTasksWithChecklist().test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals(emptyList<ChecklistItemEntity>(), tasks.first().checklistItems)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenTaskWithChecklist_whenGetTaskWithChecklistById_thenCorrectTaskReturned() = runTest {
        val taskId = dao.insertTask(buildEntity(title = "Supermarket"))
        checklistItemDao.insertAll(listOf(
            ChecklistItemEntity(taskId = taskId, text = "Bread", isChecked = false)
        ))

        val result = dao.getTaskWithChecklistById(taskId)
        assertEquals("Supermarket", result?.task?.title)
        assertEquals(1, result?.checklistItems?.size)
        assertEquals("Bread", result?.checklistItems?.first()?.text)
    }
}
