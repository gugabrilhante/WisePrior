package com.gustavo.brilhante.data.repository

import app.cash.turbine.test
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.storage.datasources.TagDataSource
import com.gustavo.brilhante.storage.entity.TagEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TagRepositoryImplTest {

    private val dataSource: TagDataSource = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = TagRepositoryImpl(dataSource, testDispatcher)

    private val entity = TagEntity(id = 1L, name = "Work", color = 0xFF3B82F6L)
    private val tag    = Tag(id = 1L,       name = "Work", color = 0xFF3B82F6L)

    @Test
    fun `given data source has tags, when getTags called, then maps entities to domain models`() = runTest {
        every { dataSource.allTags } returns flowOf(listOf(entity))

        repository.getTags().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(tag, items.first())
            awaitComplete()
        }
    }

    @Test
    fun `given data source is empty, when getTags called, then emits empty list`() = runTest {
        every { dataSource.allTags } returns flowOf(emptyList())

        repository.getTags().test {
            assertEquals(emptyList<Tag>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `given a tag, when addTag called, then inserts mapped entity and returns generated id`() = runTest {
        val slot = slot<TagEntity>()
        coEvery { dataSource.insertTag(capture(slot)) } returns 99L

        val result = repository.addTag(tag)

        assertEquals(99L, result)
        coVerify(exactly = 1) { dataSource.insertTag(any()) }
        assertEquals(tag.id,    slot.captured.id)
        assertEquals(tag.name,  slot.captured.name)
        assertEquals(tag.color, slot.captured.color)
    }

    @Test
    fun `given a tag, when updateTag called, then passes mapped entity to data source`() = runTest {
        val slot = slot<TagEntity>()
        coEvery { dataSource.updateTag(capture(slot)) } returns Unit

        repository.updateTag(tag)

        coVerify(exactly = 1) { dataSource.updateTag(any()) }
        assertEquals(tag.name,  slot.captured.name)
        assertEquals(tag.color, slot.captured.color)
    }

    @Test
    fun `given a tag, when deleteTag called, then invokes transactional delete on data source`() = runTest {
        val slot = slot<TagEntity>()
        coEvery { dataSource.deleteTagTransactional(capture(slot)) } returns Unit

        repository.deleteTag(tag)

        coVerify(exactly = 1) { dataSource.deleteTagTransactional(any()) }
        assertEquals(tag.id,   slot.captured.id)
        assertEquals(tag.name, slot.captured.name)
    }
}
