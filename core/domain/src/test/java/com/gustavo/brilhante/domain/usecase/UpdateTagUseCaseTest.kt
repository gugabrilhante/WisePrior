package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TagRepository
import com.gustavo.brilhante.model.Tag
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Test

class UpdateTagUseCaseTest {

    private val repository: TagRepository = mockk(relaxed = true)
    private val useCase = UpdateTagUseCase(repository)

    @Test
    fun `given a tag, when invoke called, then delegates to repository updateTag`() = runTest {
        val tag = Tag(id = 2, name = "Personal", color = 0xFF00FF)

        useCase(tag)

        coVerify(exactly = 1) { repository.updateTag(tag) }
    }

    @Test
    fun `given invoke called multiple times, when each call has a different tag, then repository receives each tag`() = runTest {
        val first = Tag(id = 1, name = "Work", color = 0xFF3B82F6L)
        val second = Tag(id = 2, name = "Personal", color = 0xFF22C55EL)

        useCase(first)
        useCase(second)

        coVerify(exactly = 1) { repository.updateTag(first) }
        coVerify(exactly = 1) { repository.updateTag(second) }
    }

    @Test
    fun `given repository throws, when invoke called, then exception propagates`() = runTest {
        val tag = Tag(id = 2, name = "Personal", color = 0xFF00FF)
        coEvery { repository.updateTag(tag) } throws RuntimeException("DB error")

        try {
            useCase(tag)
            fail("Expected RuntimeException was not thrown")
        } catch (_: RuntimeException) { }
    }
}
