package com.gustavo.brilhante.wiseprior.di

import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.notifications.NotificationsModule
import dagger.Binds
import dagger.Module
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
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NotificationsModule::class]
)
interface TestNotificationsModule {
    @Binds
    @Singleton
    fun bindFakeScheduler(fake: FakeNotificationScheduler): NotificationScheduler
}
