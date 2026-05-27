package com.gustavo.brilhante.tasklist.data

import com.gustavo.brilhante.model.TaskSortOption
import kotlinx.coroutines.flow.Flow

interface SortPreferences {
    val sortOption: Flow<TaskSortOption>
    suspend fun setSortOption(option: TaskSortOption)
}
