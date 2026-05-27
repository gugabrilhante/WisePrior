package com.gustavo.brilhante.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gustavo.brilhante.storage.entity.ChecklistItemEntity

@Dao
interface ChecklistItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ChecklistItemEntity>)

    @Query("DELETE FROM checklist_items WHERE taskId = :taskId")
    suspend fun deleteAllForTask(taskId: Long)

    @Query("SELECT * FROM checklist_items WHERE taskId = :taskId ORDER BY id ASC")
    suspend fun getItemsForTask(taskId: Long): List<ChecklistItemEntity>
}
