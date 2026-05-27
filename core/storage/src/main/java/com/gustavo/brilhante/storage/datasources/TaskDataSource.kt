package com.gustavo.brilhante.storage.datasources

import androidx.room.withTransaction
import com.gustavo.brilhante.storage.dao.ChecklistItemDao
import com.gustavo.brilhante.storage.dao.TaskDao
import com.gustavo.brilhante.storage.database.AppDatabase
import com.gustavo.brilhante.storage.entity.ChecklistItemEntity
import com.gustavo.brilhante.storage.entity.TaskEntity
import com.gustavo.brilhante.storage.entity.TaskWithChecklistItems
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskDataSource @Inject constructor(
    private val database: AppDatabase,
    private val taskDao: TaskDao,
    private val checklistItemDao: ChecklistItemDao
) {
    val allTasks: Flow<List<TaskWithChecklistItems>> = taskDao.getAllTasksWithChecklist()

    suspend fun getTaskById(id: Long): TaskWithChecklistItems? = taskDao.getTaskWithChecklistById(id)

    suspend fun insertTask(task: TaskEntity, checklistItems: List<ChecklistItemEntity>) {
        database.withTransaction {
            val taskId = taskDao.insertTask(task)
            if (checklistItems.isNotEmpty()) {
                checklistItemDao.insertAll(checklistItems.map { it.copy(taskId = taskId) })
            }
        }
    }

    suspend fun updateTask(task: TaskEntity, checklistItems: List<ChecklistItemEntity>) {
        database.withTransaction {
            taskDao.updateTask(task)
            checklistItemDao.deleteAllForTask(task.id)
            if (checklistItems.isNotEmpty()) {
                checklistItemDao.insertAll(checklistItems.map { it.copy(taskId = task.id) })
            }
        }
    }

    suspend fun deleteTask(task: TaskEntity) {
        database.withTransaction {
            checklistItemDao.deleteAllForTask(task.id)
            taskDao.deleteTask(task)
        }
    }
}
