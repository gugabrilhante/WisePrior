package com.gustavo.brilhante.notifications

import android.app.AlarmManager
import android.content.Context
import com.gustavo.brilhante.model.Task
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class AlarmManagerNotificationSchedulerTest {

    private lateinit var context: Context
    private lateinit var scheduler: AlarmManagerNotificationScheduler

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        scheduler = AlarmManagerNotificationScheduler(context)
    }

    @Test
    fun `given task with due date, when schedule called, then sets exact alarm`() {
        val futureTime = System.currentTimeMillis() + 100000
        val task = Task(id = 1, title = "Test Task", dueDate = futureTime)

        scheduler.schedule(task)

        val shadowAlarmManager = shadowOf(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
        val nextAlarm = shadowAlarmManager.nextScheduledAlarm
        assert(nextAlarm != null)
        assert(nextAlarm?.triggerAtTime == futureTime)
    }

    @Test
    fun `given task with past due date and no recurrence, when schedule called, then skips`() {
        val pastTime = System.currentTimeMillis() - 100000
        val task = Task(id = 1, title = "Test Task", dueDate = pastTime)

        scheduler.schedule(task)

        val shadowAlarmManager = shadowOf(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
        assert(shadowAlarmManager.nextScheduledAlarm == null)
    }

    @Test
    fun `given taskId, when cancel called, then cancels alarm`() {
        val taskId = 1L
        val task = Task(id = taskId, title = "Test", dueDate = System.currentTimeMillis() + 10000)
        scheduler.schedule(task)
        
        scheduler.cancel(taskId)

        val shadowAlarmManager = shadowOf(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
        assert(shadowAlarmManager.nextScheduledAlarm == null)
    }
}
