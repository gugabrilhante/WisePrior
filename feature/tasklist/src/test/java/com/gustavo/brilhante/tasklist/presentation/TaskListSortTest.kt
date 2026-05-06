package com.gustavo.brilhante.tasklist.presentation

import app.cash.turbine.test
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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@kotlinx.coroutines.ExperimentalCoroutinesApi
class TaskListSortTest {

    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk(relaxed = true)
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val getTagsUseCase: GetTagsUseCase = mockk()
    private val addTagUseCase: AddTagUseCase = mockk(relaxed = true)
    private val updateTagUseCase: UpdateTagUseCase = mockk(relaxed = true)
    private val deleteTagUseCase: DeleteTagUseCase = mockk(relaxed = true)
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)
    private val sortPreferences: SortPreferencesDataStore = mockk()
    private val calculateTaskPriority = CalculateTaskPriorityUseCase()
    private val dateFormatter = DateFormatterImpl()

    private val sortOptionFlow = MutableStateFlow(TaskSortOption.SMART_PRIORITY)

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getTagsUseCase() } returns flowOf(emptyList())
        every { sortPreferences.sortOption } returns sortOptionFlow
        coEvery { sortPreferences.setSortOption(any()) } coAnswers {
            sortOptionFlow.value = firstArg()
        }
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

    private val oldTask = Task(id = 1, title = "Old task", createdAt = 1_000L)
    private val newTask = Task(id = 2, title = "New task", createdAt = 9_000L)

    @Test
    fun `CREATED_DESC sorts newest first`() = runTest {
        every { getTasksUseCase() } returns flowOf(listOf(oldTask, newTask))
        sortOptionFlow.value = TaskSortOption.CREATED_DESC
        val viewModel = buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(newTask.id, state.tasks.first().id)
            assertEquals(oldTask.id, state.tasks.last().id)
        }
    }

    @Test
    fun `CREATED_ASC sorts oldest first`() = runTest {
        every { getTasksUseCase() } returns flowOf(listOf(newTask, oldTask))
        sortOptionFlow.value = TaskSortOption.CREATED_ASC
        val viewModel = buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(oldTask.id, state.tasks.first().id)
            assertEquals(newTask.id, state.tasks.last().id)
        }
    }

    @Test
    fun `SMART_PRIORITY sorts urgent tasks first`() = runTest {
        val urgentTask = Task(id = 3, title = "Urgent", isUrgent = true, createdAt = 500L)
        val normalTask = Task(id = 4, title = "Normal", createdAt = 800L)
        every { getTasksUseCase() } returns flowOf(listOf(normalTask, urgentTask))
        sortOptionFlow.value = TaskSortOption.SMART_PRIORITY
        val viewModel = buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(urgentTask.id, state.tasks.first().id)
        }
    }

    @Test
    fun `SMART_PRIORITY puts completed tasks last`() = runTest {
        val completedTask = Task(id = 5, title = "Done", isCompleted = true, createdAt = 9_000L)
        val activeTask = Task(id = 6, title = "Active", createdAt = 1_000L)
        every { getTasksUseCase() } returns flowOf(listOf(completedTask, activeTask))
        sortOptionFlow.value = TaskSortOption.SMART_PRIORITY
        val viewModel = buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(activeTask.id, state.tasks.first().id)
            assertEquals(completedTask.id, state.tasks.last().id)
        }
    }

    @Test
    fun `setSortOption persists and re-sorts list`() = runTest {
        every { getTasksUseCase() } returns flowOf(listOf(oldTask, newTask))
        sortOptionFlow.value = TaskSortOption.CREATED_DESC
        val viewModel = buildViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(newTask.id, initial.tasks.first().id)

            viewModel.setSortOption(TaskSortOption.CREATED_ASC)
            val updated = awaitItem()
            assertEquals(oldTask.id, updated.tasks.first().id)
            assertEquals(TaskSortOption.CREATED_ASC, updated.sortOption)
        }
    }

    @Test
    fun `HIGH priority task ranks above MEDIUM in smart sort`() = runTest {
        val highTask = Task(id = 7, title = "High", priority = Priority.HIGH, createdAt = 100L)
        val mediumTask = Task(id = 8, title = "Medium", priority = Priority.MEDIUM, createdAt = 900L)
        every { getTasksUseCase() } returns flowOf(listOf(mediumTask, highTask))
        sortOptionFlow.value = TaskSortOption.SMART_PRIORITY
        val viewModel = buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(highTask.id, state.tasks.first().id)
        }
    }
}
