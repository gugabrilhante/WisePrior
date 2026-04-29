package com.gustavo.brilhante.wiseprior.di

import android.content.Context
import androidx.room.Room
import com.gustavo.brilhante.storage.DatabaseModule
import com.gustavo.brilhante.storage.dao.TagDao
import com.gustavo.brilhante.storage.dao.TaskDao
import com.gustavo.brilhante.storage.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Replaces [DatabaseModule] in tests with an in-memory Room database.
 * Each test process gets a fresh, isolated database — no persistence between runs.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideInMemoryDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()
}
