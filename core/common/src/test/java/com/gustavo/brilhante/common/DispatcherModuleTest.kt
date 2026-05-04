package com.gustavo.brilhante.common

import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

class DispatcherModuleTest {

    @Test
    fun testProvidesDefaultDispatcher() {
        assertEquals(Dispatchers.Default, DispatcherModule.providesDefaultDispatcher())
    }

    @Test
    fun testProvidesIoDispatcher() {
        assertEquals(Dispatchers.IO, DispatcherModule.providesIoDispatcher())
    }

    @Test
    fun testProvidesMainDispatcher() {
        assertEquals(Dispatchers.Main, DispatcherModule.providesMainDispatcher())
    }
}
