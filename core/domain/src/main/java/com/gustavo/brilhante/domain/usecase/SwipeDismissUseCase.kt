package com.gustavo.brilhante.domain.usecase

import kotlinx.coroutines.delay
import javax.inject.Inject

class SwipeDismissUseCase @Inject constructor() {
    suspend operator fun invoke(action: suspend () -> Unit) {
        delay(400)
        action()
    }
}
