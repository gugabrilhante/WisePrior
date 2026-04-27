package com.gustavo.brilhante.domain.usecase

import app.cash.turbine.test
import com.gustavo.brilhante.domain.repository.TaskRepository
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Task
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTasksUseCaseTest {

    private val repository: TaskRepository = mockk()
    private val useCase = GetTasksUseCase(repository)

    @Test
    fun `invoke delegates to repository and returns tasks`() = runTest {
        val tasks = listOf(
            Task(id = 1, title = "Task A", priority = Priority.HIGH),
            Task(id = 2, title = "Task B")
        )
        every { repository.getTasks() } returns flowOf(tasks)

        useCase().test {
            assertEquals(tasks, awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { repository.getTasks() }
    }

    @Test
    fun `invoke returns empty list when repository has no tasks`() = runTest {
        every { repository.getTasks() } returns flowOf(emptyList())

        useCase().test {
            assertEquals(emptyList<Task>(), awaitItem())
            awaitComplete()
        }
    }
}
