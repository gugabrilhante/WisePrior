package com.gustavo.brilhante.taskeditor.presentation

import app.cash.turbine.test
import com.gustavo.brilhante.common.DateFormatter
import com.gustavo.brilhante.domain.usecase.AddTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTaskByIdUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceType
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.NotificationScheduler
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class TaskEditorViewModelTest {

    private val addTaskUseCase: AddTaskUseCase = mockk(relaxed = true)
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val getTaskByIdUseCase: GetTaskByIdUseCase = mockk()
    private val getTagsUseCase: GetTagsUseCase = mockk()
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)
    private val dateFormatter: DateFormatter = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()
    private var originalTimeZone: TimeZone? = null

    private lateinit var viewModel: TaskEditorViewModel

    @Before
    fun setup() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Dispatchers.setMain(testDispatcher)
        every { getTagsUseCase() } returns flowOf(emptyList())
        every { dateFormatter.formatDate(any()) } returns "Mon, Jan 1, 2024"
        every { dateFormatter.formatTime(any()) } returns "10:00"
        viewModel = TaskEditorViewModel(
            addTaskUseCase, updateTaskUseCase, getTaskByIdUseCase,
            getTagsUseCase, notificationScheduler, dateFormatter
        )
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
        Dispatchers.resetMain()
    }

    // ── loadTask ─────────────────────────────────────────────────────────────

    @Test
    fun `loadTask with negative id resets state to defaults`() = runTest {
        viewModel.onEvent(TaskEditorEvent.TitleChanged("some title"))
        viewModel.loadTask(-1L)

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertFalse(state.hasDate)
        assertFalse(state.hasTime)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadTask with valid id populates uiState from task`() = runTest {
        val task = Task(
            id = 10L,
            title = "Meeting",
            notes = "With team",
            url = "https://meet.example.com",
            dueDate = 1_700_000_000_000L,
            hasTime = true,
            isUrgent = true,
            priority = Priority.HIGH,
            tagIds = listOf(1L, 2L),
            isFlagged = true,
            recurrenceType = RecurrenceType.WEEKLY
        )
        coEvery { getTaskByIdUseCase(10L) } returns task

        viewModel.loadTask(10L)

        val state = viewModel.uiState.value
        assertEquals("Meeting", state.title)
        assertEquals("With team", state.notes)
        assertEquals("https://meet.example.com", state.url)
        assertTrue(state.hasDate)
        assertTrue(state.hasTime)
        assertTrue(state.isUrgent)
        assertEquals(Priority.HIGH, state.priority)
        assertEquals(setOf(1L, 2L), state.selectedTagIds)
        assertTrue(state.isFlagged)
        assertEquals(RecurrenceType.WEEKLY, state.recurrenceType)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadTask with same id twice does not reload and preserves state`() = runTest {
        val task = Task(id = 10L, title = "Task")
        coEvery { getTaskByIdUseCase(10L) } returns task

        viewModel.loadTask(10L)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Task", viewModel.uiState.value.title)

        viewModel.loadTask(10L)

        coVerify(exactly = 1) { getTaskByIdUseCase(10L) }
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Task", viewModel.uiState.value.title)
    }

    // ── field events ──────────────────────────────────────────────────────────

    @Test
    fun `TitleChanged updates title and clears titleError`() {
        viewModel.onEvent(TaskEditorEvent.Save)
        assertNotNull(viewModel.uiState.value.titleError)

        viewModel.onEvent(TaskEditorEvent.TitleChanged("My Task"))

        assertEquals("My Task", viewModel.uiState.value.title)
        assertNull(viewModel.uiState.value.titleError)
    }

    @Test
    fun `NotesChanged updates notes`() {
        viewModel.onEvent(TaskEditorEvent.NotesChanged("Some notes"))
        assertEquals("Some notes", viewModel.uiState.value.notes)
    }

    @Test
    fun `UrlChanged updates url`() {
        viewModel.onEvent(TaskEditorEvent.UrlChanged("https://example.com"))
        assertEquals("https://example.com", viewModel.uiState.value.url)
    }

    @Test
    fun `PriorityChanged updates priority`() {
        viewModel.onEvent(TaskEditorEvent.PriorityChanged(Priority.MEDIUM))
        assertEquals(Priority.MEDIUM, viewModel.uiState.value.priority)
    }

    @Test
    fun `RecurrenceChanged updates recurrenceType`() {
        viewModel.onEvent(TaskEditorEvent.RecurrenceChanged(RecurrenceType.DAILY))
        assertEquals(RecurrenceType.DAILY, viewModel.uiState.value.recurrenceType)
    }

    // ── toggle events ─────────────────────────────────────────────────────────

    @Test
    fun `ToggleDate turns hasDate on`() {
        assertFalse(viewModel.uiState.value.hasDate)
        viewModel.onEvent(TaskEditorEvent.ToggleDate)
        assertTrue(viewModel.uiState.value.hasDate)
    }

    @Test
    fun `ToggleDate turning off resets hasTime and recurrenceType`() {
        viewModel.onEvent(TaskEditorEvent.ToggleDate)
        viewModel.onEvent(TaskEditorEvent.ToggleTime)
        viewModel.onEvent(TaskEditorEvent.RecurrenceChanged(RecurrenceType.WEEKLY))
        viewModel.onEvent(TaskEditorEvent.ToggleDate)

        val state = viewModel.uiState.value
        assertFalse(state.hasDate)
        assertFalse(state.hasTime)
        assertEquals(RecurrenceType.NONE, state.recurrenceType)
    }

    @Test
    fun `ToggleUrgent toggles isUrgent`() {
        viewModel.onEvent(TaskEditorEvent.ToggleUrgent)
        assertTrue(viewModel.uiState.value.isUrgent)
        viewModel.onEvent(TaskEditorEvent.ToggleUrgent)
        assertFalse(viewModel.uiState.value.isUrgent)
    }

    // ── formatted date/time ───────────────────────────────────────────────────

    @Test
    fun `ToggleDate on populates formattedDate`() {
        viewModel.onEvent(TaskEditorEvent.ToggleDate)
        assertNotNull(viewModel.uiState.value.formattedDate)
    }

    // ── tag selection ─────────────────────────────────────────────────────────

    @Test
    fun `onTagSelected adds tagId to selectedTagIds`() {
        viewModel.onTagSelected(1L)
        viewModel.onTagSelected(2L)
        assertEquals(setOf(1L, 2L), viewModel.uiState.value.selectedTagIds)
    }

    @Test
    fun `onTagRemoved removes tagId from selectedTagIds`() {
        viewModel.onTagSelected(1L)
        viewModel.onTagSelected(2L)
        viewModel.onTagRemoved(1L)
        assertEquals(setOf(2L), viewModel.uiState.value.selectedTagIds)
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    fun `Save with blank title sets titleError`() = runTest {
        viewModel.onEvent(TaskEditorEvent.Save)
        assertNotNull(viewModel.uiState.value.titleError)
    }

    @Test
    fun `Save new task calls addTaskUseCase and emits navigation event`() = runTest {
        viewModel.onEvent(TaskEditorEvent.TitleChanged("New task"))

        viewModel.navigationEvent.test {
            viewModel.onEvent(TaskEditorEvent.Save)
            awaitItem()
        }

        coVerify(exactly = 1) { addTaskUseCase(any()) }
    }

    @Test
    fun `Save existing task calls updateTaskUseCase`() = runTest {
        val task = Task(id = 10L, title = "Existing")
        coEvery { getTaskByIdUseCase(10L) } returns task
        viewModel.loadTask(10L)

        viewModel.navigationEvent.test {
            viewModel.onEvent(TaskEditorEvent.Save)
            awaitItem()
        }

        coVerify(exactly = 1) { updateTaskUseCase(any()) }
    }

    @Test
    fun `Save trims title and notes whitespace`() = runTest {
        viewModel.onEvent(TaskEditorEvent.TitleChanged("  My Task  "))
        viewModel.onEvent(TaskEditorEvent.NotesChanged("  Some notes  "))

        viewModel.navigationEvent.test {
            viewModel.onEvent(TaskEditorEvent.Save)
            awaitItem()
        }

        coVerify {
            addTaskUseCase(match { it.title == "My Task" && it.notes == "Some notes" })
        }
    }

    @Test
    fun `Save with hasDate stores exact dueDate and time`() = runTest {
        val testDate = 1_700_000_000_000L
        viewModel.onEvent(TaskEditorEvent.TitleChanged("Dated task"))
        viewModel.onEvent(TaskEditorEvent.ToggleDate)
        viewModel.onEvent(TaskEditorEvent.DueDateChanged(testDate))
        viewModel.onEvent(TaskEditorEvent.TimeChanged(15, 45))

        val expectedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = testDate
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 45)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val expectedMillis = expectedCal.timeInMillis

        viewModel.navigationEvent.test {
            viewModel.onEvent(TaskEditorEvent.Save)
            awaitItem()
        }

        coVerify {
            addTaskUseCase(match { it.dueDate == expectedMillis })
        }
    }
}
