package com.gustavo.brilhante.tasklist.presentation

import app.cash.turbine.test
import com.gustavo.brilhante.common.DateFormatter
import com.gustavo.brilhante.domain.usecase.AddTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTagUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.tasklist.model.TaskCollection
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListCollectionFilterTest {

    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk(relaxed = true)
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val getTagsUseCase: GetTagsUseCase = mockk()
    private val addTagUseCase: AddTagUseCase = mockk(relaxed = true)
    private val updateTagUseCase: UpdateTagUseCase = mockk(relaxed = true)
    private val deleteTagUseCase: DeleteTagUseCase = mockk(relaxed = true)
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)
    private val dateFormatter: DateFormatter = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    // Fixed date for deterministic testing: April 30, 2026 at noon UTC (current date)
    private val fixedClock = Clock.fixed(Instant.parse("2026-04-30T12:00:00Z"), ZoneOffset.UTC)
    private val todayMillis: Long = fixedClock.millis()
    // pastMillis is Sep 9 2001 — never matches "today" in any timezone
    private val pastMillis  = 1_000_000_000_000L

    private val taskNoDate  = Task(id = 1, title = "No date")
    private val taskPast    = Task(id = 2, title = "Past",      dueDate = pastMillis)
    private val taskToday   = Task(id = 3, title = "Today",     dueDate = todayMillis)
    private val taskFlagged = Task(id = 4, title = "Flagged",   isFlagged = true)
    private val taskDone    = Task(id = 5, title = "Completed", isCompleted = true)
    private val taskTagged  = Task(id = 6, title = "Tagged",    tagIds = listOf(42L))

    private val allTasks = listOf(taskNoDate, taskPast, taskToday, taskFlagged, taskDone, taskTagged)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getTagsUseCase() } returns flowOf(emptyList())
        every { dateFormatter.formatShortDate(any()) } returns "Jan 1"
        every { dateFormatter.formatShortDateTime(any()) } returns "Jan 1 10:00"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(tasks: List<Task> = allTasks): TaskListViewModel {
        every { getTasksUseCase() } returns MutableStateFlow(tasks)
        return TaskListViewModel(
            getTasksUseCase, deleteTaskUseCase, updateTaskUseCase, getTagsUseCase,
            addTagUseCase, updateTagUseCase, deleteTagUseCase,
            notificationScheduler, dateFormatter
        )
    }

    // ── Collection.All ────────────────────────────────────────────────────────

    @Test
    fun `given all tasks, when All selected, then all tasks are shown`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(allTasks, awaitItem().tasks)
        }
    }

    // ── Collection.Today ──────────────────────────────────────────────────────

    @Test
    fun `given tasks with mixed due dates, when Today selected, then only tasks due today are shown`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.Today)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(listOf(taskToday), awaitItem().tasks)
        }
    }

    @Test
    fun `given no task due today, when Today selected, then shows empty list`() = runTest(testDispatcher) {
        val viewModel = buildViewModel(listOf(taskNoDate, taskPast))
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.Today)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(emptyList<Task>(), awaitItem().tasks)
        }
    }

    // ── Collection.Scheduled ─────────────────────────────────────────────────

    @Test
    fun `given tasks with and without due dates, when Scheduled selected, then only tasks with any due date are shown`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.Scheduled)
        advanceUntilIdle()

        viewModel.uiState.test {
            val tasks = awaitItem().tasks
            assertTrue(tasks.contains(taskPast))
            assertTrue(tasks.contains(taskToday))
            assertTrue(tasks.none { it.dueDate == null })
        }
    }

    // ── Collection.Flagged ────────────────────────────────────────────────────

    @Test
    fun `given mixed tasks, when Flagged selected, then only flagged tasks are shown`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.Flagged)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(listOf(taskFlagged), awaitItem().tasks)
        }
    }

    // ── Collection.Completed ─────────────────────────────────────────────────

    @Test
    fun `given mixed tasks, when Completed selected, then only completed tasks are shown`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.Completed)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(listOf(taskDone), awaitItem().tasks)
        }
    }

    // ── Collection.ByTag ─────────────────────────────────────────────────────

    @Test
    fun `given tasks with tags, when ByTag selected, then only tasks carrying that tag are shown`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.ByTag(42L))
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(listOf(taskTagged), awaitItem().tasks)
        }
    }

    @Test
    fun `given no tasks with tag, when ByTag selected, then shows empty list`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.ByTag(999L))
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(emptyList<Task>(), awaitItem().tasks)
        }
    }

    // ── selectedCollection reflected in uiState ───────────────────────────────

    @Test
    fun `given a collection, when onCollectionSelected called, then uiState selectedCollection updates`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.Flagged)
        advanceUntilIdle()

        assertEquals(TaskCollection.Flagged, viewModel.uiState.value.selectedCollection)
    }

    // ── CollectionCounts ─────────────────────────────────────────────────────

    @Test
    fun `given all tasks, when state emitted, then collectionCounts reflect unfiltered totals`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val counts = awaitItem().collectionCounts
            assertEquals(allTasks.size, counts.all)
            assertEquals(1, counts.today)      // taskToday only
            assertEquals(2, counts.scheduled)  // taskPast + taskToday
            assertEquals(1, counts.flagged)    // taskFlagged
            assertEquals(1, counts.completed)  // taskDone
        }
    }

    @Test
    fun `given filtered collection, when state emitted, then collectionCounts still reflect all tasks`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.Flagged)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(taskFlagged), state.tasks)
            assertEquals(allTasks.size, state.collectionCounts.all)
        }
    }

    // ── TagCounts ─────────────────────────────────────────────────────────────

    @Test
    fun `given tasks with tags, when state emitted, then tagCounts maps tagId to task count`() = runTest(testDispatcher) {
        val tasks = listOf(
            Task(id = 1, title = "A", tagIds = listOf(10L, 20L)),
            Task(id = 2, title = "B", tagIds = listOf(10L)),
            Task(id = 3, title = "C", tagIds = listOf(30L))
        )
        val viewModel = buildViewModel(tasks)
        advanceUntilIdle()

        viewModel.uiState.test {
            val counts = awaitItem().tagCounts
            assertEquals(2, counts[10L])
            assertEquals(1, counts[20L])
            assertEquals(1, counts[30L])
        }
    }

    // ── formattedDueDates ─────────────────────────────────────────────────────

    @Test
    fun `given task with dueDate and no time, when state emitted, then formattedDueDates uses formatShortDate`() = runTest(testDispatcher) {
        every { dateFormatter.formatShortDate(todayMillis) } returns "Today"
        val tasks = listOf(Task(id = 1, title = "Task", dueDate = todayMillis, hasTime = false))
        val viewModel = buildViewModel(tasks)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals("Today", awaitItem().formattedDueDates[1L])
        }
    }

    @Test
    fun `given task with dueDate and time, when state emitted, then formattedDueDates uses formatShortDateTime`() = runTest(testDispatcher) {
        every { dateFormatter.formatShortDateTime(todayMillis) } returns "Today 14:00"
        val tasks = listOf(Task(id = 2, title = "Task", dueDate = todayMillis, hasTime = true))
        val viewModel = buildViewModel(tasks)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals("Today 14:00", awaitItem().formattedDueDates[2L])
        }
    }
}
