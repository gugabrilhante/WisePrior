package com.gustavo.brilhante.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gustavo.brilhante.storage.entity.TaskDB
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskDB)

    @Update
    suspend fun updateTask(task: TaskDB)

    @Delete
    suspend fun deleteTask(task: TaskDB)
}
