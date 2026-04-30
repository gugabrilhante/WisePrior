package com.gustavo.brilhante.domain.usecase

import app.cash.turbine.test
import com.gustavo.brilhante.domain.repository.TagRepository
import com.gustavo.brilhante.model.Tag
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTagsUseCaseTest {

    private val repository: TagRepository = mockk()
    private val useCase = GetTagsUseCase(repository)

    @Test
    fun `given repository has tags, when invoke called, then emits full tag list`() = runTest {
        val tags = listOf(
            Tag(id = 1L, name = "Work", color = 0xFF3B82F6L),
            Tag(id = 2L, name = "Personal", color = 0xFF22C55EL)
        )
        every { repository.getTags() } returns flowOf(tags)

        useCase().test {
            assertEquals(tags, awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { repository.getTags() }
    }

    @Test
    fun `given repository has no tags, when invoke called, then emits empty list`() = runTest {
        every { repository.getTags() } returns flowOf(emptyList())

        useCase().test {
            assertEquals(emptyList<Tag>(), awaitItem())
            awaitComplete()
        }
    }
}
