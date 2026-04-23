package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TaskRepository
import com.gustavo.brilhante.model.Task
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteTaskUseCaseTest {

    private val repository: TaskRepository = mockk(relaxed = true)
    private val useCase = DeleteTaskUseCase(repository)

    @Test
    fun `invoke calls repository deleteTask with the given task`() = runTest {
        val task = Task(id = 3, title = "Task to delete")

        useCase(task)

        coVerify(exactly = 1) { repository.deleteTask(task) }
    }
}
