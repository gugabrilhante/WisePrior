package com.gustavo.brilhante.tasklist.di

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Test

class SortModuleTest {

    @Test
    fun `test provideSortDataStore`() {
        val context: Context = mockk(relaxed = true)
        val dataStore = SortModule.provideSortDataStore(context)
        assertNotNull(dataStore)
    }
}
