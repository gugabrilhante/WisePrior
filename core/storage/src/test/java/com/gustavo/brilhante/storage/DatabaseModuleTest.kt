package com.gustavo.brilhante.storage

import android.content.Context
import com.gustavo.brilhante.storage.database.AppDatabase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Test

class DatabaseModuleTest {

    @Test
    fun `test provideDatabase`() {
        val context: Context = mockk(relaxed = true)
        val db = DatabaseModule.provideDatabase(context)
        assertNotNull(db)
    }

    @Test
    fun `test provideTaskDao`() {
        val db: AppDatabase = mockk()
        every { db.taskDao() } returns mockk()
        val dao = DatabaseModule.provideTaskDao(db)
        assertNotNull(dao)
    }

    @Test
    fun `test provideTagDao`() {
        val db: AppDatabase = mockk()
        every { db.tagDao() } returns mockk()
        val dao = DatabaseModule.provideTagDao(db)
        assertNotNull(dao)
    }
}
