package com.gustavo.brilhante.notifications

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class BootReceiverTest {

    private val context: Context = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val receiver = BootReceiver()

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        
        every { context.applicationContext } returns context
        every { context.getApplicationContext() } returns context
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `given boot intent, when onReceive called, then enqueues RescheduleNotificationsWorker`() {
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)

        receiver.onReceive(context, intent)

        verify { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `given other intent, when onReceive called, then does nothing`() {
        val intent = Intent(Intent.ACTION_SEND)

        receiver.onReceive(context, intent)

        verify(exactly = 0) { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }
}
