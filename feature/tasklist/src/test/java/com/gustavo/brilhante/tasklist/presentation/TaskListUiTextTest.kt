package com.gustavo.brilhante.tasklist.presentation

import app.cash.turbine.test
import com.gustavo.brilhante.ui.DateFormatter
import com.gustavo.brilhante.domain.usecase.AddTagUseCase
import com.gustavo.brilhante.domain.time.ClockProvider
import com.gustavo.brilhante.domain.usecase.CalculateTaskPriorityUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTagUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.model.TaskSortOption
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.tasklist.data.SortPreferencesDataStore
import com.gustavo.brilhante.tasklist.model.TaskCollection
import com.gustavo.brilhante.ui.UiText
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListUiTextTest {

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
    private val clockProvider: ClockProvider = mockk()
    private val calculateTaskPriority = CalculateTaskPriorityUseCase(clockProvider)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { clockProvider.currentTimeMillis() } returns 1000L
        every { getTasksUseCase() } returns flowOf(emptyList())
        every { sortPreferences.sortOption } returns flowOf(TaskSortOption.SMART_PRIORITY)
        coEvery { sortPreferences.setSortOption(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(tags: List<Tag> = emptyList()): TaskListViewModel {
        every { getTagsUseCase() } returns MutableStateFlow(tags)
        return TaskListViewModel(
            getTasksUseCase, deleteTaskUseCase, updateTaskUseCase, getTagsUseCase,
            addTagUseCase, updateTagUseCase, deleteTagUseCase,
            notificationScheduler, dateFormatter, sortPreferences, calculateTaskPriority
        )
    }

    // ── screenTitle per collection ────────────────────────────────────────────

    @Test
    fun `given All collection, when state emitted, then screenTitle is StringResource`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem().screenTitle is UiText.StringResource)
        }
    }

    @Test
    fun `given Today collection, when state emitted, then screenTitle is StringResource`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.Today)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem().screenTitle is UiText.StringResource)
        }
    }

    @Test
    fun `given Scheduled collection, when state emitted, then screenTitle is StringResource`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.Scheduled)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem().screenTitle is UiText.StringResource)
        }
    }

    @Test
    fun `given Flagged collection, when state emitted, then screenTitle is StringResource`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.Flagged)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem().screenTitle is UiText.StringResource)
        }
    }

    @Test
    fun `given Completed collection, when state emitted, then screenTitle is StringResource`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.Completed)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem().screenTitle is UiText.StringResource)
        }
    }

    @Test
    fun `given ByTag collection with matching tag, when state emitted, then screenTitle is DynamicString with tag name`() = runTest(testDispatcher) {
        val tag = Tag(id = 7L, name = "Work", color = 0L)
        val viewModel = buildViewModel(tags = listOf(tag))
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.ByTag(7L))
        advanceUntilIdle()

        viewModel.uiState.test {
            val title = awaitItem().screenTitle
            assertTrue(title is UiText.DynamicString)
            assertEquals("Work", (title as UiText.DynamicString).value)
        }
    }

    @Test
    fun `given ByTag collection with unknown tag id, when state emitted, then screenTitle falls back to StringResource`() = runTest(testDispatcher) {
        val viewModel = buildViewModel(tags = emptyList())
        advanceUntilIdle()
        viewModel.onCollectionSelected(TaskCollection.ByTag(999L))
        advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem().screenTitle is UiText.StringResource)
        }
    }

    @Test
    fun `collection changes produce different screenTitle values`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val titleAll = viewModel.uiState.value.screenTitle
        viewModel.onCollectionSelected(TaskCollection.Today)
        advanceUntilIdle()
        val titleToday = viewModel.uiState.value.screenTitle

        assertTrue(titleAll is UiText.StringResource)
        assertTrue(titleToday is UiText.StringResource)
        assertTrue((titleAll as UiText.StringResource).resId != (titleToday as UiText.StringResource).resId)
    }

    // ── addTagLabel & canAddTag ───────────────────────────────────────────────

    @Test
    fun `given fewer than max tags, when state emitted, then canAddTag is true and addTagLabel is StringResource`() = runTest(testDispatcher) {
        val viewModel = buildViewModel(tags = listOf(Tag(id = 1L, name = "Work", color = 0L)))
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.canAddTag)
            assertTrue(state.addTagLabel is UiText.StringResource)
        }
    }

    @Test
    fun `given no tags, when state emitted, then canAddTag is true`() = runTest(testDispatcher) {
        val viewModel = buildViewModel(tags = emptyList())
        advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem().canAddTag)
        }
    }

    @Test
    fun `given max tags reached, when state emitted, then canAddTag is false and addTagLabel is PluralResource`() = runTest(testDispatcher) {
        val maxTags = (1L..5L).map { Tag(id = it, name = "Tag $it", color = 0L) }
        val viewModel = buildViewModel(tags = maxTags)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.canAddTag)
            assertTrue(state.addTagLabel is UiText.PluralResource)
        }
    }

    @Test
    fun `given max tags reached, addTagLabel PluralResource count is 5`() = runTest(testDispatcher) {
        val maxTags = (1L..5L).map { Tag(id = it, name = "Tag $it", color = 0L) }
        val viewModel = buildViewModel(tags = maxTags)
        advanceUntilIdle()

        viewModel.uiState.test {
            val label = awaitItem().addTagLabel as UiText.PluralResource
            assertEquals(5, label.count)
        }
    }

    // ── sortOptions ───────────────────────────────────────────────────────────

    @Test
    fun `given default sort, when state emitted, then sortOptions has three entries`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(3, awaitItem().sortOptions.size)
        }
    }

    @Test
    fun `given default SMART_PRIORITY sort, when state emitted, then SMART_PRIORITY option is selected`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val options = awaitItem().sortOptions
            val selected = options.filter { it.isSelected }
            assertEquals(1, selected.size)
            assertEquals(TaskSortOption.SMART_PRIORITY, selected.first().option)
        }
    }

    @Test
    fun `given default sort, sortOptions covers all three TaskSortOption values`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val optionValues = awaitItem().sortOptions.map { it.option }.toSet()
            assertEquals(
                setOf(
                    TaskSortOption.CREATED_DESC,
                    TaskSortOption.CREATED_ASC,
                    TaskSortOption.SMART_PRIORITY
                ),
                optionValues
            )
        }
    }

    @Test
    fun `given default sort, all sortOption labels are StringResource`() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val options = awaitItem().sortOptions
            assertTrue(options.all { it.label is UiText.StringResource })
        }
    }

    // ── showEmptyState ────────────────────────────────────────────────────────

    @Test
    fun `given no tasks, when state emitted, then showEmptyState is true`() = runTest(testDispatcher) {
        every { getTasksUseCase() } returns flowOf(emptyList())
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem().showEmptyState)
        }
    }

    @Test
    fun `given tasks present, when state emitted, then showEmptyState is false`() = runTest(testDispatcher) {
        every { getTasksUseCase() } returns flowOf(listOf(Task(id = 1L, title = "Task", createdAt = 1000L)))
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            assertFalse(awaitItem().showEmptyState)
        }
    }
}
