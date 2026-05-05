package com.gustavo.brilhante.storage.datasources

import com.gustavo.brilhante.storage.dao.TagDao
import com.gustavo.brilhante.storage.entity.TagEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TagDataSourceTest {

    private val tagDao: TagDao = mockk(relaxed = true)
    private val dataSource = TagDataSource(tagDao)

    @Test
    fun `test allTags flow`() = runTest {
        val tags = listOf(TagEntity(id = 1, name = "Work", color = 0))
        every { tagDao.getAllTags() } returns flowOf(tags)

        dataSource.allTags.collect {
            assertEquals(tags, it)
        }
    }

    @Test
    fun `test insertTag`() = runTest {
        val tag = TagEntity(name = "Work", color = 0)
        coEvery { tagDao.insertTag(tag) } returns 1L

        val result = dataSource.insertTag(tag)

        assertEquals(1L, result)
        coVerify { tagDao.insertTag(tag) }
    }

    @Test
    fun `test updateTag`() = runTest {
        val tag = TagEntity(id = 1, name = "Work", color = 0)
        coEvery { tagDao.updateTag(tag) } returns Unit

        dataSource.updateTag(tag)

        coVerify { tagDao.updateTag(tag) }
    }

    @Test
    fun `test deleteTagTransactional`() = runTest {
        val tag = TagEntity(id = 1, name = "Work", color = 0)
        coEvery { tagDao.deleteTagAndRemoveFromTasks(tag) } returns Unit

        dataSource.deleteTagTransactional(tag)

        coVerify { tagDao.deleteTagAndRemoveFromTasks(tag) }
    }
}
