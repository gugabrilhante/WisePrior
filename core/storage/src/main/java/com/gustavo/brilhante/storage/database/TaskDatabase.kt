package com.gustavo.brilhante.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gustavo.brilhante.storage.dao.TaskDao
import com.gustavo.brilhante.storage.entity.TaskEntity

@Database(entities = [TaskEntity::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
