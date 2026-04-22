package com.gustavo.brilhante.data

import com.gustavo.brilhante.data.repository.TaskRepositoryImpl
import com.gustavo.brilhante.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
}
