package com.gustavo.brilhante.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gustavo.brilhante.model.Task
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class AlarmManagerNotificationSchedulerTest {

    private val context: Context = mockk(relaxed = true) {
        every { applicationContext } returns this@mockk
    }
    private val alarmManager: AlarmManager = mockk(relaxed = true)
    private lateinit var scheduler: AlarmManagerNotificationScheduler

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0

        every { context.getSystemService(AlarmManager::class.java) } returns alarmManager
        every { context.applicationContext } returns context

        mockkStatic(PendingIntent::class)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockk(relaxed = true)
        
        mockkConstructor(Intent::class)

        scheduler = AlarmManagerNotificationScheduler(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `given task with due date, when schedule called, then sets exact alarm`() {
        val futureTime = System.currentTimeMillis() + 100000
        val task = Task(id = 1, title = "Test Task", dueDate = futureTime)

        every { alarmManager.canScheduleExactAlarms() } returns true

        scheduler.schedule(task)

        verify {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                futureTime,
                any()
            )
        }
    }

    @Test
    fun `given task with past due date and no recurrence, when schedule called, then skips`() {
        val pastTime = System.currentTimeMillis() - 100000
        val task = Task(id = 1, title = "Test Task", dueDate = pastTime)

        scheduler.schedule(task)

        verify(exactly = 0) {
            alarmManager.setExactAndAllowWhileIdle(any(), any(), any())
            alarmManager.setAndAllowWhileIdle(any(), any(), any())
        }
    }

    @Test
    fun `given taskId, when cancel called, then cancels alarm`() {
        val taskId = 1L
        val mockPendingIntent: PendingIntent = mockk(relaxed = true)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockPendingIntent

        scheduler.cancel(taskId)

        verify {
            alarmManager.cancel(mockPendingIntent)
            mockPendingIntent.cancel()
        }
    }
}
