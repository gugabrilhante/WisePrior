package com.gustavo.brilhante.notifications

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class AlarmReceiverTest {

    private lateinit var context: Context
    private val mockNotificationHelper: NotificationHelper = mockk(relaxed = true)
    private val mockScheduler: AlarmManagerNotificationScheduler = mockk(relaxed = true)
    private val receiver = AlarmReceiver().apply {
        notificationHelper = mockNotificationHelper
        scheduler = mockScheduler
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `given valid intent, when onReceive called, then shows notification`() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, 1L)
            putExtra(EXTRA_TASK_TITLE, "Test Task")
            putExtra(EXTRA_TASK_NOTES, "Notes")
            putExtra(EXTRA_DUE_DATE, System.currentTimeMillis())
        }

        receiver.performReceive(intent)

        verify { mockNotificationHelper.showNotification(1L, "Test Task", "Notes") }
    }
}
