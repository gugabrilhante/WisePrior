package com.gustavo.brilhante.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gustavo.brilhante.model.DefaultTagColorPalette
import com.gustavo.brilhante.storage.converter.Converters
import com.gustavo.brilhante.storage.dao.TagDao
import com.gustavo.brilhante.storage.dao.TaskDao
import com.gustavo.brilhante.storage.database.migration.RecurrenceMigrationMapper
import com.gustavo.brilhante.storage.database.migration.TagColorGenerator
import com.gustavo.brilhante.storage.database.migration.TagMigrationMapper
import com.gustavo.brilhante.storage.database.migration.TagNameParser
import com.gustavo.brilhante.storage.entity.TagEntity
import com.gustavo.brilhante.storage.entity.TaskEntity

@Database(entities = [TaskEntity::class, TagEntity::class], version = 7, exportSchema = true)
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
                // Create the new tags table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tags` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `color` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // Create the new tasks table schema
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

                // Read all unique tag names from old tasks and create Tag entities
                val nameToId = mutableMapOf<String, Long>()
                val cursor = database.query("SELECT DISTINCT tags FROM tasks WHERE tags != ''")
                val uniqueNames = mutableSetOf<String>()

                while (cursor.moveToNext()) {
                    val tagsString = cursor.getString(0)
                    val parsedTags = TagNameParser.parse(tagsString)
                    uniqueNames.addAll(parsedTags)
                }
                cursor.close()

                // Insert unique tags into the tags table and build name→id mapping
                // Default colors cycle through a simple palette
                val colors = DefaultTagColorPalette.colors
                val colorGenerator = TagColorGenerator(colors)
                uniqueNames.forEachIndexed { index, name ->
                    val color = colorGenerator.getColorForIndex(index)
                    database.execSQL(
                        "INSERT INTO tags (name, color) VALUES (?, ?)",
                        arrayOf<Any>(name, color)
                    )
                    // Query to get the last inserted row id
                    val idCursor = database.query("SELECT last_insert_rowid()")
                    if (idCursor.moveToFirst()) {
                        nameToId[name] = idCursor.getLong(0)
                    }
                    idCursor.close()
                }

                // Migrate tasks row by row, converting tag names to tag IDs
                val tasksCursor = database.query("SELECT * FROM tasks")
                val idIndex = tasksCursor.getColumnIndex("id")
                val titleIndex = tasksCursor.getColumnIndex("title")
                val notesIndex = tasksCursor.getColumnIndex("notes")
                val urlIndex = tasksCursor.getColumnIndex("url")
                val dueDateIndex = tasksCursor.getColumnIndex("dueDate")
                val hasTimeIndex = tasksCursor.getColumnIndex("hasTime")
                val isUrgentIndex = tasksCursor.getColumnIndex("isUrgent")
                val priorityIndex = tasksCursor.getColumnIndex("priority")
                val tagsIndex = tasksCursor.getColumnIndex("tags")
                val isFlaggedIndex = tasksCursor.getColumnIndex("isFlagged")
                val isCompletedIndex = tasksCursor.getColumnIndex("isCompleted")
                val recurrenceTypeIndex = tasksCursor.getColumnIndex("recurrenceType")
                val createdAtIndex = tasksCursor.getColumnIndex("createdAt")

                while (tasksCursor.moveToNext()) {
                    val id = tasksCursor.getLong(idIndex)
                    val title = tasksCursor.getString(titleIndex)
                    val notes = tasksCursor.getString(notesIndex)
                    val url = tasksCursor.getString(urlIndex)
                    val dueDate = if (tasksCursor.isNull(dueDateIndex)) null else tasksCursor.getLong(dueDateIndex)
                    val hasTime = tasksCursor.getInt(hasTimeIndex)
                    val isUrgent = tasksCursor.getInt(isUrgentIndex)
                    val priority = tasksCursor.getString(priorityIndex)
                    val oldTags = tasksCursor.getString(tagsIndex)
                    val isFlagged = tasksCursor.getInt(isFlaggedIndex)
                    val isCompleted = tasksCursor.getInt(isCompletedIndex)
                    val recurrenceType = tasksCursor.getString(recurrenceTypeIndex)
                    val createdAt = tasksCursor.getLong(createdAtIndex)

                    // Convert tag names to tag IDs
                    val tagIds = TagMigrationMapper.mapToIdString(oldTags, nameToId)

                    // Insert into tasks_new
                    if (dueDate == null) {
                        database.execSQL(
                            """
                            INSERT INTO tasks_new
                            (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceType, createdAt)
                            VALUES (?, ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent(),
                            arrayOf<Any?>(id, title, notes, url, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceType, createdAt)
                        )
                    } else {
                        database.execSQL(
                            """
                            INSERT INTO tasks_new
                            (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceType, createdAt)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent(),
                            arrayOf<Any?>(id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceType, createdAt)
                        )
                    }
                }
                tasksCursor.close()

                database.execSQL("DROP TABLE `tasks`")
                database.execSQL("ALTER TABLE `tasks_new` RENAME TO `tasks`")
            }
        }

        // Replaces `recurrenceType TEXT` with `recurrenceUnit TEXT` + `recurrenceInterval INTEGER`.
        // Converts old enum names: DAILY→DAYS, WEEKLY→WEEKS, MONTHLY→MONTHS, NONE→NONE.
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
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
                        `recurrenceUnit` TEXT NOT NULL,
                        `recurrenceInterval` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                val cursor = database.query("SELECT * FROM tasks")
                val idIndex = cursor.getColumnIndex("id")
                val titleIndex = cursor.getColumnIndex("title")
                val notesIndex = cursor.getColumnIndex("notes")
                val urlIndex = cursor.getColumnIndex("url")
                val dueDateIndex = cursor.getColumnIndex("dueDate")
                val hasTimeIndex = cursor.getColumnIndex("hasTime")
                val isUrgentIndex = cursor.getColumnIndex("isUrgent")
                val priorityIndex = cursor.getColumnIndex("priority")
                val tagIdsIndex = cursor.getColumnIndex("tagIds")
                val isFlaggedIndex = cursor.getColumnIndex("isFlagged")
                val isCompletedIndex = cursor.getColumnIndex("isCompleted")
                val recurrenceTypeIndex = cursor.getColumnIndex("recurrenceType")
                val createdAtIndex = cursor.getColumnIndex("createdAt")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val title = cursor.getString(titleIndex)
                    val notes = cursor.getString(notesIndex)
                    val url = cursor.getString(urlIndex)
                    val dueDate = if (cursor.isNull(dueDateIndex)) null else cursor.getLong(dueDateIndex)
                    val hasTime = cursor.getInt(hasTimeIndex)
                    val isUrgent = cursor.getInt(isUrgentIndex)
                    val priority = cursor.getString(priorityIndex)
                    val tagIds = cursor.getString(tagIdsIndex)
                    val isFlagged = cursor.getInt(isFlaggedIndex)
                    val isCompleted = cursor.getInt(isCompletedIndex)
                    val recurrenceType = cursor.getString(recurrenceTypeIndex)
                    val createdAt = cursor.getLong(createdAtIndex)

                    val recurrenceUnit = RecurrenceMigrationMapper.map(recurrenceType)
                    val recurrenceInterval = 1

                    if (dueDate == null) {
                        database.execSQL(
                            """
                            INSERT INTO tasks_new
                            (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceUnit, recurrenceInterval, createdAt)
                            VALUES (?, ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent(),
                            arrayOf<Any?>(id, title, notes, url, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceUnit, recurrenceInterval, createdAt)
                        )
                    } else {
                        database.execSQL(
                            """
                            INSERT INTO tasks_new
                            (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceUnit, recurrenceInterval, createdAt)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent(),
                            arrayOf<Any?>(id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceUnit, recurrenceInterval, createdAt)
                        )
                    }
                }
                cursor.close()

                database.execSQL("DROP TABLE `tasks`")
                database.execSQL("ALTER TABLE `tasks_new` RENAME TO `tasks`")
            }
        }

    }
}
