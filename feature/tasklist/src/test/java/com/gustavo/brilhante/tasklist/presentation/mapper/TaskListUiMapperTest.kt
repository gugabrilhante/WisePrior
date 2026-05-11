package com.gustavo.brilhante.tasklist.presentation.mapper

import com.gustavo.brilhante.domain.usecase.CalculateTaskPriorityUseCase
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.model.TaskSortOption
import com.gustavo.brilhante.tasklist.model.TaskCollection
import com.gustavo.brilhante.tasklist.presentation.TaskListUiState
import com.gustavo.brilhante.ui.DateFormatter
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class TaskListUiMapperTest {

    private val dateFormatter = mockk<DateFormatter>()
    private val calculateTaskPriority = mockk<CalculateTaskPriorityUseCase>()
    private val sortOptionUiMapper = SortOptionUiMapper()
    private lateinit var mapper: TaskListUiMapper

    @Before
    fun setup() {
        mapper = TaskListUiMapper(dateFormatter, calculateTaskPriority, sortOptionUiMapper)
    }

    @Test
    fun `map should correctly filter and sort tasks`() {
        val task1 = Task(id = 1, title = "Task 1", createdAt = 1000L)
        val task2 = Task(id = 2, title = "Task 2", createdAt = 2000L)
        val tasks = listOf(task1, task2)
        val collection = TaskCollection.All
        val sortOption = TaskSortOption.CREATED_DESC
        
        val uiState = mapper.map(tasks, collection, emptyList(), sortOption, TaskListUiState())

        assertEquals(2, uiState.tasks.size)
        assertEquals(2L, uiState.tasks[0].id)
        assertEquals(1L, uiState.tasks[1].id)
    }

    @Test
    fun `map should format due dates correctly`() {
        val dueDate = 10000L
        val task = Task(id = 1, title = "Task 1", dueDate = dueDate, hasTime = true, createdAt = 1000L)
        val tasks = listOf(task)
        
        every { dateFormatter.formatShortDateTime(dueDate) } returns "Formatted Date"
        
        val uiState = mapper.map(tasks, TaskCollection.All, emptyList(), TaskSortOption.CREATED_DESC, TaskListUiState())

        assertEquals("Formatted Date", uiState.formattedDueDates[1L])
    }
}
