package com.gustavo.brilhante.data.repository

import com.gustavo.brilhante.data.models.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasksFlow(): Flow<List<Task>>
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
}