package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TaskRepository
import com.gustavo.brilhante.model.Task
import javax.inject.Inject

class GetTaskByIdUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(id: Long): Task? = repository.getTaskById(id)
}
