package com.gustavo.brilhante.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.gustavo.brilhante.domain.logging.Logger
import com.gustavo.brilhante.domain.system.AndroidVersionProvider
import com.gustavo.brilhante.domain.time.CalendarProvider
import com.gustavo.brilhante.domain.time.ClockProvider
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import io.mockk.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class AlarmManagerNotificationSchedulerTest {

    private val context = mockk<Context>(relaxed = true)
    private val alarmManager = mockk<AlarmManager>(relaxed = true)
    private val clockProvider = mockk<ClockProvider>()
    private val calendarProvider = mockk<CalendarProvider>()
    private val versionProvider = mockk<AndroidVersionProvider>()
    private val logger = mockk<Logger>(relaxed = true)
    private val intentFactory = mockk<AlarmIntentFactory>()
    private val pendingIntent = mockk<PendingIntent>(relaxed = true)

    private lateinit var scheduler: AlarmManagerNotificationScheduler

    @Before
    fun setup() {
        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        every { intentFactory.createPendingIntent(any(), any(), any(), any(), any(), any()) } returns pendingIntent
        every { intentFactory.getExistingPendingIntent(any()) } returns pendingIntent
        
        scheduler = AlarmManagerNotificationScheduler(
            context,
            clockProvider,
            calendarProvider,
            versionProvider,
            logger,
            intentFactory
        )
    }

    @Test
    fun `schedule given task with due date in future, schedules exact alarm`() {
        val now = 1000L
        val futureTime = 2000L
        val task = Task(id = 1, title = "Test", dueDate = futureTime, createdAt = now)
        
        every { clockProvider.currentTimeMillis() } returns now
        every { versionProvider.sdkInt } returns Build.VERSION_CODES.S
        every { alarmManager.canScheduleExactAlarms() } returns true

        scheduler.schedule(task)

        verify {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, futureTime, pendingIntent)
        }
    }

    @Test
    fun `schedule given task with due date in past and no recurrence, skips`() {
        val now = 2000L
        val pastTime = 1000L
        val task = Task(id = 1, title = "Test", dueDate = pastTime, createdAt = now)
        
        every { clockProvider.currentTimeMillis() } returns now

        scheduler.schedule(task)

        verify(exactly = 0) { alarmManager.setExactAndAllowWhileIdle(any(), any(), any()) }
        verify { logger.d(any(), match { it.contains("Skipping past") }) }
    }

    @Test
    fun `schedule given task with due date in past and recurrence, advances to future`() {
        val now = 2500L
        val pastTime = 1000L // + 1000 = 2000 (still past), + 1000 = 3000 (future)
        val task = Task(
            id = 1, 
            title = "Test", 
            dueDate = pastTime, 
            recurrenceRule = RecurrenceRule(RecurrenceUnit.DAYS, 1),
            createdAt = now
        )
        
        every { clockProvider.currentTimeMillis() } returns now
        every { versionProvider.sdkInt } returns Build.VERSION_CODES.R
        
        val cal = mockk<Calendar>(relaxed = true)
        every { calendarProvider.getInstance() } returns cal
        // first call to nextOccurrence: from 1000 -> 2000
        // second call: from 2000 -> 3000
        var currentTime = pastTime
        every { cal.timeInMillis } answers { currentTime }
        every { cal.add(Calendar.DAY_OF_YEAR, 1) } answers { currentTime += 1000 }

        scheduler.schedule(task)

        verify {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 3000L, pendingIntent)
        }
    }

    @Test
    fun `cancel given taskId, cancels pending intent and alarm`() {
        val taskId = 1L
        
        scheduler.cancel(taskId)

        verify {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    @Test
    fun `rescheduleAll given tasks, schedules only future or recurring tasks`() {
        val now = 2000L
        val futureTask = Task(id = 1, title = "Future", dueDate = 3000L, createdAt = now)
        val pastTask = Task(id = 2, title = "Past", dueDate = 1000L, createdAt = now)
        val recurringPastTask = Task(id = 3, title = "Recurring", dueDate = 1000L, recurrenceRule = RecurrenceRule(RecurrenceUnit.HOURS, 1), createdAt = now)
        
        every { clockProvider.currentTimeMillis() } returns now
        every { versionProvider.sdkInt } returns Build.VERSION_CODES.R
        
        // Mocking nextOccurrence for recurringPastTask
        val cal = mockk<Calendar>(relaxed = true)
        every { calendarProvider.getInstance() } returns cal
        var currentTime = 1000L
        every { cal.timeInMillis } answers { currentTime }
        every { cal.add(Calendar.HOUR_OF_DAY, 1) } answers { currentTime += 3600000 }

        scheduler.rescheduleAll(listOf(futureTask, pastTask, recurringPastTask))

        verify(exactly = 2) { alarmManager.setExactAndAllowWhileIdle(any(), any(), any()) }
    }

    @Test
    fun `scheduleExact uses fallback when canScheduleExactAlarms is false`() {
        val now = 1000L
        val futureTime = 2000L
        val task = Task(id = 1, title = "Test", dueDate = futureTime, createdAt = now)
        
        every { clockProvider.currentTimeMillis() } returns now
        every { versionProvider.sdkInt } returns Build.VERSION_CODES.S
        every { alarmManager.canScheduleExactAlarms() } returns false

        scheduler.schedule(task)

        verify {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, futureTime, pendingIntent)
        }
        verify { logger.w(any(), match { it.contains("not granted") }) }
    }
}
