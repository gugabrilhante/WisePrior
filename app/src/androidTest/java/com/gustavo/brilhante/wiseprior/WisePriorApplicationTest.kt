package com.gustavo.brilhante.wiseprior

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WisePriorApplicationTest {

    // HiltTestRunner replaces WisePriorApplication with HiltTestApplication,
    // so tests verify the app context and WorkManager (initialized in HiltTestRunner) are available.

    @Test
    fun testApplication_isNotNull() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        assertNotNull(app)
    }

    @Test
    fun testWorkManagerConfiguration_isInitialized() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        assertNotNull(WorkManager.getInstance(context))
    }
}
