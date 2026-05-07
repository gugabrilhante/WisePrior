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
    fun `invoke calls repository deleteTag with the given tag`() = runTest {
        val tag = Tag(id = 1, name = "Work", color = 0)

        useCase(tag)

        coVerify(exactly = 1) { repository.deleteTag(tag) }
    }
}
