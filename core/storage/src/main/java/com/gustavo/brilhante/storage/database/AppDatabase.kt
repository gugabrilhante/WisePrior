package com.gustavo.brilhante.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gustavo.brilhante.storage.converter.Converters
import com.gustavo.brilhante.storage.dao.TaskDao
import com.gustavo.brilhante.storage.entity.TaskEntity

@Database(entities = [TaskEntity::class], version = 3, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        const val databaseName = "wiseprior_database"
    }
}
