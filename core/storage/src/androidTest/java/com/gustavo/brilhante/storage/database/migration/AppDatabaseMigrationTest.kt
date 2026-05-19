package com.gustavo.brilhante.storage.database.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.gustavo.brilhante.model.DefaultTagColorPalette
import com.gustavo.brilhante.storage.database.AppDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate3To4() {
        var db = helper.createDatabase(TEST_DB, 3)

        // Validate column doesn't exist
        var cursor = db.query("PRAGMA table_info(tasks)")
        var found = false
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex("name")) == "recurrenceType") {
                found = true
            }
        }
        cursor.close()
        assertThat(found).isFalse()

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, AppDatabase.MIGRATION_3_4)

        // Validate column exists
        cursor = db.query("PRAGMA table_info(tasks)")
        found = false
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex("name")) == "recurrenceType") {
                found = true
            }
        }
        cursor.close()
        assertThat(found).isTrue()
    }

    @Test
    fun migrate4To5() {
        var db = helper.createDatabase(TEST_DB, 4)

        // Validate column doesn't exist
        var cursor = db.query("PRAGMA table_info(tasks)")
        var found = false
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex("name")) == "isCompleted") {
                found = true
            }
        }
        cursor.close()
        assertThat(found).isFalse()

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

        // Validate column exists
        cursor = db.query("PRAGMA table_info(tasks)")
        found = false
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex("name")) == "isCompleted") {
                found = true
            }
        }
        cursor.close()
        assertThat(found).isTrue()
    }

    @Test
    fun migrate5To6() {
        val db5 = helper.createDatabase(TEST_DB, 5)

        // Seed v5 database
        db5.execSQL(
            """
            INSERT INTO tasks (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tags, isFlagged, isCompleted, recurrenceType, createdAt)
            VALUES (1, 'No tags', '', '', NULL, 0, 0, 'LOW', '', 0, 0, 'NONE', 1000)
            """.trimIndent()
        )
        db5.execSQL(
            """
            INSERT INTO tasks (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tags, isFlagged, isCompleted, recurrenceType, createdAt)
            VALUES (2, 'One tag', '', '', 2000, 1, 1, 'HIGH', 'Work', 1, 0, 'DAILY', 1000)
            """.trimIndent()
        )
        db5.execSQL(
            """
            INSERT INTO tasks (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tags, isFlagged, isCompleted, recurrenceType, createdAt)
            VALUES (3, 'Multiple tags', '', '', 3000, 1, 0, 'MEDIUM', 'Work, Personal , Urgent', 0, 1, 'WEEKLY', 1000)
            """.trimIndent()
        )
        db5.execSQL(
            """
            INSERT INTO tasks (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tags, isFlagged, isCompleted, recurrenceType, createdAt)
            VALUES (4, 'Duplicated tags', '', '', 4000, 0, 0, 'LOW', 'Work,Work,Personal', 0, 0, 'MONTHLY', 1000)
            """.trimIndent()
        )
        db5.close()

        val db6 = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        // Validate tags table
        val tagsCursor = db6.query("SELECT * FROM tags ORDER BY id ASC")
        val tags = mutableListOf<Pair<String, Long>>()
        while (tagsCursor.moveToNext()) {
            tags.add(tagsCursor.getString(tagsCursor.getColumnIndex("name")) to tagsCursor.getLong(tagsCursor.getColumnIndex("color")))
        }
        tagsCursor.close()

        // Work, Personal, Urgent
        assertThat(tags).hasSize(3)
        assertThat(tags.map { it.first }).containsExactly("Work", "Personal", "Urgent").inOrder()
        assertThat(tags[0].second).isEqualTo(DefaultTagColorPalette.RED)
        assertThat(tags[1].second).isEqualTo(DefaultTagColorPalette.BLUE)
        assertThat(tags[2].second).isEqualTo(DefaultTagColorPalette.GREEN)

        // Validate tasks table
        val tasksCursor = db6.query("SELECT id, title, tagIds, dueDate FROM tasks ORDER BY id ASC")

        // Task 1: No tags
        tasksCursor.moveToNext()
        assertThat(tasksCursor.getLong(tasksCursor.getColumnIndex("id"))).isEqualTo(1)
        assertThat(tasksCursor.getString(tasksCursor.getColumnIndex("tagIds"))).isEqualTo("")
        assertThat(tasksCursor.isNull(tasksCursor.getColumnIndex("dueDate"))).isTrue()

        // Task 2: One tag (Work)
        tasksCursor.moveToNext()
        assertThat(tasksCursor.getLong(tasksCursor.getColumnIndex("id"))).isEqualTo(2)
        assertThat(tasksCursor.getString(tasksCursor.getColumnIndex("tagIds"))).isEqualTo("1") // Work is first inserted, id 1
        assertThat(tasksCursor.getLong(tasksCursor.getColumnIndex("dueDate"))).isEqualTo(2000)

        // Task 3: Multiple tags (Work, Personal, Urgent)
        tasksCursor.moveToNext()
        assertThat(tasksCursor.getLong(tasksCursor.getColumnIndex("id"))).isEqualTo(3)
        assertThat(tasksCursor.getString(tasksCursor.getColumnIndex("tagIds"))).isEqualTo("1,2,3")

        // Task 4: Duplicated tags (Work, Personal)
        tasksCursor.moveToNext()
        assertThat(tasksCursor.getLong(tasksCursor.getColumnIndex("id"))).isEqualTo(4)
        assertThat(tasksCursor.getString(tasksCursor.getColumnIndex("tagIds"))).isEqualTo("1,2")

        tasksCursor.close()
    }

    @Test
    fun migrate7To8() {
        val db7 = helper.createDatabase(TEST_DB, 7)

        // Seed a task at v7
        db7.execSQL(
            """
            INSERT INTO tasks (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceUnit, recurrenceInterval, createdAt)
            VALUES (1, 'Shopping', '', '', NULL, 0, 0, 'NONE', '', 0, 0, 'NONE', 1, 1000)
            """.trimIndent()
        )
        db7.close()

        val db8 = helper.runMigrationsAndValidate(TEST_DB, 8, true, AppDatabase.MIGRATION_7_8)

        // checklist_items table must exist and be empty for existing tasks
        val cursor = db8.query("SELECT * FROM checklist_items")
        assertThat(cursor.count).isEqualTo(0)
        cursor.close()

        // Verify schema columns
        val schemaCursor = db8.query("PRAGMA table_info(checklist_items)")
        val columns = mutableListOf<String>()
        while (schemaCursor.moveToNext()) {
            columns.add(schemaCursor.getString(schemaCursor.getColumnIndex("name")))
        }
        schemaCursor.close()
        assertThat(columns).containsAtLeast("id", "taskId", "text", "isChecked")
    }

    @Test
    fun migrate6To7() {
        val db6 = helper.createDatabase(TEST_DB, 6)

        // Seed v6 database
        db6.execSQL(
            """
            INSERT INTO tasks (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceType, createdAt)
            VALUES (1, 'Daily', '', '', 1000, 0, 0, 'LOW', '', 0, 0, 'DAILY', 1000)
            """.trimIndent()
        )
        db6.execSQL(
            """
            INSERT INTO tasks (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceType, createdAt)
            VALUES (2, 'Weekly', '', '', 2000, 0, 0, 'LOW', '', 0, 0, 'WEEKLY', 1000)
            """.trimIndent()
        )
        db6.execSQL(
            """
            INSERT INTO tasks (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceType, createdAt)
            VALUES (3, 'Monthly', '', '', 3000, 0, 0, 'LOW', '', 0, 0, 'MONTHLY', 1000)
            """.trimIndent()
        )
        db6.execSQL(
            """
            INSERT INTO tasks (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceType, createdAt)
            VALUES (4, 'None', '', '', 4000, 0, 0, 'LOW', '', 0, 0, 'NONE', 1000)
            """.trimIndent()
        )
        db6.execSQL(
            """
            INSERT INTO tasks (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceType, createdAt)
            VALUES (5, 'Invalid', '', '', 5000, 0, 0, 'LOW', '', 0, 0, 'UNKNOWN', 1000)
            """.trimIndent()
        )
        db6.execSQL(
            """
            INSERT INTO tasks (id, title, notes, url, dueDate, hasTime, isUrgent, priority, tagIds, isFlagged, isCompleted, recurrenceType, createdAt)
            VALUES (6, 'Null dueDate', '', '', NULL, 0, 0, 'LOW', '', 0, 0, 'NONE', 1000)
            """.trimIndent()
        )
        db6.close()

        val db7 = helper.runMigrationsAndValidate(TEST_DB, 7, true, AppDatabase.MIGRATION_6_7)

        val cursor = db7.query("SELECT id, recurrenceUnit, recurrenceInterval, dueDate FROM tasks ORDER BY id ASC")

        // Task 1: DAILY -> DAYS, interval 1
        cursor.moveToNext()
        assertThat(cursor.getString(cursor.getColumnIndex("recurrenceUnit"))).isEqualTo("DAYS")
        assertThat(cursor.getInt(cursor.getColumnIndex("recurrenceInterval"))).isEqualTo(1)

        // Task 2: WEEKLY -> WEEKS, interval 1
        cursor.moveToNext()
        assertThat(cursor.getString(cursor.getColumnIndex("recurrenceUnit"))).isEqualTo("WEEKS")
        assertThat(cursor.getInt(cursor.getColumnIndex("recurrenceInterval"))).isEqualTo(1)

        // Task 3: MONTHLY -> MONTHS, interval 1
        cursor.moveToNext()
        assertThat(cursor.getString(cursor.getColumnIndex("recurrenceUnit"))).isEqualTo("MONTHS")
        assertThat(cursor.getInt(cursor.getColumnIndex("recurrenceInterval"))).isEqualTo(1)

        // Task 4: NONE -> NONE, interval 1
        cursor.moveToNext()
        assertThat(cursor.getString(cursor.getColumnIndex("recurrenceUnit"))).isEqualTo("NONE")
        assertThat(cursor.getInt(cursor.getColumnIndex("recurrenceInterval"))).isEqualTo(1)

        // Task 5: UNKNOWN -> NONE, interval 1
        cursor.moveToNext()
        assertThat(cursor.getString(cursor.getColumnIndex("recurrenceUnit"))).isEqualTo("NONE")
        assertThat(cursor.getInt(cursor.getColumnIndex("recurrenceInterval"))).isEqualTo(1)

        // Task 6: Null dueDate
        cursor.moveToNext()
        assertThat(cursor.getString(cursor.getColumnIndex("recurrenceUnit"))).isEqualTo("NONE")
        assertThat(cursor.isNull(cursor.getColumnIndex("dueDate"))).isTrue()

        cursor.close()
    }
}
