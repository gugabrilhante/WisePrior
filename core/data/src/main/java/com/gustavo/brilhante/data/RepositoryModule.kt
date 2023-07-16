package com.gustavo.brilhante.data

import com.gustavo.brilhante.data.repository.TaskRepository
import com.gustavo.brilhante.data.repository.TaskRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindTaskManagerRepository(repository: TaskRepositoryImpl): TaskRepository
}