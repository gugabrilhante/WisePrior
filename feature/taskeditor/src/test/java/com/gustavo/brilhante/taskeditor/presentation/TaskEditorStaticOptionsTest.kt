package com.gustavo.brilhante.taskeditor.presentation

import com.gustavo.brilhante.domain.time.CalendarProvider
import com.gustavo.brilhante.domain.time.ClockProvider
import com.gustavo.brilhante.domain.usecase.AddTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTaskByIdUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.ui.DateFormatterImpl
import com.gustavo.brilhante.ui.UiText
import io.mockk.coEvery
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class TaskEditorStaticOptionsTest {

    private val addTaskUseCase: AddTaskUseCase = mockk(relaxed = true)
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val getTaskByIdUseCase: GetTaskByIdUseCase = mockk()
    private val getTagsUseCase: GetTagsUseCase = mockk()
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)
    private val clockProvider: ClockProvider = mockk()
    private val calendarProvider: CalendarProvider = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: TaskEditorViewModel

    @Before
    fun setup() {
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
        Dispatchers.resetMain()
    }

    // ── screenTitle ───────────────────────────────────────────────────────────

    @Test
    fun `given new task, when loadTask called with negative id, then screenTitle is StringResource`() {
        viewModel.loadTask(-1L)

        assertTrue(viewModel.uiState.value.screenTitle is UiText.StringResource)
    }

    @Test
    fun `given existing task, when loadTask called with valid id, then screenTitle is StringResource`() = runTest {
        coEvery { getTaskByIdUseCase(10L) } returns Task(id = 10L, title = "Existing", createdAt = 1000L)

        viewModel.loadTask(10L)

        assertTrue(viewModel.uiState.value.screenTitle is UiText.StringResource)
    }

    @Test
    fun `new and edit modes produce different screenTitle resId values`() = runTest {
        coEvery { getTaskByIdUseCase(10L) } returns Task(id = 10L, title = "Existing", createdAt = 1000L)

        viewModel.loadTask(-1L)
        val newTitle = viewModel.uiState.value.screenTitle as UiText.StringResource

        val realFormatter = DateFormatterImpl(calendarProvider)
        val editViewModel = TaskEditorViewModel(
            addTaskUseCase, updateTaskUseCase, getTaskByIdUseCase,
            getTagsUseCase, notificationScheduler, realFormatter, clockProvider
        )
        editViewModel.loadTask(10L)
        val editTitle = editViewModel.uiState.value.screenTitle as UiText.StringResource

        assertTrue(newTitle.resId != editTitle.resId)
    }

    // ── priorityOptions ───────────────────────────────────────────────────────

    @Test
    fun `given new task, when loadTask called, then priorityOptions has four entries`() {
        viewModel.loadTask(-1L)

        assertEquals(4, viewModel.uiState.value.priorityOptions.size)
    }

    @Test
    fun `given new task, when loadTask called, then priorityOptions covers all Priority values`() {
        viewModel.loadTask(-1L)

        val priorities = viewModel.uiState.value.priorityOptions.map { it.priority }.toSet()
        assertEquals(Priority.entries.toSet(), priorities)
    }

    @Test
    fun `given new task, when loadTask called, then all priority option labels are StringResource`() {
        viewModel.loadTask(-1L)

        val options = viewModel.uiState.value.priorityOptions
        assertTrue(options.all { it.label is UiText.StringResource })
    }

    @Test
    fun `given new task with default NONE priority, then NONE option is selected and others are not`() {
        viewModel.loadTask(-1L)

        val options = viewModel.uiState.value.priorityOptions
        assertTrue(options.first { it.priority == Priority.NONE }.isSelected)
        assertFalse(options.first { it.priority == Priority.LOW }.isSelected)
        assertFalse(options.first { it.priority == Priority.MEDIUM }.isSelected)
        assertFalse(options.first { it.priority == Priority.HIGH }.isSelected)
    }

    @Test
    fun `given PriorityChanged to HIGH, then HIGH option is selected and NONE is not`() {
        viewModel.loadTask(-1L)
        viewModel.onEvent(TaskEditorEvent.PriorityChanged(Priority.HIGH))

        val options = viewModel.uiState.value.priorityOptions
        assertTrue(options.first { it.priority == Priority.HIGH }.isSelected)
        assertFalse(options.first { it.priority == Priority.NONE }.isSelected)
        assertFalse(options.first { it.priority == Priority.LOW }.isSelected)
        assertFalse(options.first { it.priority == Priority.MEDIUM }.isSelected)
    }

    @Test
    fun `given PriorityChanged to MEDIUM, then exactly one option is selected`() {
        viewModel.loadTask(-1L)
        viewModel.onEvent(TaskEditorEvent.PriorityChanged(Priority.MEDIUM))

        val selectedCount = viewModel.uiState.value.priorityOptions.count { it.isSelected }
        assertEquals(1, selectedCount)
    }

    @Test
    fun `given existing task with HIGH priority, when loadTask called, then HIGH option is selected`() = runTest {
        coEvery { getTaskByIdUseCase(5L) } returns Task(id = 5L, title = "Task", priority = Priority.HIGH, createdAt = 1000L)

        viewModel.loadTask(5L)

        val options = viewModel.uiState.value.priorityOptions
        assertTrue(options.first { it.priority == Priority.HIGH }.isSelected)
        assertFalse(options.first { it.priority == Priority.NONE }.isSelected)
    }

    // ── recurrenceUnitOptions ─────────────────────────────────────────────────

    @Test
    fun `given new task, when loadTask called, then recurrenceUnitOptions has four entries`() {
        viewModel.loadTask(-1L)

        assertEquals(4, viewModel.uiState.value.recurrenceUnitOptions.size)
    }

    @Test
    fun `given new task, when loadTask called, then NONE unit is excluded from recurrenceUnitOptions`() {
        viewModel.loadTask(-1L)

        val units = viewModel.uiState.value.recurrenceUnitOptions.map { it.unit }
        assertFalse(units.contains(RecurrenceUnit.NONE))
    }

    @Test
    fun `given new task, when loadTask called, then recurrenceUnitOptions contains HOURS DAYS WEEKS MONTHS`() {
        viewModel.loadTask(-1L)

        val units = viewModel.uiState.value.recurrenceUnitOptions.map { it.unit }.toSet()
        assertEquals(
            setOf(RecurrenceUnit.HOURS, RecurrenceUnit.DAYS, RecurrenceUnit.WEEKS, RecurrenceUnit.MONTHS),
            units
        )
    }

    @Test
    fun `given new task, when loadTask called, then all recurrenceUnit labels are StringResource`() {
        viewModel.loadTask(-1L)

        val options = viewModel.uiState.value.recurrenceUnitOptions
        assertTrue(options.all { it.label is UiText.StringResource })
    }

    @Test
    fun `given new task, each recurrenceUnit option uses a distinct string resource`() {
        viewModel.loadTask(-1L)

        val resIds = viewModel.uiState.value.recurrenceUnitOptions
            .map { (it.label as UiText.StringResource).resId }
        assertEquals(resIds.size, resIds.toSet().size)
    }
}
