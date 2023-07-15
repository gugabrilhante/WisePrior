package com.gustavo.brilhante.storage.datasources

import com.gustavo.brilhante.storage.dao.TaskDao
import com.gustavo.brilhante.storage.entity.TaskDB
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskDataSource @Inject constructor(private val taskDao: TaskDao) {
    val allTasks: Flow<List<TaskDB>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TaskDB) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskDB) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskDB) {
        taskDao.deleteTask(task)
    }
}