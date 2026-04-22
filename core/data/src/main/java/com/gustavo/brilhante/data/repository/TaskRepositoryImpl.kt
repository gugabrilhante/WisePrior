package com.gustavo.brilhante.data.repository

import com.gustavo.brilhante.common.IoDispatcher
import com.gustavo.brilhante.data.mapper.toEntity
import com.gustavo.brilhante.data.mapper.toModel
import com.gustavo.brilhante.domain.repository.TaskRepository
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.storage.datasources.TaskDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDataSource: TaskDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TaskRepository {

    override fun getTasks(): Flow<List<Task>> =
        taskDataSource.allTasks
            .map { entities -> entities.map { it.toModel() } }
            .flowOn(ioDispatcher)

    override suspend fun getTaskById(id: Long): Task? = withContext(ioDispatcher) {
        taskDataSource.getTaskById(id)?.toModel()
    }

    override suspend fun addTask(task: Task) = withContext(ioDispatcher) {
        taskDataSource.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) = withContext(ioDispatcher) {
        taskDataSource.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) = withContext(ioDispatcher) {
        taskDataSource.deleteTask(task.toEntity())
    }
}
