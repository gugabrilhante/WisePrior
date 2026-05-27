package com.gustavo.brilhante.storage.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gustavo.brilhante.storage.database.AppDatabase
import com.gustavo.brilhante.storage.entity.ChecklistItemEntity
import com.gustavo.brilhante.storage.entity.TaskEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChecklistItemDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var dao: ChecklistItemDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskDao = database.taskDao()
        dao = database.checklistItemDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private suspend fun insertTask(title: String = "Task"): Long = taskDao.insertTask(
        TaskEntity(title = title, createdAt = 1000L)
    )

    private fun buildItem(taskId: Long, text: String, isChecked: Boolean = false) =
        ChecklistItemEntity(taskId = taskId, text = text, isChecked = isChecked)

    // ── insertAll ─────────────────────────────────────────────────────────────

    @Test
    fun givenItems_whenInsertAll_thenGetItemsForTaskReturnsThem() = runTest {
        val taskId = insertTask()
        dao.insertAll(listOf(buildItem(taskId, "Milk"), buildItem(taskId, "Eggs")))

        val items = dao.getItemsForTask(taskId)
        assertEquals(2, items.size)
        assertEquals("Milk", items[0].text)
        assertEquals("Eggs", items[1].text)
    }

    @Test
    fun givenNoItems_whenGetItemsForTask_thenReturnsEmpty() = runTest {
        val taskId = insertTask()
        assertTrue(dao.getItemsForTask(taskId).isEmpty())
    }

    // ── deleteAllForTask ──────────────────────────────────────────────────────

    @Test
    fun givenExistingItems_whenDeleteAllForTask_thenReturnsEmptyList() = runTest {
        val taskId = insertTask()
        dao.insertAll(listOf(buildItem(taskId, "Item 1"), buildItem(taskId, "Item 2")))

        dao.deleteAllForTask(taskId)

        assertTrue(dao.getItemsForTask(taskId).isEmpty())
    }

    @Test
    fun givenTwoTasks_whenDeleteItemsForOneTask_thenOtherTaskItemsUnaffected() = runTest {
        val taskId1 = insertTask("Task 1")
        val taskId2 = insertTask("Task 2")
        dao.insertAll(listOf(buildItem(taskId1, "A"), buildItem(taskId2, "B")))

        dao.deleteAllForTask(taskId1)

        assertTrue(dao.getItemsForTask(taskId1).isEmpty())
        assertEquals(1, dao.getItemsForTask(taskId2).size)
    }

    // ── isChecked preservation ────────────────────────────────────────────────

    @Test
    fun givenCheckedItem_whenInsertedAndFetched_thenIsCheckedPreserved() = runTest {
        val taskId = insertTask()
        dao.insertAll(listOf(buildItem(taskId, "Butter", isChecked = true)))

        val items = dao.getItemsForTask(taskId)
        assertEquals(true, items.first().isChecked)
    }

    // ── ordering by id ────────────────────────────────────────────────────────

    @Test
    fun givenMultipleItems_whenFetched_thenOrderedByIdAscending() = runTest {
        val taskId = insertTask()
        dao.insertAll(listOf(
            buildItem(taskId, "First"),
            buildItem(taskId, "Second"),
            buildItem(taskId, "Third")
        ))

        val items = dao.getItemsForTask(taskId)
        assertEquals(listOf("First", "Second", "Third"), items.map { it.text })
    }
}
