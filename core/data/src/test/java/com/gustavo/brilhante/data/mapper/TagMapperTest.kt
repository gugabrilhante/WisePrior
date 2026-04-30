package com.gustavo.brilhante.data.mapper

import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.storage.entity.TagEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class TagMapperTest {

    private val sampleEntity = TagEntity(id = 1L, name = "Work", color = 0xFF3B82F6L)
    private val sampleTag    = Tag(id = 1L,       name = "Work", color = 0xFF3B82F6L)

    @Test
    fun `given a TagEntity, when toModel called, then all fields map correctly`() {
        val model = sampleEntity.toModel()
        assertEquals(sampleTag, model)
    }

    @Test
    fun `given a Tag, when toEntity called, then all fields map correctly`() {
        val entity = sampleTag.toEntity()
        assertEquals(sampleEntity, entity)
    }

    @Test
    fun `given a Tag, when toEntity then toModel, then round-trip preserves all fields`() {
        val roundTripped = sampleTag.toEntity().toModel()
        assertEquals(sampleTag, roundTripped)
    }

    @Test
    fun `given a TagEntity, when toModel then toEntity, then round-trip preserves all fields`() {
        val roundTripped = sampleEntity.toModel().toEntity()
        assertEquals(sampleEntity, roundTripped)
    }

    @Test
    fun `given tag with id zero, when toEntity called, then entity id is zero`() {
        val tag = Tag(id = 0L, name = "New Tag", color = 0xFFFFFFFL)
        assertEquals(0L, tag.toEntity().id)
    }
}
