package com.gustavo.brilhante.tasklist.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.tasklist.presentation.TaskListViewModel
import com.gustavo.brilhante.tasklist.ui.TaskListScreen

fun EntryProviderScope<NavKey>.taskListEntries(
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit
) {
    entry<TaskListRoute> {
        val viewModel: TaskListViewModel = hiltViewModel()
        TaskListScreen(
            onAddTask = onAddTask,
            onEditTask = onEditTask,
            viewModel = viewModel
        )
    }
}
