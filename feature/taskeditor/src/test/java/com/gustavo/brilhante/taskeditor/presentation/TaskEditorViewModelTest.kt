package com.gustavo.brilhante.taskeditor.presentation

import app.cash.turbine.test
import com.gustavo.brilhante.domain.time.CalendarProvider
import com.gustavo.brilhante.domain.time.ClockProvider
import com.gustavo.brilhante.domain.usecase.AddTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTaskByIdUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.ui.DateFormatterImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class TaskEditorViewModelTest {

    private val addTaskUseCase: AddTaskUseCase = mockk(relaxed = true)
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val getTaskByIdUseCase: GetTaskByIdUseCase = mockk()
    private val getTagsUseCase: GetTagsUseCase = mockk()
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)
    private val clockProvider: ClockProvider = mockk()
    private val calendarProvider: CalendarProvider = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()
    private var originalTimeZone: TimeZone? = null

    private lateinit var viewModel: TaskEditorViewModel

    @Before
    fun setup() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Dispatchers.setMain(testDispatcher)

        every { clockProvider.currentTimeMillis() } returns 1_000_000_000L
        every { calendarProvider.getInstance() } answers { Calendar.getInstance() }
        every { calendarProvider.getInstance(any<TimeZone>()) } answers { Calendar.getInstance(it.invocation.args[0] as TimeZone) }

        val realFormatter = DateFormatterImpl(calendarProvider)

        every { getTagsUseCase() } returns flowOf(emptyList())
        viewModel = TaskEditorViewModel(
            addTaskUseCase, updateTaskUseCase, getTaskByIdUseCase,
            getTagsUseCase, notificationScheduler, realFormatter, clockProvider
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
        assertFalse(state.dateSection.hasDate)
        assertFalse(state.dateSection.hasTime)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadTask with negative id called again preserves in-progress draft`() = runTest {
        viewModel.loadTask(-1L)
        viewModel.onEvent(TaskEditorEvent.TitleChanged("draft"))
        viewModel.loadTask(-1L)

        assertEquals("draft", viewModel.uiState.value.title)
    }

    @Test
    fun `loadTask with valid id populates uiState from task`() = runTest {
        val rule = RecurrenceRule(RecurrenceUnit.WEEKS, 1)
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
            recurrenceRule = rule,
            createdAt = 1000L
        )
        coEvery { getTaskByIdUseCase(10L) } returns task
        val tags = listOf(
            com.gustavo.brilhante.model.Tag(1L, "T1", 0L),
            com.gustavo.brilhante.model.Tag(2L, "T2", 0L)
        )
        every { getTagsUseCase() } returns flowOf(tags)
        
        viewModel = TaskEditorViewModel(
            addTaskUseCase, updateTaskUseCase, getTaskByIdUseCase,
            getTagsUseCase, notificationScheduler, DateFormatterImpl(calendarProvider), clockProvider
        )

        viewModel.loadTask(10L)

        val state = viewModel.uiState.value
        assertEquals("Meeting", state.title)
        assertEquals("With team", state.notes)
        assertEquals("https://meet.example.com", state.url)
        assertTrue(state.dateSection.hasDate)
        assertTrue(state.dateSection.hasTime)
        assertTrue(state.isUrgent)
        assertEquals(Priority.HIGH, state.priority)
        assertEquals(2, state.tagSection.tags.filter { it.isSelected }.size)
        assertTrue(state.isFlagged)
        assertEquals(rule, state.recurrenceRule)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadTask with same id twice does not reload and preserves state`() = runTest {
        val task = Task(id = 10L, title = "Task", createdAt = 1000L)
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
    fun `ToggleRecurrence toggles recurrenceRule`() {
        assertFalse(viewModel.uiState.value.recurrenceRule.isRecurring)
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)
        assertTrue(viewModel.uiState.value.recurrenceRule.isRecurring)
        assertEquals(RecurrenceUnit.DAYS, viewModel.uiState.value.recurrenceRule.unit)
        assertEquals(1, viewModel.uiState.value.recurrenceRule.interval)
        
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)
        assertFalse(viewModel.uiState.value.recurrenceRule.isRecurring)
    }

    // ── toggle events ─────────────────────────────────────────────────────────

    @Test
    fun `ToggleDate turns hasDate on`() {
        assertFalse(viewModel.uiState.value.dateSection.hasDate)
        viewModel.onEvent(TaskEditorEvent.ToggleDate)
        assertTrue(viewModel.uiState.value.dateSection.hasDate)
    }

    @Test
    fun `ToggleDate turning off resets hasTime and recurrenceRule`() {
        viewModel.onEvent(TaskEditorEvent.ToggleDate)
        viewModel.onEvent(TaskEditorEvent.ToggleTime)
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)
        viewModel.onEvent(TaskEditorEvent.ToggleDate)

        val state = viewModel.uiState.value
        assertFalse(state.dateSection.hasDate)
        assertFalse(state.dateSection.hasTime)
        assertFalse(state.recurrenceRule.isRecurring)
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
        assertNotNull(viewModel.uiState.value.dateSection.formattedDate)
    }

    // ── tag selection ─────────────────────────────────────────────────────────

    @Test
    fun `TagClicked toggles tag selection`() = runTest {
        val tag1 = com.gustavo.brilhante.model.Tag(1L, "Work", 0xFF000000L)
        every { getTagsUseCase() } returns flowOf(listOf(tag1))
        
        // Re-initialize to collect the new flow
        viewModel = TaskEditorViewModel(
            addTaskUseCase, updateTaskUseCase, getTaskByIdUseCase,
            getTagsUseCase, notificationScheduler, DateFormatterImpl(calendarProvider), clockProvider
        )

        viewModel.onEvent(TaskEditorEvent.TagClicked(1L))
        assertTrue(viewModel.uiState.value.tagSection.tags.find { it.id == 1L }?.isSelected == true)
        
        viewModel.onEvent(TaskEditorEvent.TagClicked(1L))
        assertTrue(viewModel.uiState.value.tagSection.tags.find { it.id == 1L }?.isSelected == false)
    }

    // ── dialogs ───────────────────────────────────────────────────────────────

    @Test
    fun `ShowDialog updates dialogState`() {
        viewModel.onEvent(TaskEditorEvent.ShowDialog(ActiveDialog.DatePicker))
        assertEquals(ActiveDialog.DatePicker, viewModel.uiState.value.dialogState.activeDialog)
        
        viewModel.onEvent(TaskEditorEvent.DismissDialog)
        assertNull(viewModel.uiState.value.dialogState.activeDialog)
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
        val task = Task(id = 10L, title = "Existing", createdAt = 1000L)
        coEvery { getTaskByIdUseCase(10L) } returns task
        viewModel.loadTask(10L)

        viewModel.navigationEvent.test {
            viewModel.onEvent(TaskEditorEvent.Save)
            awaitItem()
        }

        coVerify(exactly = 1) { updateTaskUseCase(any()) }
    }

    @Test
    fun `Save existing task preserves original createdAt`() = runTest {
        val originalCreatedAt = 1_600_000_000_000L
        val task = Task(id = 10L, title = "Existing", createdAt = originalCreatedAt)
        coEvery { getTaskByIdUseCase(10L) } returns task
        viewModel.loadTask(10L)

        viewModel.navigationEvent.test {
            viewModel.onEvent(TaskEditorEvent.Save)
            awaitItem()
        }

        coVerify {
            updateTaskUseCase(match { it.createdAt == originalCreatedAt })
        }
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

    @Test
    fun `Increment and Decrement interval updates recurrenceRule`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)
        assertEquals(1, viewModel.uiState.value.recurrenceRule.interval)
        
        viewModel.onEvent(TaskEditorEvent.IncrementInterval)
        assertEquals(2, viewModel.uiState.value.recurrenceRule.interval)
        assertTrue(viewModel.uiState.value.dateSection.recurrenceUiModel.canDecrement)

        viewModel.onEvent(TaskEditorEvent.DecrementInterval)
        assertEquals(1, viewModel.uiState.value.recurrenceRule.interval)
        assertFalse(viewModel.uiState.value.dateSection.recurrenceUiModel.canDecrement)
    }

    @Test
    fun `RecurrenceUnitSelected updates unit`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)
        viewModel.onEvent(TaskEditorEvent.RecurrenceUnitSelected(RecurrenceUnit.WEEKS))
        assertEquals(RecurrenceUnit.WEEKS, viewModel.uiState.value.recurrenceRule.unit)
    }
}
