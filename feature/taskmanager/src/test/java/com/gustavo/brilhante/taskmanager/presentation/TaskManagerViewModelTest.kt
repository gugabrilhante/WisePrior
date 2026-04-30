package com.gustavo.brilhante.taskmanager.presentation

import app.cash.turbine.test
import com.gustavo.brilhante.ui.DateFormatter
import com.gustavo.brilhante.ui.DateFormatterImpl
import com.gustavo.brilhante.data.models.Task
import com.gustavo.brilhante.data.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TaskManagerViewModelTest {

    private val repository: TaskRepository = mockk()
    private val dateFormatter: DateFormatter = DateFormatterImpl()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: TaskManagerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tasks flow updates from repository`() = runTest {
        val tasks = listOf(Task("T1", "D1", 1000L))
        every { repository.getAllTasksFlow() } returns flowOf(tasks)
        
        viewModel = TaskManagerViewModel(repository, dateFormatter)

        viewModel.tasks.test {
            assertEquals(tasks, awaitItem())
        }
    }

    @Test
    fun `formattedDates flow reflects task due dates`() = runTest {
        val tasks = listOf(Task("T1", "D1", 1700000000000L)) // Nov 14, 2023
        every { repository.getAllTasksFlow() } returns flowOf(tasks)
        
        viewModel = TaskManagerViewModel(repository, dateFormatter)

        viewModel.formattedDates.test {
            val dates = awaitItem()
            assertEquals("Nov 14", dates[0])
        }
    }
}
