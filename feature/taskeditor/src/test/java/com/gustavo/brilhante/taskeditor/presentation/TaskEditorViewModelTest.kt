package com.gustavo.brilhante.taskeditor.presentation

import app.cash.turbine.test
import com.gustavo.brilhante.domain.usecase.AddTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTaskByIdUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceType
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.NotificationScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
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
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: TaskEditorViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TaskEditorViewModel(
            addTaskUseCase, updateTaskUseCase, getTaskByIdUseCase, notificationScheduler
        )
    }

    @After
    fun tearDown() {
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
            tags = listOf("work"),
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
        assertEquals(listOf("work"), state.tags)
        assertTrue(state.isFlagged)
        assertEquals(RecurrenceType.WEEKLY, state.recurrenceType)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadTask with valid id when task not found sets isLoading false`() = runTest {
        coEvery { getTaskByIdUseCase(99L) } returns null

        viewModel.loadTask(99L)

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadTask with same id twice does not reload`() = runTest {
        coEvery { getTaskByIdUseCase(10L) } returns Task(id = 10L, title = "Task")

        viewModel.loadTask(10L)
        viewModel.loadTask(10L)

        coVerify(exactly = 1) { getTaskByIdUseCase(10L) }
    }

    // ── field events ──────────────────────────────────────────────────────────

    @Test
    fun `TitleChanged updates title and clears titleError`() {
        viewModel.onEvent(TaskEditorEvent.Save) // triggers titleError since title is blank
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
        viewModel.onEvent(TaskEditorEvent.ToggleDate) // on
        viewModel.onEvent(TaskEditorEvent.ToggleTime)
        viewModel.onEvent(TaskEditorEvent.RecurrenceChanged(RecurrenceType.WEEKLY))
        viewModel.onEvent(TaskEditorEvent.ToggleDate) // off

        val state = viewModel.uiState.value
        assertFalse(state.hasDate)
        assertFalse(state.hasTime)
        assertEquals(RecurrenceType.NONE, state.recurrenceType)
    }

    @Test
    fun `ToggleTime toggles hasTime`() {
        viewModel.onEvent(TaskEditorEvent.ToggleTime)
        assertTrue(viewModel.uiState.value.hasTime)
        viewModel.onEvent(TaskEditorEvent.ToggleTime)
        assertFalse(viewModel.uiState.value.hasTime)
    }

    @Test
    fun `ToggleUrgent toggles isUrgent`() {
        viewModel.onEvent(TaskEditorEvent.ToggleUrgent)
        assertTrue(viewModel.uiState.value.isUrgent)
        viewModel.onEvent(TaskEditorEvent.ToggleUrgent)
        assertFalse(viewModel.uiState.value.isUrgent)
    }

    @Test
    fun `ToggleFlagged toggles isFlagged`() {
        viewModel.onEvent(TaskEditorEvent.ToggleFlagged)
        assertTrue(viewModel.uiState.value.isFlagged)
    }

    // ── date & time events ────────────────────────────────────────────────────

    @Test
    fun `DueDateChanged stores correct local date from UTC midnight`() {
        // Build UTC midnight for 2026-06-15 explicitly
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val utcMidnight = utcCal.timeInMillis

        viewModel.onEvent(TaskEditorEvent.DueDateChanged(utcMidnight))

        val storedCal = Calendar.getInstance().apply {
            timeInMillis = viewModel.uiState.value.dueDate
        }
        assertEquals(2026, storedCal.get(Calendar.YEAR))
        assertEquals(Calendar.JUNE, storedCal.get(Calendar.MONTH))
        assertEquals(15, storedCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `DueDateChanged preserves hour and minute from previous dueDate`() {
        // Set a known hour/minute via TimeChanged first (on initial date)
        viewModel.onEvent(TaskEditorEvent.TimeChanged(9, 45))

        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        viewModel.onEvent(TaskEditorEvent.DueDateChanged(utcCal.timeInMillis))

        val storedCal = Calendar.getInstance().apply {
            timeInMillis = viewModel.uiState.value.dueDate
        }
        assertEquals(9, storedCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(45, storedCal.get(Calendar.MINUTE))
    }

    @Test
    fun `DueDateChanged hides date picker`() {
        viewModel.onEvent(TaskEditorEvent.ShowDatePicker)
        assertTrue(viewModel.uiState.value.showDatePicker)

        val utcMidnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        viewModel.onEvent(TaskEditorEvent.DueDateChanged(utcMidnight))

        assertFalse(viewModel.uiState.value.showDatePicker)
    }

    @Test
    fun `TimeChanged updates hour and minute on existing date`() {
        // Set a base date
        val utcMidnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        viewModel.onEvent(TaskEditorEvent.DueDateChanged(utcMidnight))

        viewModel.onEvent(TaskEditorEvent.TimeChanged(14, 30))

        val storedCal = Calendar.getInstance().apply {
            timeInMillis = viewModel.uiState.value.dueDate
        }
        assertEquals(14, storedCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, storedCal.get(Calendar.MINUTE))
        assertEquals(0, storedCal.get(Calendar.SECOND))
        // Date must remain unchanged
        assertEquals(15, storedCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `TimeChanged hides time picker`() {
        viewModel.onEvent(TaskEditorEvent.ShowTimePicker)
        assertTrue(viewModel.uiState.value.showTimePicker)

        viewModel.onEvent(TaskEditorEvent.TimeChanged(10, 0))

        assertFalse(viewModel.uiState.value.showTimePicker)
    }

    // ── picker visibility events ───────────────────────────────────────────────

    @Test
    fun `ShowDatePicker and HideDatePicker toggle showDatePicker`() {
        viewModel.onEvent(TaskEditorEvent.ShowDatePicker)
        assertTrue(viewModel.uiState.value.showDatePicker)

        viewModel.onEvent(TaskEditorEvent.HideDatePicker)
        assertFalse(viewModel.uiState.value.showDatePicker)
    }

    @Test
    fun `ShowTimePicker and HideTimePicker toggle showTimePicker`() {
        viewModel.onEvent(TaskEditorEvent.ShowTimePicker)
        assertTrue(viewModel.uiState.value.showTimePicker)

        viewModel.onEvent(TaskEditorEvent.HideTimePicker)
        assertFalse(viewModel.uiState.value.showTimePicker)
    }

    // ── tag events ────────────────────────────────────────────────────────────

    @Test
    fun `TagAdded appends tag to list`() {
        viewModel.onEvent(TaskEditorEvent.TagAdded("work"))
        viewModel.onEvent(TaskEditorEvent.TagAdded("urgent"))
        assertEquals(listOf("work", "urgent"), viewModel.uiState.value.tags)
    }

    @Test
    fun `TagAdded deduplicates tags`() {
        viewModel.onEvent(TaskEditorEvent.TagAdded("work"))
        viewModel.onEvent(TaskEditorEvent.TagAdded("work"))
        assertEquals(listOf("work"), viewModel.uiState.value.tags)
    }

    @Test
    fun `TagRemoved removes the given tag`() {
        viewModel.onEvent(TaskEditorEvent.TagAdded("work"))
        viewModel.onEvent(TaskEditorEvent.TagAdded("home"))
        viewModel.onEvent(TaskEditorEvent.TagRemoved("work"))
        assertEquals(listOf("home"), viewModel.uiState.value.tags)
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
            awaitItem() // navigation event received
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
        coVerify(exactly = 0) { addTaskUseCase(any()) }
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
    fun `Save without hasDate stores null dueDate`() = runTest {
        viewModel.onEvent(TaskEditorEvent.TitleChanged("No date task"))
        // hasDate is false by default

        viewModel.navigationEvent.test {
            viewModel.onEvent(TaskEditorEvent.Save)
            awaitItem()
        }

        coVerify {
            addTaskUseCase(match { it.dueDate == null })
        }
    }

    @Test
    fun `Save with hasDate stores dueDate`() = runTest {
        viewModel.onEvent(TaskEditorEvent.TitleChanged("Dated task"))
        viewModel.onEvent(TaskEditorEvent.ToggleDate)

        viewModel.navigationEvent.test {
            viewModel.onEvent(TaskEditorEvent.Save)
            awaitItem()
        }

        coVerify {
            addTaskUseCase(match { it.dueDate != null })
        }
    }
}
