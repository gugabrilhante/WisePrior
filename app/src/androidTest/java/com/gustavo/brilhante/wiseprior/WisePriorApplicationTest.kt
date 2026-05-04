package com.gustavo.brilhante.wiseprior

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gustavo.brilhante.wiseprior.application.WisePriorApplication
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WisePriorApplicationTest {

    @Test
    fun testApplication_isCorrectType() {
        val app = ApplicationProvider.getApplicationContext<WisePriorApplication>()
        assertNotNull(app)
    }

    @Test
    fun testWorkManagerConfiguration() {
        val app = ApplicationProvider.getApplicationContext<WisePriorApplication>()
        val config = app.workManagerConfiguration
        assertNotNull(config)
    }
}
