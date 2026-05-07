package com.gustavo.brilhante.ui

import android.content.Context
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class UiTextTest {

    @Test
    fun `DynamicString asString(context) returns its value`() {
        val context = mockk<Context>()
        val uiText = UiText.DynamicString("Hello")
        
        assertEquals("Hello", uiText.asString(context))
    }

    @Test
    fun `StringResource asString(context) calls context getString with args`() {
        val context = mockk<Context>()
        val resId = 123
        val args = listOf("Arg1", 2)
        val uiText = UiText.StringResource(resId, args)
        
        every { context.getString(resId, "Arg1", 2) } returns "Formatted String"
        
        assertEquals("Formatted String", uiText.asString(context))
    }

    @Test
    fun `PluralResource asString(context) calls context resources getQuantityString with args`() {
        val context = mockk<Context>()
        val resources = mockk<Resources>()
        val resId = 456
        val count = 5
        val args = listOf("ArgA")
        val uiText = UiText.PluralResource(resId, count, args)
        
        every { context.resources } returns resources
        every { resources.getQuantityString(resId, count, "ArgA") } returns "Plural Form"
        
        assertEquals("Plural Form", uiText.asString(context))
    }

    @Test
    fun `UiText equality works correctly`() {
        val ds1 = UiText.DynamicString("test")
        val ds2 = UiText.DynamicString("test")
        val ds3 = UiText.DynamicString("other")
        
        assertEquals(ds1, ds2)
        assert(ds1 != ds3)
        
        val sr1 = UiText.StringResource(1, listOf("a"))
        val sr2 = UiText.StringResource(1, listOf("a"))
        val sr3 = UiText.StringResource(2, listOf("a"))
        
        assertEquals(sr1, sr2)
        assert(sr1 != sr3)
    }
}
