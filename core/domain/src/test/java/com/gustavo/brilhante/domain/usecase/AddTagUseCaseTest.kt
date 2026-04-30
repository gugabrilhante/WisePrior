package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TagRepository
import com.gustavo.brilhante.model.Tag
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddTagUseCaseTest {

    private val repository: TagRepository = mockk()
    private val useCase = AddTagUseCase(repository)

    @Test
    fun `given a tag, when invoke called, then delegates to repository and returns generated id`() = runTest {
        val tag = Tag(name = "Personal", color = 0xFF22C55EL)
        coEvery { repository.addTag(tag) } returns 42L

        val result = useCase(tag)

        assertEquals(42L, result)
        coVerify(exactly = 1) { repository.addTag(tag) }
    }

    @Test
    fun `given a tag, when invoke called, then propagates the id returned by repository`() = runTest {
        val tag = Tag(name = "Work", color = 0xFF3B82F6L)
        coEvery { repository.addTag(tag) } returns 1L

        assertEquals(1L, useCase(tag))
    }
}
