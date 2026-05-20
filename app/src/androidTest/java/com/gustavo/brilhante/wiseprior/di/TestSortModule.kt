package com.gustavo.brilhante.wiseprior.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.gustavo.brilhante.tasklist.data.SortPreferences
import com.gustavo.brilhante.tasklist.data.SortPreferencesDataStore
import com.gustavo.brilhante.tasklist.di.SortModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.util.UUID
import javax.inject.Singleton

/**
 * Replaces [SortModule] in tests with a DataStore backed by a unique file per test run.
 * DataStore forbids multiple instances with the same name in one process, so tests that
 * share the process would fail with IllegalStateException if the production module were used.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [SortModule::class]
)
object TestSortModule {

    @Provides
    @Singleton
    fun provideSortDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile("sort_preferences_test_${UUID.randomUUID()}")
            }
        )

    @Provides
    @Singleton
    fun provideSortPreferences(dataStore: DataStore<Preferences>): SortPreferences =
        SortPreferencesDataStore(dataStore)
}
