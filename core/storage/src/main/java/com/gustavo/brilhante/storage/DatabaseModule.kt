package com.gustavo.brilhante.storage

import android.content.Context
import androidx.room.Room
import com.gustavo.brilhante.storage.dao.TagDao
import com.gustavo.brilhante.storage.dao.TaskDao
import com.gustavo.brilhante.storage.database.AppDatabase
import com.gustavo.brilhante.storage.database.AppDatabase.Companion.MIGRATION_3_4
import com.gustavo.brilhante.storage.database.AppDatabase.Companion.MIGRATION_4_5
import com.gustavo.brilhante.storage.database.AppDatabase.Companion.MIGRATION_5_6
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.databaseName)
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .build()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()
}
