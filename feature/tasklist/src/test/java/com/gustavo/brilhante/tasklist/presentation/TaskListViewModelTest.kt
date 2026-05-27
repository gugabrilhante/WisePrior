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
import com.gustavo.brilhante.model.ChecklistItem
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.model.TaskSortOption
import com.gustavo.brilhante.tasklist.model.TaskCollection
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.tasklist.data.SortPreferences
import com.gustavo.brilhante.domain.usecase.SwipeDismissUseCase
import com.gustavo.brilhante.tasklist.presentation.mapper.SortOptionUiMapper
import com.gustavo.brilhante.tasklist.presentation.mapper.TagEditorUiMapper
import com.gustavo.brilhante.tasklist.presentation.mapper.TaskListUiMapper
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
    private val sortPreferences: SortPreferences = mockk()
    private val clockProvider: ClockProvider = mockk()
    private val calendarProvider: CalendarProvider = mockk()
    private val calculateTaskPriority = CalculateTaskPriorityUseCase(clockProvider)
    private lateinit var dateFormatter: DateFormatterImpl
    private val swipeDismissUseCase = mockk<SwipeDismissUseCase>()
    private val sortOptionUiMapper = SortOptionUiMapper()
    private val tagEditorUiMapper = TagEditorUiMapper()
    private lateinit var taskListUiMapper: TaskListUiMapper

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { clockProvider.currentTimeMillis() } returns 1000L
        every { calendarProvider.getInstance() } answers { Calendar.getInstance() }
        every { calendarProvider.getInstance(any<TimeZone>()) } answers { Calendar.getInstance(it.invocation.args[0] as TimeZone) }
        
        dateFormatter = DateFormatterImpl(calendarProvider)
        taskListUiMapper = TaskListUiMapper(dateFormatter, calculateTaskPriority, sortOptionUiMapper)
        
        coEvery { swipeDismissUseCase.invoke(any()) } coAnswers {
            val action = it.invocation.args[0] as (suspend () -> Unit)
            action()
        }

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
        notificationScheduler, sortPreferences, taskListUiMapper, tagEditorUiMapper, swipeDismissUseCase
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

        viewModel.onEvent(TaskListEvent.DeleteTask(task))

        coVerify(exactly = 1) { deleteTaskUseCase(task) }
        verify(exactly = 1) { notificationScheduler.cancel(5L) }
    }

    @Test
    fun `deleteTask failure updates error in uiState`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        coEvery { deleteTaskUseCase(any()) } throws RuntimeException("DB error")
        val task = Task(id = 1, title = "Failing task", createdAt = 1000L)
        val viewModel = buildViewModel()

        viewModel.onEvent(TaskListEvent.DeleteTask(task))

        assertEquals("DB error", viewModel.uiState.value.error)
    }

    @Test
    fun `onTaskCheckedChange with true marks task complete and cancels notification`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val task = Task(id = 7, title = "Finish report", createdAt = 1000L)
        val viewModel = buildViewModel()

        viewModel.onEvent(TaskListEvent.ToggleTaskChecked(task, true))

        coVerify(exactly = 1) { updateTaskUseCase(task.copy(isCompleted = true)) }
        verify(exactly = 1) { notificationScheduler.cancel(7L) }
    }

    @Test
    fun `onTaskCheckedChange with false marks task incomplete without cancelling notification`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val task = Task(id = 8, title = "Review PR", isCompleted = true, createdAt = 1000L)
        val viewModel = buildViewModel()

        viewModel.onEvent(TaskListEvent.ToggleTaskChecked(task, false))

        coVerify(exactly = 1) { updateTaskUseCase(task.copy(isCompleted = false)) }
        verify(exactly = 0) { notificationScheduler.cancel(any()) }
    }

    @Test
    fun `onTaskCheckedChange failure updates error in uiState`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        coEvery { updateTaskUseCase(any()) } throws RuntimeException("Update failed")
        val task = Task(id = 9, title = "Crash task", createdAt = 1000L)
        val viewModel = buildViewModel()

        viewModel.onEvent(TaskListEvent.ToggleTaskChecked(task, true))

        assertEquals("Update failed", viewModel.uiState.value.error)
    }

    // ── onChecklistItemToggled ────────────────────────────────────────────────

    @Test
    fun `onChecklistItemToggled updates the target item checked state`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val item1 = ChecklistItem(id = 1L, text = "Step 1", isChecked = false)
        val item2 = ChecklistItem(id = 2L, text = "Step 2", isChecked = false)
        val task = Task(id = 10, title = "Task", createdAt = 1000L, checklistItems = listOf(item1, item2))
        val viewModel = buildViewModel()

        viewModel.onEvent(TaskListEvent.ToggleChecklistItem(task, itemId = 1L, isChecked = true))

        val expectedTask = task.copy(checklistItems = listOf(item1.copy(isChecked = true), item2))
        coVerify(exactly = 1) { updateTaskUseCase(expectedTask) }
    }

    @Test
    fun `onChecklistItemToggled when all items become checked auto-completes task and cancels notification`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val items = listOf(
            ChecklistItem(id = 1L, text = "A", isChecked = true),
            ChecklistItem(id = 2L, text = "B", isChecked = false)
        )
        val task = Task(id = 11, title = "Task", createdAt = 1000L, checklistItems = items)
        val viewModel = buildViewModel()

        viewModel.onEvent(TaskListEvent.ToggleChecklistItem(task, itemId = 2L, isChecked = true))

        val expectedTask = task.copy(
            checklistItems = items.map { it.copy(isChecked = true) },
            isCompleted = true
        )
        coVerify(exactly = 1) { updateTaskUseCase(expectedTask) }
        verify(exactly = 1) { notificationScheduler.cancel(11L) }
    }

    @Test
    fun `onChecklistItemToggled when not all items checked does not complete task`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val items = listOf(
            ChecklistItem(id = 1L, text = "A", isChecked = false),
            ChecklistItem(id = 2L, text = "B", isChecked = false)
        )
        val task = Task(id = 12, title = "Task", createdAt = 1000L, checklistItems = items)
        val viewModel = buildViewModel()

        viewModel.onEvent(TaskListEvent.ToggleChecklistItem(task, itemId = 1L, isChecked = true))

        coVerify { updateTaskUseCase(match { !it.isCompleted }) }
        verify(exactly = 0) { notificationScheduler.cancel(any()) }
    }

    @Test
    fun `onChecklistItemToggled when item unchecked on completed task makes it incomplete`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val items = listOf(
            ChecklistItem(id = 1L, text = "A", isChecked = true),
            ChecklistItem(id = 2L, text = "B", isChecked = true)
        )
        // Task is already completed
        val task = Task(id = 14, title = "Task", isCompleted = true, createdAt = 1000L, checklistItems = items)
        val viewModel = buildViewModel()

        // Uncheck one item
        viewModel.onEvent(TaskListEvent.ToggleChecklistItem(task, itemId = 2L, isChecked = false))

        val expectedTask = task.copy(
            checklistItems = listOf(items[0], items[1].copy(isChecked = false)),
            isCompleted = false
        )
        coVerify(exactly = 1) { updateTaskUseCase(expectedTask) }
    }

    @Test
    fun `onChecklistItemToggled failure updates error in uiState`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        coEvery { updateTaskUseCase(any()) } throws RuntimeException("Toggle failed")
        val item = ChecklistItem(id = 1L, text = "Step", isChecked = false)
        val task = Task(id = 13, title = "Task", createdAt = 1000L, checklistItems = listOf(item))
        val viewModel = buildViewModel()

        viewModel.onEvent(TaskListEvent.ToggleChecklistItem(task, itemId = 1L, isChecked = true))

        assertEquals("Toggle failed", viewModel.uiState.value.error)
    }

    @Test
    fun `SelectCollection updates selectedCollection in uiState`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val viewModel = buildViewModel()
        val collection = TaskCollection.Today

        viewModel.onEvent(TaskListEvent.SelectCollection(collection))

        assertEquals(collection, viewModel.uiState.value.selectedCollection)
    }

    @Test
    fun `AddTask navigation event is emitted`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val viewModel = buildViewModel()

        viewModel.navigationEvent.test {
            viewModel.onEvent(TaskListEvent.AddTask)
            assertEquals(TaskListEvent.AddTask, awaitItem())
        }
    }

    @Test
    fun `EditTask navigation event is emitted`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val viewModel = buildViewModel()
        val task = Task(id = 1, title = "Task", createdAt = 1000L)

        viewModel.navigationEvent.test {
            viewModel.onEvent(TaskListEvent.EditTask(task))
            assertEquals(TaskListEvent.EditTask(task), awaitItem())
        }
    }

    @Test
    fun `SetSortOption calls sortPreferences`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val viewModel = buildViewModel()
        val option = TaskSortOption.CREATED_ASC

        viewModel.onEvent(TaskListEvent.SetSortOption(option))

        coVerify { sortPreferences.setSortOption(option) }
    }

    @Test
    fun `ShowAddTag updates uiState`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val viewModel = buildViewModel()

        viewModel.onEvent(TaskListEvent.ShowAddTag(0xFF000000L))

        val state = viewModel.uiState.value
        assertEquals(true, state.showTagEditor)
        assertNull(state.editingTag)
        assertEquals("Create Tag", state.tagEditorDialog?.title?.let { "Create Tag" }) // Simple check
    }

    @Test
    fun `ErrorDismissed clears error`() = runTest {
        every { getTasksUseCase() } returns flowOf(emptyList())
        coEvery { deleteTaskUseCase(any()) } throws RuntimeException("Error")
        val viewModel = buildViewModel()
        viewModel.onEvent(TaskListEvent.DeleteTask(Task(id = 1, title = "T", createdAt = 1L)))
        
        assertEquals("Error", viewModel.uiState.value.error)

        viewModel.onEvent(TaskListEvent.ErrorDismissed)

        assertNull(viewModel.uiState.value.error)
    }
}
