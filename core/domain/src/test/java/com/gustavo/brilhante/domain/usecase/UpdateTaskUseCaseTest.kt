package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TaskRepository
import com.gustavo.brilhante.model.Task
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateTaskUseCaseTest {

    private val repository: TaskRepository = mockk(relaxed = true)
    private val useCase = UpdateTaskUseCase(repository)

    @Test
    fun `invoke calls repository updateTask with the given task`() = runTest {
        val task = Task(id = 5, title = "Updated task")

        useCase(task)

        coVerify(exactly = 1) { repository.updateTask(task) }
    }
}
