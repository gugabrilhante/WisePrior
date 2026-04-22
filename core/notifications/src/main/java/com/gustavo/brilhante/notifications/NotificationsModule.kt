package com.gustavo.brilhante.notifications

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NotificationsModule {

    @Binds
    @Singleton
    fun bindNotificationScheduler(
        impl: AlarmManagerNotificationScheduler
    ): NotificationScheduler
}
