package com.gustavo.brilhante.tasklist.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutModeResolverTest {

    private val resolver = LayoutModeResolver()

    @Test
    fun `isExpanded should return true if width is 600 or more`() {
        assertTrue(resolver.isExpanded(600))
        assertTrue(resolver.isExpanded(800))
    }

    @Test
    fun `isExpanded should return false if width is less than 600`() {
        assertFalse(resolver.isExpanded(599))
        assertFalse(resolver.isExpanded(400))
    }
}
