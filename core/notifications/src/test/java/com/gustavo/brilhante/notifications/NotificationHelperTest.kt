package com.gustavo.brilhante.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class NotificationHelperTest {

    private val context: Context = mockk(relaxed = true) {
        every { applicationContext } returns this@mockk
    }
    private val notificationManager: NotificationManager = mockk(relaxed = true)
    private lateinit var helper: NotificationHelper

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0

        every { context.getSystemService(NotificationManager::class.java) } returns notificationManager
        every { context.packageName } returns "com.gustavo.brilhante"
        every { context.applicationContext } returns context
        
        mockkStatic(PendingIntent::class)
        every { PendingIntent.getActivity(any(), any(), any(), any()) } returns mockk(relaxed = true)

        helper = NotificationHelper(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when createChannel called, then creates notification channel`() {
        helper.createChannel()

        verify { notificationManager.createNotificationChannel(any<NotificationChannel>()) }
    }

    @Test
    fun `when showNotification called, then notifies notification manager`() {
        mockkStatic(NotificationManagerCompat::class)
        val mockManagerCompat: NotificationManagerCompat = mockk(relaxed = true)
        every { NotificationManagerCompat.from(context) } returns mockManagerCompat

        helper.showNotification(1L, "Title", "Notes")

        verify { mockManagerCompat.notify(any(), any()) }
    }
}
