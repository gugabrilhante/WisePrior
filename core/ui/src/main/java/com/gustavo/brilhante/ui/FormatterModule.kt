package com.gustavo.brilhante.ui

import com.gustavo.brilhante.domain.time.CalendarProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FormatterModule {

    @Provides
    @Singleton
    fun provideDateFormatter(calendarProvider: CalendarProvider): DateFormatter = 
        DateFormatterImpl(calendarProvider)
}
