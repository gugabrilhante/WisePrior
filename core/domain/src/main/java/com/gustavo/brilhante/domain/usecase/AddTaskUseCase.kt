package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TaskRepository
import com.gustavo.brilhante.model.Task
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) = repository.addTask(task)
}
