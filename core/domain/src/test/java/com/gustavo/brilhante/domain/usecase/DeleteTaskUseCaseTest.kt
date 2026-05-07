package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TaskRepository
import com.gustavo.brilhante.model.Task
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Test

class DeleteTaskUseCaseTest {

    private val repository: TaskRepository = mockk(relaxed = true)
    private val useCase = DeleteTaskUseCase(repository)

    @Test
    fun `given a task, when invoke called, then delegates to repository deleteTask`() = runTest {
        val task = Task(id = 3, title = "Task to delete")

        useCase(task)

        coVerify(exactly = 1) { repository.deleteTask(task) }
    }

    @Test
    fun `given a fully populated task, when invoke called, then repository receives the complete task`() = runTest {
        val task = Task(
            id = 7,
            title = "Complete project",
            notes = "Finish by EOD",
            isUrgent = true,
            isFlagged = true,
            isCompleted = true
        )

        useCase(task)

        coVerify(exactly = 1) { repository.deleteTask(task) }
    }

    @Test
    fun `given repository throws, when invoke called, then exception propagates`() = runTest {
        val task = Task(id = 3, title = "Task to delete")
        coEvery { repository.deleteTask(task) } throws RuntimeException("DB error")

        try {
            useCase(task)
            fail("Expected RuntimeException was not thrown")
        } catch (_: RuntimeException) { }
    }
}
