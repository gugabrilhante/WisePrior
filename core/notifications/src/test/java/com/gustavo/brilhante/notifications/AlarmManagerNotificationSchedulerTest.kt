package com.gustavo.brilhante.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.gustavo.brilhante.domain.logging.Logger
import com.gustavo.brilhante.domain.system.AndroidVersionProvider
import com.gustavo.brilhante.domain.time.CalendarProvider
import com.gustavo.brilhante.domain.time.ClockProvider
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class AlarmManagerNotificationSchedulerTest {

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager
    private val clockProvider = mockk<ClockProvider>()
    private val calendarProvider = mockk<CalendarProvider>()
    private val versionProvider = mockk<AndroidVersionProvider>()
    private val logger = mockk<Logger>(relaxed = true)
    private val intentFactory = mockk<AlarmIntentFactory>()
    private val pendingIntent = mockk<PendingIntent>(relaxed = true)
    private val showDetailsIntent = mockk<PendingIntent>(relaxed = true)

    private lateinit var scheduler: AlarmManagerNotificationScheduler

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Return different mocks based on task id to avoid overwriting in ShadowAlarmManager
        every { intentFactory.createPendingIntent(any(), any(), any(), any(), any(), any()) } answers {
            mockk<PendingIntent>(relaxed = true)
        }
        every { intentFactory.getExistingPendingIntent(any()) } returns pendingIntent
        every { intentFactory.createShowDetailsPendingIntent(any()) } returns showDetailsIntent
        
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
    fun `schedule given task with due date in future, schedules exact alarm clock`() {
        val now = 1000L
        val futureTime = 2000L
        val task = Task(id = 1, title = "Test", dueDate = futureTime, createdAt = now)
        
        every { clockProvider.currentTimeMillis() } returns now
        every { versionProvider.sdkInt } returns Build.VERSION_CODES.S
        
        scheduler.schedule(task)

        val shadow = shadowOf(alarmManager)
        val nextAlarm = shadow.nextScheduledAlarm
        assert(nextAlarm != null)
        assertEquals(futureTime, nextAlarm?.triggerAtTime)
    }

    @Test
    fun `schedule given task with due date in past and no recurrence, skips`() {
        val now = 2000L
        val pastTime = 1000L
        val task = Task(id = 1, title = "Test", dueDate = pastTime, createdAt = now)
        
        every { clockProvider.currentTimeMillis() } returns now

        scheduler.schedule(task)

        val shadow = shadowOf(alarmManager)
        assert(shadow.nextScheduledAlarm == null)
        verify { logger.d(any(), match { it.contains("Skipping past") }) }
    }

    @Test
    fun `schedule given task with due date in past and recurrence, advances to future`() {
        val now = 2500L
        val pastTime = 1000L 
        val task = Task(
            id = 1, 
            title = "Test", 
            dueDate = pastTime, 
            recurrenceRule = RecurrenceRule(RecurrenceUnit.DAYS, 1),
            createdAt = now
        )
        
        every { clockProvider.currentTimeMillis() } returns now
        every { versionProvider.sdkInt } returns Build.VERSION_CODES.R
        
        val mockCal = mockk<Calendar>(relaxed = true)
        every { calendarProvider.getInstance() } returns mockCal
        var currentTime = pastTime
        every { mockCal.timeInMillis } answers { currentTime }
        every { mockCal.add(Calendar.DAY_OF_YEAR, 1) } answers { currentTime += 1000 }

        scheduler.schedule(task)

        val shadow = shadowOf(alarmManager)
        assertEquals(3000L, shadow.nextScheduledAlarm?.triggerAtTime)
    }

    @Test
    fun `cancel given taskId, cancels pending intent and alarm`() {
        val taskId = 1L
        val pi = mockk<PendingIntent>(relaxed = true)
        every { intentFactory.getExistingPendingIntent(taskId) } returns pi
        
        scheduler.cancel(taskId)

        verify {
            pi.cancel()
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
        
        val mockCal = mockk<Calendar>(relaxed = true)
        every { calendarProvider.getInstance() } returns mockCal
        var currentTime = 1000L
        every { mockCal.timeInMillis } answers { currentTime }
        every { mockCal.add(Calendar.HOUR_OF_DAY, 1) } answers { currentTime += 3600000 }

        scheduler.rescheduleAll(listOf(futureTask, pastTask, recurringPastTask))

        val shadow = shadowOf(alarmManager)
        assertEquals(2, shadow.scheduledAlarms.size)
    }

    @Test
    fun `scheduleFromReceiver advances to future for late recurring alarms`() {
        val now = 5000L
        val lateDueDate = 1000L // 4 hours late
        val rule = RecurrenceRule(RecurrenceUnit.HOURS, 1)
        
        every { clockProvider.currentTimeMillis() } returns now
        every { versionProvider.sdkInt } returns Build.VERSION_CODES.R
        
        val mockCal = mockk<Calendar>(relaxed = true)
        every { calendarProvider.getInstance() } returns mockCal
        var currentTime = lateDueDate
        every { mockCal.timeInMillis } answers { currentTime }
        every { mockCal.add(Calendar.HOUR_OF_DAY, 1) } answers { currentTime += 1000 }

        scheduler.scheduleFromReceiver(1L, "Title", "Notes", lateDueDate, true, rule)

        val shadow = shadowOf(alarmManager)
        // It should advance until > 5000. 
        // 1000+1000=2000, +1000=3000, +1000=4000, +1000=5000 (still <= now), +1000=6000
        assertEquals(6000L, shadow.nextScheduledAlarm?.triggerAtTime)
    }
}
