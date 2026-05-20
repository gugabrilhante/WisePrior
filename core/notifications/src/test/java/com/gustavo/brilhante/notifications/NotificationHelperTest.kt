package com.gustavo.brilhante.notifications

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class NotificationHelperTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var helper: NotificationHelper

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        helper = NotificationHelper(context)
        
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when createChannel called, then creates notification channel`() {
        helper.createChannel()

        val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
        assert(channel != null)
        assert(channel.id == CHANNEL_ID)
    }

    @Test
    fun `when showNotification called, then notifies notification manager`() {
        mockkStatic(NotificationManagerCompat::class)
        val mockManagerCompat: NotificationManagerCompat = mockk(relaxed = true)
        every { NotificationManagerCompat.from(any()) } returns mockManagerCompat
        every { mockManagerCompat.areNotificationsEnabled() } returns true

        helper.showNotification(1L, "Title", "Notes")

        verify { mockManagerCompat.notify(any(), any()) }
    }
}
