package com.gustavo.brilhante.data

import com.gustavo.brilhante.data.time.SystemClockProvider
import com.gustavo.brilhante.domain.time.ClockProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ClockModule {

    @Binds
    @Singleton
    fun bindClockProvider(impl: SystemClockProvider): ClockProvider
}
