package com.gustavo.brilhante.tasklist.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.gustavo.brilhante.tasklist.data.SortPreferences
import com.gustavo.brilhante.tasklist.data.SortPreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SortModule {

    @Provides
    @Singleton
    fun provideSortPreferences(dataStore: DataStore<Preferences>): SortPreferences =
        SortPreferencesDataStore(dataStore)

    @Provides
    @Singleton
    fun provideSortDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("sort_preferences") }
        )
}
