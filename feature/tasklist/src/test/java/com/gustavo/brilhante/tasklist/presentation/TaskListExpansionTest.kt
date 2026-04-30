package com.gustavo.brilhante.tasklist.presentation

import com.gustavo.brilhante.common.DateFormatter
import com.gustavo.brilhante.domain.usecase.AddTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTagUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.notifications.NotificationScheduler
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for card-expansion state managed by [TaskListViewModel].
 *
 * Expansion is stored in [TaskListUiState.expandedTaskIds] so it survives config
 * changes and is not reset when items are recycled in a LazyColumn.
 */
class TaskListExpansionTest {

    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk(relaxed = true)
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val getTagsUseCase: GetTagsUseCase = mockk()
    private val addTagUseCase: AddTagUseCase = mockk(relaxed = true)
    private val updateTagUseCase: UpdateTagUseCase = mockk(relaxed = true)
    private val deleteTagUseCase: DeleteTagUseCase = mockk(relaxed = true)
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)
    private val dateFormatter: DateFormatter = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getTasksUseCase() } returns flowOf(emptyList())
        every { getTagsUseCase() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = TaskListViewModel(
        getTasksUseCase, deleteTaskUseCase, updateTaskUseCase, getTagsUseCase,
        addTagUseCase, updateTagUseCase, deleteTagUseCase,
        notificationScheduler, dateFormatter
    )

    @Test
    fun `initially no tasks are expanded`() {
        val viewModel = buildViewModel()

        assertTrue(viewModel.uiState.value.expandedTaskIds.isEmpty())
    }

    @Test
    fun `toggleExpanded on a collapsed task marks it as expanded`() {
        val viewModel = buildViewModel()

        viewModel.toggleExpanded(taskId = 1L)

        assertTrue(1L in viewModel.uiState.value.expandedTaskIds)
    }

    @Test
    fun `toggleExpanded on an already-expanded task collapses it`() {
        val viewModel = buildViewModel()
        viewModel.toggleExpanded(taskId = 1L)

        viewModel.toggleExpanded(taskId = 1L)

        assertFalse(1L in viewModel.uiState.value.expandedTaskIds)
    }

    @Test
    fun `toggling one task does not affect other tasks`() {
        val viewModel = buildViewModel()
        viewModel.toggleExpanded(taskId = 1L)
        viewModel.toggleExpanded(taskId = 2L)

        // Collapse task 1 only
        viewModel.toggleExpanded(taskId = 1L)

        val expanded = viewModel.uiState.value.expandedTaskIds
        assertFalse(1L in expanded)
        assertTrue(2L in expanded)
    }

    @Test
    fun `multiple tasks can be expanded independently`() {
        val viewModel = buildViewModel()

        viewModel.toggleExpanded(taskId = 10L)
        viewModel.toggleExpanded(taskId = 20L)
        viewModel.toggleExpanded(taskId = 30L)

        val expanded = viewModel.uiState.value.expandedTaskIds
        assertEquals(setOf(10L, 20L, 30L), expanded)
    }

    @Test
    fun `default expansion state for any task id is collapsed`() {
        val viewModel = buildViewModel()

        assertFalse(99L in viewModel.uiState.value.expandedTaskIds)
    }
}
