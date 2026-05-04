package com.gustavo.brilhante.notifications

import android.content.Context
import android.content.Intent
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class AlarmReceiverTest {

    private val context: Context = mockk(relaxed = true) {
        every { applicationContext } returns this@mockk
    }
    private val mockNotificationHelper: NotificationHelper = mockk(relaxed = true)
    private val mockScheduler: AlarmManagerNotificationScheduler = mockk(relaxed = true)
    private val receiver = AlarmReceiver().apply {
        notificationHelper = mockNotificationHelper
        scheduler = mockScheduler
    }

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `given valid intent, when onReceive called, then shows notification`() {
        val mockIntent: Intent = mockk(relaxed = true)
        every { mockIntent.getLongExtra(EXTRA_TASK_ID, any()) } returns 1L
        every { mockIntent.getStringExtra(EXTRA_TASK_TITLE) } returns "Test Task"
        every { mockIntent.getStringExtra(EXTRA_TASK_NOTES) } returns "Notes"
        every { mockIntent.getLongExtra(EXTRA_DUE_DATE, any()) } returns System.currentTimeMillis()

        receiver.performReceive(mockIntent)

        verify { mockNotificationHelper.showNotification(1L, "Test Task", "Notes") }
    }
}
