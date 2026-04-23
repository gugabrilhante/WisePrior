package com.gustavo.brilhante.tasklist.presentation

import app.cash.turbine.test
import com.gustavo.brilhante.domain.usecase.DeleteTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.NotificationScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TaskListViewModelTest {

    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk(relaxed = true)
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): TaskListViewModel {
        return TaskListViewModel(getTasksUseCase, deleteTaskUseCase, notificationScheduler)
    }

    @Test
    fun `initial state has isLoading true`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val viewModel = buildViewModel()

        // After init with UnconfinedTestDispatcher the flow already collected
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loading tasks updates uiState tasks list`() = runTest {
        val tasks = listOf(
            Task(id = 1, title = "Task A", priority = Priority.HIGH),
            Task(id = 2, title = "Task B")
        )
        every { getTasksUseCase() } returns flowOf(tasks)

        val viewModel = buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(tasks, state.tasks)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `uiState reacts to multiple flow emissions`() = runTest {
        val tasksFlow = MutableStateFlow<List<Task>>(emptyList())
        every { getTasksUseCase() } returns tasksFlow

        val viewModel = buildViewModel()

        viewModel.uiState.test {
            // First emission: empty
            assertEquals(emptyList<Task>(), awaitItem().tasks)

            // Second emission: one task added
            val task = Task(id = 1, title = "New task")
            tasksFlow.value = listOf(task)
            assertEquals(listOf(task), awaitItem().tasks)
        }
    }

    @Test
    fun `deleteTask calls deleteTaskUseCase and cancels notification`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val task = Task(id = 5, title = "Delete me")
        val viewModel = buildViewModel()

        viewModel.deleteTask(task)

        coVerify(exactly = 1) { deleteTaskUseCase(task) }
        verify(exactly = 1) { notificationScheduler.cancel(5L) }
    }

    @Test
    fun `deleteTask failure updates error in uiState`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        coEvery { deleteTaskUseCase(any()) } throws RuntimeException("DB error")
        val task = Task(id = 1, title = "Failing task")
        val viewModel = buildViewModel()

        viewModel.deleteTask(task)

        assertEquals("DB error", viewModel.uiState.value.error)
    }
}
