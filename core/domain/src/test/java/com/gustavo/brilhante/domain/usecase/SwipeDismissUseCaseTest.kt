package com.gustavo.brilhante.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SwipeDismissUseCaseTest {

    private val useCase = SwipeDismissUseCase()

    @Test
    fun `invoke should execute action`() = runTest {
        var executed = false
        useCase {
            executed = true
        }
        assertTrue(executed)
    }
}
