package com.gustavo.brilhante.tasklist.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gustavo.brilhante.model.TaskSortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val SORT_OPTION_KEY = stringPreferencesKey("sort_option")

@Singleton
class SortPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SortPreferences {
    override val sortOption: Flow<TaskSortOption> = dataStore.data.map { prefs ->
        prefs[SORT_OPTION_KEY]
            ?.let { runCatching { TaskSortOption.valueOf(it) }.getOrNull() }
            ?: TaskSortOption.SMART_PRIORITY
    }

    override suspend fun setSortOption(option: TaskSortOption) {
        dataStore.edit { it[SORT_OPTION_KEY] = option.name }
    }
}
