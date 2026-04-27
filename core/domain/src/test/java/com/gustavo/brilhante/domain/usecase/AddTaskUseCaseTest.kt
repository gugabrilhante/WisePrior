package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TaskRepository
import com.gustavo.brilhante.model.Task
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddTaskUseCaseTest {

    private val repository: TaskRepository = mockk(relaxed = true)
    private val useCase = AddTaskUseCase(repository)

    @Test
    fun `invoke calls repository addTask with the given task`() = runTest {
        val task = Task(title = "Buy groceries")

        useCase(task)

        coVerify(exactly = 1) { repository.addTask(task) }
    }
}
