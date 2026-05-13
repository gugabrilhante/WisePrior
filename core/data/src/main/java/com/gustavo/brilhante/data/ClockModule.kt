package com.gustavo.brilhante.data

import com.gustavo.brilhante.data.logging.AndroidLogger
import com.gustavo.brilhante.data.system.AndroidVersionProviderImpl
import com.gustavo.brilhante.data.system.TestEnvironmentProviderImpl
import com.gustavo.brilhante.data.time.SystemCalendarProvider
import com.gustavo.brilhante.data.time.SystemClockProvider
import com.gustavo.brilhante.domain.logging.Logger
import com.gustavo.brilhante.domain.system.AndroidVersionProvider
import com.gustavo.brilhante.domain.system.TestEnvironmentProvider
import com.gustavo.brilhante.domain.time.CalendarProvider
import com.gustavo.brilhante.domain.time.ClockProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ProviderModule {

    @Binds
    @Singleton
    fun bindClockProvider(impl: SystemClockProvider): ClockProvider

    @Binds
    @Singleton
    fun bindCalendarProvider(impl: SystemCalendarProvider): CalendarProvider

    @Binds
    @Singleton
    fun bindAndroidVersionProvider(impl: AndroidVersionProviderImpl): AndroidVersionProvider

    @Binds
    @Singleton
    fun bindTestEnvironmentProvider(impl: TestEnvironmentProviderImpl): TestEnvironmentProvider

    @Binds
    @Singleton
    fun bindLogger(impl: AndroidLogger): Logger
}
