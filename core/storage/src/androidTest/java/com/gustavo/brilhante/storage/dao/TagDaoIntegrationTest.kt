package com.gustavo.brilhante.storage.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gustavo.brilhante.storage.database.AppDatabase
import com.gustavo.brilhante.storage.entity.TagEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var tagDao: TagDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tagDao = database.tagDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetTags() = runTest {
        val tag = TagEntity(name = "Work", color = 0xFF0000)
        val id = tagDao.insertTag(tag)
        
        val allTags = tagDao.getAllTags().first()
        assertEquals(1, allTags.size)
        assertEquals(tag.copy(id = id), allTags[0])
    }

    @Test
    fun updateTag() = runTest {
        val tag = TagEntity(name = "Work", color = 0xFF0000)
        val id = tagDao.insertTag(tag)
        val updatedTag = tag.copy(id = id, name = "Home")
        
        tagDao.updateTag(updatedTag)
        
        val allTags = tagDao.getAllTags().first()
        assertEquals("Home", allTags[0].name)
    }

    @Test
    fun deleteTag() = runTest {
        val tag = TagEntity(name = "Work", color = 0xFF0000)
        val id = tagDao.insertTag(tag)
        
        tagDao.deleteTagAndRemoveFromTasks(tag.copy(id = id))
        
        val allTags = tagDao.getAllTags().first()
        assertTrue(allTags.isEmpty())
    }
}
