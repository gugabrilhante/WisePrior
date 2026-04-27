package com.gustavo.brilhante.storage.converter

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromLongList joins ids with comma`() {
        val ids = listOf(1L, 2L, 3L)
        assertEquals("1,2,3", converters.fromLongList(ids))
    }

    @Test
    fun `fromLongList returns empty string for empty list`() {
        assertEquals("", converters.fromLongList(emptyList()))
    }

    @Test
    fun `toLongList splits comma-separated string into list`() {
        val result = converters.toLongList("1,2,3")
        assertEquals(listOf(1L, 2L, 3L), result)
    }

    @Test
    fun `toLongList returns empty list for blank string`() {
        assertEquals(emptyList<Long>(), converters.toLongList(""))
        assertEquals(emptyList<Long>(), converters.toLongList("   "))
    }

    @Test
    fun `toLongList silently drops malformed tokens`() {
        val result = converters.toLongList("1, abc, 2")
        assertEquals(listOf(1L, 2L), result)
    }

    @Test
    fun `fromLongList and toLongList are inverse operations`() {
        val original = listOf(10L, 20L, 30L)
        val encoded = converters.fromLongList(original)
        val decoded = converters.toLongList(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `single id round-trips correctly`() {
        val ids = listOf(99L)
        assertEquals(ids, converters.toLongList(converters.fromLongList(ids)))
    }
}
