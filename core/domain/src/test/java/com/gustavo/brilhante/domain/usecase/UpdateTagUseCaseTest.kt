package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TagRepository
import com.gustavo.brilhante.model.Tag
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateTagUseCaseTest {

    private val repository: TagRepository = mockk(relaxed = true)
    private val useCase = UpdateTagUseCase(repository)

    @Test
    fun `given a tag, when invoke called, then delegates to repository updateTag`() = runTest {
        val tag = Tag(id = 3L, name = "Updated tag", color = 0xFFEF4444L)

        useCase(tag)

        coVerify(exactly = 1) { repository.updateTag(tag) }
    }
}
