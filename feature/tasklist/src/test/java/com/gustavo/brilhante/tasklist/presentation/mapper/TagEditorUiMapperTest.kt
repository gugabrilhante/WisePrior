package com.gustavo.brilhante.tasklist.presentation.mapper

import com.gustavo.brilhante.model.Tag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TagEditorUiMapperTest {

    private val mapper = TagEditorUiMapper()

    @Test
    fun `map should return new tag state when editingTag is null`() {
        val result = mapper.map(null, 123L)

        assertEquals("", result.initialName)
        assertEquals(123L, result.initialColor)
        assertFalse(result.showDelete)
    }

    @Test
    fun `map should return edit tag state when editingTag is provided`() {
        val tag = Tag(id = 1, name = "Work", color = 456L)
        val result = mapper.map(tag, 123L)

        assertEquals("Work", result.initialName)
        assertEquals(456L, result.initialColor)
        assertTrue(result.showDelete)
    }
}
