package com.gustavo.brilhante.data.repository

import com.gustavo.brilhante.common.IoDispatcher
import com.gustavo.brilhante.data.models.Task
import com.gustavo.brilhante.storage.datasources.TaskDataSource
import com.gustavo.brilhante.storage.entity.TaskEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDataSource: TaskDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TaskRepository {
    override fun getAllTasksFlow(): Flow<List<Task>> = taskDataSource.allTasks.map {
        it.map { task -> task.toTask() }
    }.flowOn(ioDispatcher)

    override suspend fun insertTask(task: Task) {
        taskDataSource.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        taskDataSource.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        taskDataSource.deleteTask(task.toEntity())
    }

    // TODO CREATE MAPPER AND INJECT
    fun Task.toEntity() = TaskEntity(
        title = this.title,
        description = this.description,
        dueDate = this.dueDate,
        reminderEnabled = this.reminderEnabled
    )

    fun TaskEntity.toTask() = Task(
        title = this.title,
        description = this.description,
        dueDate = this.dueDate,
        reminderEnabled = this.reminderEnabled
    )
}