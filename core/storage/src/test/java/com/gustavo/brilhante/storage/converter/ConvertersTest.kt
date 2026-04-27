package com.gustavo.brilhante.storage.converter

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromTagsList joins tags with comma`() {
        val tags = listOf("work", "urgent", "home")
        assertEquals("work,urgent,home", converters.fromTagsList(tags))
    }

    @Test
    fun `fromTagsList returns empty string for empty list`() {
        assertEquals("", converters.fromTagsList(emptyList()))
    }

    @Test
    fun `toTagsList splits comma-separated string into list`() {
        val result = converters.toTagsList("work,urgent,home")
        assertEquals(listOf("work", "urgent", "home"), result)
    }

    @Test
    fun `toTagsList returns empty list for blank string`() {
        assertEquals(emptyList<String>(), converters.toTagsList(""))
        assertEquals(emptyList<String>(), converters.toTagsList("   "))
    }

    @Test
    fun `fromTagsList and toTagsList are inverse operations`() {
        val original = listOf("alpha", "beta", "gamma")
        val encoded = converters.fromTagsList(original)
        val decoded = converters.toTagsList(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `single tag round-trips correctly`() {
        val tags = listOf("solo")
        assertEquals(tags, converters.toTagsList(converters.fromTagsList(tags)))
    }
}
