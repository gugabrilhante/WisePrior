package com.gustavo.brilhante.notifications

import android.content.Context
import android.content.Intent
import com.gustavo.brilhante.domain.logging.Logger
import androidx.test.core.app.ApplicationProvider
import com.gustavo.brilhante.model.RecurrenceUnit
import io.mockk.every
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
    private val mockScheduler: NotificationScheduler = mockk(relaxed = true)
    private val mockLogger: Logger = mockk(relaxed = true)
    
    private val receiver = AlarmReceiver().apply {
        notificationHelper = mockNotificationHelper
        scheduler = mockScheduler
        logger = mockLogger
    }

    private val TAG = "AlarmReceiver"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `given valid intent, when performReceive called, then shows notification and logs success`() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, 1L)
            putExtra(EXTRA_TASK_TITLE, "Test Task")
            putExtra(EXTRA_TASK_NOTES, "Notes")
            putExtra(EXTRA_DUE_DATE, 1000L)
            putExtra(EXTRA_HAS_TIME, true)
        }

        receiver.performReceive(intent)

        verify { mockNotificationHelper.showNotification(1L, "Test Task", "Notes") }
        verify { mockLogger.d(TAG, "Alarm fired for task 1: Test Task") }
    }

    @Test
    fun `given missing title, when performReceive called, then logs warning and returns`() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, 1L)
        }

        receiver.performReceive(intent)

        verify { mockLogger.w(TAG, match { it.contains("Missing title") }) }
        verify(exactly = 0) { mockNotificationHelper.showNotification(any(), any(), any()) }
    }

    @Test
    fun `given invalid taskId, when performReceive called, then logs warning and returns`() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, -1L)
            putExtra(EXTRA_TASK_TITLE, "Title")
            putExtra(EXTRA_DUE_DATE, 1000L)
        }

        receiver.performReceive(intent)

        verify { mockLogger.w(TAG, match { it.contains("Invalid alarm intent") }) }
        verify(exactly = 0) { mockNotificationHelper.showNotification(any(), any(), any()) }
    }

    @Test
    fun `given invalid dueDate, when performReceive called, then logs warning and returns`() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, 1L)
            putExtra(EXTRA_TASK_TITLE, "Title")
            putExtra(EXTRA_DUE_DATE, -1L)
        }

        receiver.performReceive(intent)

        verify { mockLogger.w(TAG, match { it.contains("Invalid alarm intent") }) }
        verify(exactly = 0) { mockNotificationHelper.showNotification(any(), any(), any()) }
    }

    @Test
    fun `given recurring task, when performReceive called, then reschedules next occurrence`() {
        val taskId = 1L
        val title = "Recurring Task"
        val dueDate = 1000L
        val nextDueDate = 2000L
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_DUE_DATE, dueDate)
            putExtra(EXTRA_RECURRENCE_UNIT, RecurrenceUnit.DAYS.name)
            putExtra(EXTRA_RECURRENCE_INTERVAL, 2)
        }
        
        every { mockScheduler.nextOccurrence(dueDate, any()) } returns nextDueDate

        receiver.performReceive(intent)

        verify { mockNotificationHelper.showNotification(taskId, title, "") }
        verify { mockScheduler.nextOccurrence(dueDate, match { it.unit == RecurrenceUnit.DAYS && it.interval == 2 }) }
        verify { 
            mockScheduler.scheduleFromReceiver(
                taskId = taskId,
                title = title,
                notes = "",
                dueDate = nextDueDate,
                hasTime = false,
                recurrenceRule = match { it.unit == RecurrenceUnit.DAYS && it.interval == 2 }
            ) 
        }
        verify { mockLogger.d(TAG, "Rescheduled recurring task 1 for 2000") }
    }

    @Test
    fun `given recurring task with invalid recurrence unit, when performReceive called, then defaults to NONE and does not reschedule`() {
        val taskId = 1L
        val title = "Task"
        val dueDate = 1000L
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_DUE_DATE, dueDate)
            putExtra(EXTRA_RECURRENCE_UNIT, "INVALID_UNIT")
        }

        receiver.performReceive(intent)

        verify { mockNotificationHelper.showNotification(taskId, title, "") }
        verify(exactly = 0) { mockScheduler.nextOccurrence(any(), any()) }
        verify(exactly = 0) { mockScheduler.scheduleFromReceiver(any(), any(), any(), any(), any(), any()) }
    }
}
