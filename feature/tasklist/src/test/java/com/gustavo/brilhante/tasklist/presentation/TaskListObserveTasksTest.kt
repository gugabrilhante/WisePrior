package com.gustavo.brilhante.tasklist.presentation

import com.gustavo.brilhante.ui.DateFormatter
import com.gustavo.brilhante.domain.usecase.AddTagUseCase
import com.gustavo.brilhante.domain.usecase.CalculateTaskPriorityUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTagUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.model.TaskSortOption
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.tasklist.data.SortPreferencesDataStore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Tests for the observeTasks flow inside [TaskListViewModel], specifically the error
 * path handled by the .catch operator, which was 0% covered in JaCoCo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskListObserveTasksTest {

    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk(relaxed = true)
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val getTagsUseCase: GetTagsUseCase = mockk()
    private val addTagUseCase: AddTagUseCase = mockk(relaxed = true)
    private val updateTagUseCase: UpdateTagUseCase = mockk(relaxed = true)
    private val deleteTagUseCase: DeleteTagUseCase = mockk(relaxed = true)
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)
    private val dateFormatter: DateFormatter = mockk(relaxed = true)
    private val sortPreferences: SortPreferencesDataStore = mockk()
    private val calculateTaskPriority = CalculateTaskPriorityUseCase()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getTagsUseCase() } returns flowOf(emptyList())
        every { sortPreferences.sortOption } returns flowOf(TaskSortOption.CREATED_DESC)
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

    // ── .catch block (0% JaCoCo before this test) ─────────────────────────────

    @Test
    fun `given upstream flow throws, catch handler sets error message in uiState`() = runTest(testDispatcher) {
        every { getTasksUseCase() } returns flow { throw RuntimeException("DB crash") }

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals("DB crash", viewModel.uiState.value.error)
    }

    @Test
    fun `given upstream flow throws, catch handler sets isLoading to false`() = runTest(testDispatcher) {
        every { getTasksUseCase() } returns flow { throw RuntimeException("Connection error") }

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `given upstream flow throws with null message, error field reflects that`() = runTest(testDispatcher) {
        every { getTasksUseCase() } returns flow<List<Task>> { throw RuntimeException() }

        val viewModel = buildViewModel()
        advanceUntilIdle()

        // RuntimeException with no message has null message — verify error state is set
        assertNotNull(viewModel.uiState.value.error.let { it }) // error field is set (possibly null string)
    }

    @Test
    fun `given tags flow throws, catch handler still fires and sets error`() = runTest(testDispatcher) {
        every { getTasksUseCase() } returns flowOf(emptyList())
        every { getTagsUseCase() } returns flow { throw RuntimeException("Tags DB error") }

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals("Tags DB error", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ── Happy path (no exception) verifies normal state ───────────────────────

    @Test
    fun `given tasks flow emits normally, error is null and tasks are set`() = runTest(testDispatcher) {
        val tasks = listOf(Task(id = 1L, title = "Normal task"))
        every { getTasksUseCase() } returns flowOf(tasks)

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals(tasks, viewModel.uiState.value.tasks)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `given tasks flow emits then the flow updates, state stays correct`() = runTest(testDispatcher) {
        val tasksFlow = MutableStateFlow<List<Task>>(emptyList())
        every { getTasksUseCase() } returns tasksFlow

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals(0, viewModel.uiState.value.tasks.size)

        val newTask = Task(id = 2L, title = "New")
        tasksFlow.value = listOf(newTask)
        advanceUntilIdle()

        assertEquals(listOf(newTask), viewModel.uiState.value.tasks)
        assertNull(viewModel.uiState.value.error)
    }
}
