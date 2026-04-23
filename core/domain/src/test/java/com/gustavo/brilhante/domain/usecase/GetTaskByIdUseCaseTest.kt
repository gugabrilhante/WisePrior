package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TaskRepository
import com.gustavo.brilhante.model.Task
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetTaskByIdUseCaseTest {

    private val repository: TaskRepository = mockk()
    private val useCase = GetTaskByIdUseCase(repository)

    @Test
    fun `invoke returns task when repository finds it`() = runTest {
        val task = Task(id = 7, title = "Found task")
        coEvery { repository.getTaskById(7L) } returns task

        val result = useCase(7L)

        assertEquals(task, result)
        coVerify(exactly = 1) { repository.getTaskById(7L) }
    }

    @Test
    fun `invoke returns null when task does not exist`() = runTest {
        coEvery { repository.getTaskById(99L) } returns null

        val result = useCase(99L)

        assertNull(result)
    }
}
