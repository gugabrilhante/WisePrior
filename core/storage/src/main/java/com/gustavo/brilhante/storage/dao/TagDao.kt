package com.gustavo.brilhante.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gustavo.brilhante.storage.entity.TagEntity
import com.gustavo.brilhante.storage.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    abstract fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTag(tag: TagEntity): Long

    @Update
    abstract suspend fun updateTag(tag: TagEntity)

    @Delete
    abstract suspend fun deleteTagInternal(tag: TagEntity)

    @Query("SELECT * FROM tasks")
    abstract suspend fun getAllTasksInternal(): List<TaskEntity>

    @Update
    abstract suspend fun updateTasksInternal(tasks: List<TaskEntity>)

    @Transaction
    open suspend fun deleteTagAndRemoveFromTasks(tag: TagEntity) {
        val tasks = getAllTasksInternal()
        val updatedTasks = tasks.mapNotNull { task ->
            if (task.tagIds.contains(tag.id)) {
                task.copy(tagIds = task.tagIds.filter { it != tag.id })
            } else null
        }
        if (updatedTasks.isNotEmpty()) {
            updateTasksInternal(updatedTasks)
        }
        deleteTagInternal(tag)
    }
}
