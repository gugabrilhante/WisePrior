package com.gustavo.brilhante.common

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
    fun provideDateFormatter(): DateFormatter = DateFormatterImpl()
}
