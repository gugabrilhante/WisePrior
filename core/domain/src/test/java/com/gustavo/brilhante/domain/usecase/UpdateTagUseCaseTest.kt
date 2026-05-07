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
    fun `invoke calls repository updateTag with the given tag`() = runTest {
        val tag = Tag(id = 2, name = "Personal", color = 0xFF00FF)

        useCase(tag)

        coVerify(exactly = 1) { repository.updateTag(tag) }
    }
}
