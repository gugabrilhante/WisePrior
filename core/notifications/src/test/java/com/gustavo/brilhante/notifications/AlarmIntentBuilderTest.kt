package com.gustavo.brilhante.notifications

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class AlarmIntentBuilderTest {

    private lateinit var context: Context
    private lateinit var builder: AlarmIntentBuilderImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        builder = AlarmIntentBuilderImpl(context)
    }

    @Test
    fun `buildAlarmIntent should set all extras correctly`() {
        val taskId = 123L
        val title = "Task Title"
        val notes = "Task Notes"
        val dueDate = 1625097600000L
        val hasTime = true
        val recurrenceRule = RecurrenceRule(RecurrenceUnit.DAYS, 1)

        val intent = builder.buildAlarmIntent(taskId, title, notes, dueDate, hasTime, recurrenceRule)

        assertEquals(AlarmReceiver::class.java.name, intent.component?.className)
        assertEquals(taskId, intent.getLongExtra(EXTRA_TASK_ID, -1L))
        assertEquals(title, intent.getStringExtra(EXTRA_TASK_TITLE))
        assertEquals(notes, intent.getStringExtra(EXTRA_TASK_NOTES))
        assertEquals(dueDate, intent.getLongExtra(EXTRA_DUE_DATE, -1L))
        assertEquals(hasTime, intent.getBooleanExtra(EXTRA_HAS_TIME, false))
        assertEquals(recurrenceRule.unit.name, intent.getStringExtra(EXTRA_RECURRENCE_UNIT))
        assertEquals(recurrenceRule.interval, intent.getIntExtra(EXTRA_RECURRENCE_INTERVAL, -1))
    }

    @Test
    fun `buildBaseAlarmIntent should return intent with AlarmReceiver component`() {
        val intent = builder.buildBaseAlarmIntent()

        assertEquals(AlarmReceiver::class.java.name, intent.component?.className)
    }

    @Test
    fun `buildShowDetailsIntent when launch intent exists should return intent with launch activity and extras`() {
        val taskId = 456L
        val spyContext = spyk(context)
        val mockPackageManager = mockk<PackageManager>()
        val launchIntent = Intent("LAUNCH")
        
        every { spyContext.packageManager } returns mockPackageManager
        every { spyContext.packageName } returns "com.example.app"
        every { mockPackageManager.getLaunchIntentForPackage("com.example.app") } returns launchIntent
        
        val builderWithSpy = AlarmIntentBuilderImpl(spyContext)
        val intent = builderWithSpy.buildShowDetailsIntent(taskId)

        assertEquals(taskId, intent.getLongExtra(EXTRA_TASK_ID, -1L))
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP, intent.flags)
    }

    @Test
    fun `buildShowDetailsIntent when launch intent does not exist should return empty intent`() {
        val taskId = 456L
        val spyContext = spyk(context)
        val mockPackageManager = mockk<PackageManager>()
        
        every { spyContext.packageManager } returns mockPackageManager
        every { spyContext.packageName } returns "com.example.app"
        every { mockPackageManager.getLaunchIntentForPackage("com.example.app") } returns null
        
        val builderWithSpy = AlarmIntentBuilderImpl(spyContext)
        val intent = builderWithSpy.buildShowDetailsIntent(taskId)

        assertEquals(-1L, intent.getLongExtra(EXTRA_TASK_ID, -1L))
        assertEquals(0, intent.flags)
    }
}
