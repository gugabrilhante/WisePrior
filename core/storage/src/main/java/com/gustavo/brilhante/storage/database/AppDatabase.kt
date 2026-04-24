package com.gustavo.brilhante.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gustavo.brilhante.storage.converter.Converters
import com.gustavo.brilhante.storage.dao.TagDao
import com.gustavo.brilhante.storage.dao.TaskDao
import com.gustavo.brilhante.storage.entity.TagEntity
import com.gustavo.brilhante.storage.entity.TaskEntity

@Database(entities = [TaskEntity::class, TagEntity::class], version = 6, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun tagDao(): TagDao

    companion object {
        const val databaseName = "wiseprior_database"

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE tasks ADD COLUMN recurrenceType TEXT NOT NULL DEFAULT 'NONE'"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE tasks ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        // Replaces the free-form `tags TEXT` column with `tagIds TEXT` (comma-separated Long ids)
        // and creates the `tags` table for the new Tag domain model.
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tags` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `color` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE `tasks_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `url` TEXT NOT NULL,
                        `dueDate` INTEGER,
                        `hasTime` INTEGER NOT NULL,
                        `isUrgent` INTEGER NOT NULL,
                        `priority` TEXT NOT NULL,
                        `tagIds` TEXT NOT NULL,
                        `isFlagged` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `recurrenceType` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO `tasks_new`
                        (`id`,`title`,`notes`,`url`,`dueDate`,`hasTime`,`isUrgent`,`priority`,
                         `tagIds`,`isFlagged`,`isCompleted`,`recurrenceType`,`createdAt`)
                    SELECT `id`,`title`,`notes`,`url`,`dueDate`,`hasTime`,`isUrgent`,`priority`,
                           '',`isFlagged`,`isCompleted`,`recurrenceType`,`createdAt`
                    FROM `tasks`
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE `tasks`")
                database.execSQL("ALTER TABLE `tasks_new` RENAME TO `tasks`")
            }
        }
    }
}
