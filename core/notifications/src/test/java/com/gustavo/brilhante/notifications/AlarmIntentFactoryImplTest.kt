package com.gustavo.brilhante.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class AlarmIntentFactoryImplTest {

    private lateinit var context: Context
    private val mockIntentBuilder: AlarmIntentBuilder = mockk()
    private lateinit var factory: AlarmIntentFactoryImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        factory = AlarmIntentFactoryImpl(context, mockIntentBuilder)
    }

    @Test
    fun `createPendingIntent should call builder and return broadcast PendingIntent`() {
        val taskId = 1L
        val intent = Intent(context, AlarmReceiver::class.java)
        val recurrenceRule = RecurrenceRule(RecurrenceUnit.NONE, 0)
        
        every { 
            mockIntentBuilder.buildAlarmIntent(taskId, any(), any(), any(), any(), any()) 
        } returns intent
        
        val pi = factory.createPendingIntent(taskId, "Title", "Notes", 1000L, true, recurrenceRule)
        
        assertNotNull(pi)
        verify { 
            mockIntentBuilder.buildAlarmIntent(taskId, "Title", "Notes", 1000L, true, recurrenceRule) 
        }
    }

    @Test
    fun `getExistingPendingIntent should return null if no PendingIntent exists`() {
        val taskId = 2L
        val intent = Intent(context, AlarmReceiver::class.java)
        every { mockIntentBuilder.buildBaseAlarmIntent() } returns intent
        
        val pi = factory.getExistingPendingIntent(taskId)
        
        assertNull(pi)
        verify { mockIntentBuilder.buildBaseAlarmIntent() }
    }

    @Test
    fun `createShowDetailsPendingIntent should call builder and return activity PendingIntent`() {
        val taskId = 3L
        val intent = Intent("SHOW_DETAILS")
        every { mockIntentBuilder.buildShowDetailsIntent(taskId) } returns intent
        
        val pi = factory.createShowDetailsPendingIntent(taskId)
        
        assertNotNull(pi)
        verify { mockIntentBuilder.buildShowDetailsIntent(taskId) }
    }
}
