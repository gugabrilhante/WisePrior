package com.gustavo.brilhante.tasklist.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.gustavo.brilhante.tasklist.data.SortPreferences
import com.gustavo.brilhante.tasklist.data.SortPreferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SortModule {

    @Binds
    @Singleton
    abstract fun bindSortPreferences(impl: SortPreferencesDataStore): SortPreferences

    companion object {
        @Provides
        @Singleton
        fun provideSortDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                produceFile = { context.preferencesDataStoreFile("sort_preferences") }
            )
    }
}
