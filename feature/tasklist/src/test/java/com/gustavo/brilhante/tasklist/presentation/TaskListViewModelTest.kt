package com.gustavo.brilhante.tasklist.presentation

import app.cash.turbine.test
import com.gustavo.brilhante.domain.time.CalendarProvider
import com.gustavo.brilhante.domain.time.ClockProvider
import com.gustavo.brilhante.domain.usecase.AddTagUseCase
import com.gustavo.brilhante.domain.usecase.CalculateTaskPriorityUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTagUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.model.TaskSortOption
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.tasklist.data.SortPreferencesDataStore
import com.gustavo.brilhante.ui.DateFormatterImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk(relaxed = true)
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val getTagsUseCase: GetTagsUseCase = mockk()
    private val addTagUseCase: AddTagUseCase = mockk(relaxed = true)
    private val updateTagUseCase: UpdateTagUseCase = mockk(relaxed = true)
    private val deleteTagUseCase: DeleteTagUseCase = mockk(relaxed = true)
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)
    private val sortPreferences: SortPreferencesDataStore = mockk()
    private val clockProvider: ClockProvider = mockk()
    private val calendarProvider: CalendarProvider = mockk()
    private val calculateTaskPriority = CalculateTaskPriorityUseCase(clockProvider)
    private lateinit var dateFormatter: DateFormatterImpl

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { clockProvider.currentTimeMillis() } returns 1000L
        every { calendarProvider.getInstance() } answers { Calendar.getInstance() }
        every { calendarProvider.getInstance(any<TimeZone>()) } answers { Calendar.getInstance(it.invocation.args[0] as TimeZone) }
        
        dateFormatter = DateFormatterImpl(calendarProvider)
        
        every { getTagsUseCase() } returns flowOf(emptyList())
        every { sortPreferences.sortOption } returns flowOf(TaskSortOption.SMART_PRIORITY)
        coEvery { sortPreferences.setSortOption(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): TaskListViewModel = TaskListViewModel(
        getTasksUseCase, deleteTaskUseCase, updateTaskUseCase, getTagsUseCase,
        addTagUseCase, updateTagUseCase, deleteTagUseCase,
        notificationScheduler, dateFormatter, sortPreferences, calculateTaskPriority
    )

    @Test
    fun `initial state has isLoading false`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val viewModel = buildViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loading tasks updates uiState tasks list`() = runTest {
        val tasks = listOf(
            Task(id = 1, title = "Task A", priority = Priority.HIGH, createdAt = 2_000L),
            Task(id = 2, title = "Task B", createdAt = 1_000L)
        )
        every { getTasksUseCase() } returns flowOf(tasks)

        val viewModel = buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            // Default sort is SMART_PRIORITY, Task A (High Priority) comes first
            assertEquals(2, state.tasks.size)
            assertEquals(1L, state.tasks[0].id)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `uiState reacts to multiple flow emissions`() = runTest {
        val tasksFlow = MutableStateFlow<List<Task>>(emptyList())
        every { getTasksUseCase() } returns tasksFlow

        val viewModel = buildViewModel()

        viewModel.uiState.test {
            assertEquals(emptyList<Task>(), awaitItem().tasks)

            val task = Task(id = 1, title = "New task", createdAt = 1000L)
            tasksFlow.value = listOf(task)
            assertEquals(listOf(task), awaitItem().tasks)
        }
    }

    @Test
    fun `deleteTask calls deleteTaskUseCase and cancels notification`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val task = Task(id = 5, title = "Delete me", createdAt = 1000L)
        val viewModel = buildViewModel()

        viewModel.deleteTask(task)

        coVerify(exactly = 1) { deleteTaskUseCase(task) }
        verify(exactly = 1) { notificationScheduler.cancel(5L) }
    }

    @Test
    fun `deleteTask failure updates error in uiState`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        coEvery { deleteTaskUseCase(any()) } throws RuntimeException("DB error")
        val task = Task(id = 1, title = "Failing task", createdAt = 1000L)
        val viewModel = buildViewModel()

        viewModel.deleteTask(task)

        assertEquals("DB error", viewModel.uiState.value.error)
    }

    @Test
    fun `onTaskCheckedChange with true marks task complete and cancels notification`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val task = Task(id = 7, title = "Finish report", createdAt = 1000L)
        val viewModel = buildViewModel()

        viewModel.onTaskCheckedChange(task, true)

        coVerify(exactly = 1) { updateTaskUseCase(task.copy(isCompleted = true)) }
        verify(exactly = 1) { notificationScheduler.cancel(7L) }
    }

    @Test
    fun `onTaskCheckedChange with false marks task incomplete without cancelling notification`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val task = Task(id = 8, title = "Review PR", isCompleted = true, createdAt = 1000L)
        val viewModel = buildViewModel()

        viewModel.onTaskCheckedChange(task, false)

        coVerify(exactly = 1) { updateTaskUseCase(task.copy(isCompleted = false)) }
        verify(exactly = 0) { notificationScheduler.cancel(any()) }
    }

    @Test
    fun `onTaskCheckedChange failure updates error in uiState`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        coEvery { updateTaskUseCase(any()) } throws RuntimeException("Update failed")
        val task = Task(id = 9, title = "Crash task", createdAt = 1000L)
        val viewModel = buildViewModel()

        viewModel.onTaskCheckedChange(task, true)

        assertEquals("Update failed", viewModel.uiState.value.error)
    }
}
