package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TagRepository
import com.gustavo.brilhante.model.Tag
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteTagUseCaseTest {

    private val repository: TagRepository = mockk(relaxed = true)
    private val useCase = DeleteTagUseCase(repository)

    @Test
    fun `given a tag, when invoke called, then delegates to repository deleteTag`() = runTest {
        val tag = Tag(id = 5L, name = "Outdated tag", color = 0xFFEAB308L)

        useCase(tag)

        coVerify(exactly = 1) { repository.deleteTag(tag) }
    }
}
