package com.gustavo.brilhante.taskeditor.presentation

import com.gustavo.brilhante.domain.time.CalendarProvider
import com.gustavo.brilhante.domain.time.ClockProvider
import com.gustavo.brilhante.domain.usecase.AddTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTaskByIdUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.ui.DateFormatterImpl
import io.mockk.coEvery
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class TaskEditorRecurrenceTest {

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
        viewModel.loadTask(-1L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── ToggleRecurrence ──────────────────────────────────────────────────────

    @Test
    fun `given no recurrence, ToggleRecurrence enables it with DAYS and interval 1`() {
        assertFalse(viewModel.uiState.value.recurrenceRule.isRecurring)

        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)

        val rule = viewModel.uiState.value.recurrenceRule
        assertTrue(rule.isRecurring)
        assertEquals(RecurrenceUnit.DAYS, rule.unit)
        assertEquals(1, rule.interval)
    }

    @Test
    fun `given active recurrence, ToggleRecurrence disables it`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)
        assertTrue(viewModel.uiState.value.recurrenceRule.isRecurring)

        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)

        assertFalse(viewModel.uiState.value.recurrenceRule.isRecurring)
        assertEquals(RecurrenceRule.NONE, viewModel.uiState.value.recurrenceRule)
    }

    // ── IncrementInterval ─────────────────────────────────────────────────────

    @Test
    fun `IncrementInterval increases interval by 1`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence) // sets interval=1
        assertEquals(1, viewModel.uiState.value.recurrenceRule.interval)

        viewModel.onEvent(TaskEditorEvent.IncrementInterval)

        assertEquals(2, viewModel.uiState.value.recurrenceRule.interval)
    }

    @Test
    fun `IncrementInterval can be called multiple times`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)
        repeat(4) { viewModel.onEvent(TaskEditorEvent.IncrementInterval) }

        assertEquals(5, viewModel.uiState.value.recurrenceRule.interval)
    }

    @Test
    fun `IncrementInterval updates canDecrement to true`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence) // interval=1, canDecrement=false
        assertFalse(viewModel.uiState.value.dateSection.recurrenceUiModel.canDecrement)

        viewModel.onEvent(TaskEditorEvent.IncrementInterval)

        assertTrue(viewModel.uiState.value.dateSection.recurrenceUiModel.canDecrement)
    }

    // ── DecrementInterval ─────────────────────────────────────────────────────

    @Test
    fun `DecrementInterval decreases interval by 1 when interval is greater than 1`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)
        viewModel.onEvent(TaskEditorEvent.IncrementInterval) // interval=2

        viewModel.onEvent(TaskEditorEvent.DecrementInterval)

        assertEquals(1, viewModel.uiState.value.recurrenceRule.interval)
    }

    @Test
    fun `DecrementInterval does nothing when interval is 1`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence) // interval=1
        assertEquals(1, viewModel.uiState.value.recurrenceRule.interval)

        viewModel.onEvent(TaskEditorEvent.DecrementInterval)

        assertEquals(1, viewModel.uiState.value.recurrenceRule.interval)
    }

    @Test
    fun `DecrementInterval never goes below 1`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence) // interval=1
        repeat(5) { viewModel.onEvent(TaskEditorEvent.DecrementInterval) }

        assertTrue(viewModel.uiState.value.recurrenceRule.interval >= 1)
    }

    // ── canDecrement derived flag ─────────────────────────────────────

    @Test
    fun `canDecrement is false when interval equals 1`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence) // interval=1

        assertFalse(viewModel.uiState.value.dateSection.recurrenceUiModel.canDecrement)
    }

    @Test
    fun `canDecrement is true when interval is greater than 1`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)
        viewModel.onEvent(TaskEditorEvent.IncrementInterval) // interval=2

        assertTrue(viewModel.uiState.value.dateSection.recurrenceUiModel.canDecrement)
    }

    @Test
    fun `canDecrement becomes false after decrementing back to 1`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)
        viewModel.onEvent(TaskEditorEvent.IncrementInterval) // interval=2
        assertTrue(viewModel.uiState.value.dateSection.recurrenceUiModel.canDecrement)

        viewModel.onEvent(TaskEditorEvent.DecrementInterval) // interval=1

        assertFalse(viewModel.uiState.value.dateSection.recurrenceUiModel.canDecrement)
    }

    // ── RecurrenceUnitSelected ────────────────────────────────────────────────

    @Test
    fun `RecurrenceUnitSelected changes the recurrence unit`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence) // unit=DAYS

        viewModel.onEvent(TaskEditorEvent.RecurrenceUnitSelected(RecurrenceUnit.WEEKS))

        assertEquals(RecurrenceUnit.WEEKS, viewModel.uiState.value.recurrenceRule.unit)
    }

    @Test
    fun `RecurrenceUnitSelected preserves the current interval`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence) // interval=1
        viewModel.onEvent(TaskEditorEvent.IncrementInterval) // interval=2

        viewModel.onEvent(TaskEditorEvent.RecurrenceUnitSelected(RecurrenceUnit.MONTHS))

        val rule = viewModel.uiState.value.recurrenceRule
        assertEquals(RecurrenceUnit.MONTHS, rule.unit)
        assertEquals(2, rule.interval)
    }

    @Test
    fun `RecurrenceUnitSelected to all units works`() {
        viewModel.onEvent(TaskEditorEvent.ToggleRecurrence)

        listOf(RecurrenceUnit.HOURS, RecurrenceUnit.DAYS, RecurrenceUnit.WEEKS, RecurrenceUnit.MONTHS)
            .forEach { unit ->
                viewModel.onEvent(TaskEditorEvent.RecurrenceUnitSelected(unit))
                assertEquals(unit, viewModel.uiState.value.recurrenceRule.unit)
            }
    }

    // ── loadTask when task not found ──────────────────────────────────────────

    @Test
    fun `loadTask with valid id when task not found keeps isLoading false`() = runTest {
        coEvery { getTaskByIdUseCase(99L) } returns null

        viewModel.loadTask(99L)

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadTask with valid id when task not found does not change title`() = runTest {
        val realFormatter = DateFormatterImpl(calendarProvider)
        val freshViewModel = TaskEditorViewModel(
            addTaskUseCase, updateTaskUseCase, getTaskByIdUseCase,
            getTagsUseCase, notificationScheduler, realFormatter, clockProvider
        )
        coEvery { getTaskByIdUseCase(99L) } returns null

        freshViewModel.loadTask(99L)

        assertEquals("", freshViewModel.uiState.value.title)
        assertFalse(freshViewModel.uiState.value.isLoading)
    }

    // ── Task loaded with existing recurrence ──────────────────────────────────

    @Test
    fun `loadTask with recurring rule shows canDecrement correctly`() = runTest {
        val rule = RecurrenceRule(RecurrenceUnit.DAYS, 1)
        coEvery { getTaskByIdUseCase(10L) } returns Task(id = 10L, title = "Task", recurrenceRule = rule, createdAt = 1000L)

        val realFormatter = DateFormatterImpl(calendarProvider)
        val freshViewModel = TaskEditorViewModel(
            addTaskUseCase, updateTaskUseCase, getTaskByIdUseCase,
            getTagsUseCase, notificationScheduler, realFormatter, clockProvider
        )
        freshViewModel.loadTask(10L)

        // interval=1, so canDecrement=false
        assertFalse(freshViewModel.uiState.value.dateSection.recurrenceUiModel.canDecrement)
    }
}
