package com.gustavo.brilhante.wiseprior.di

import android.content.Context
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.AlarmIntentBuilder
import com.gustavo.brilhante.notifications.AlarmIntentBuilderImpl
import com.gustavo.brilhante.notifications.AlarmIntentFactory
import com.gustavo.brilhante.notifications.AlarmIntentFactoryImpl
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.notifications.NotificationsModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * No-op [NotificationScheduler] for instrumented tests.
 * Prevents real AlarmManager calls from interfering with test assertions.
 */
class FakeNotificationScheduler @Inject constructor() : NotificationScheduler {
    override fun schedule(task: Task) = Unit
    override fun cancel(taskId: Long) = Unit
    override fun rescheduleAll(tasks: List<Task>) = Unit
    override fun nextOccurrence(from: Long, rule: RecurrenceRule): Long = from
    override fun scheduleFromReceiver(
        taskId: Long,
        title: String,
        notes: String,
        dueDate: Long,
        hasTime: Boolean,
        recurrenceRule: RecurrenceRule
    ) = Unit
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NotificationsModule::class]
)
object TestNotificationsModule {

    @Provides
    @Singleton
    fun provideNotificationScheduler(fake: FakeNotificationScheduler): NotificationScheduler = fake

    @Provides
    @Singleton
    fun provideAlarmIntentBuilder(@ApplicationContext context: Context): AlarmIntentBuilder =
        AlarmIntentBuilderImpl(context)

    @Provides
    @Singleton
    fun provideAlarmIntentFactory(
        @ApplicationContext context: Context,
        intentBuilder: AlarmIntentBuilder
    ): AlarmIntentFactory =
        AlarmIntentFactoryImpl(context, intentBuilder)
}
