package com.gustavo.brilhante.data.system

import org.junit.Assert.assertFalse
import org.junit.Test

class TestEnvironmentProviderImplTest {

    @Test
    fun `isTesting should return false when running unit tests because Espresso is not in classpath`() {
        val provider = TestEnvironmentProviderImpl()
        val result = provider.isTesting()
        assertFalse(result)
    }
}
