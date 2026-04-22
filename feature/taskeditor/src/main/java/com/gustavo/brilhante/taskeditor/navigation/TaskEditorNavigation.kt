package com.gustavo.brilhante.taskeditor.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gustavo.brilhante.taskeditor.presentation.TaskEditorViewModel
import com.gustavo.brilhante.taskeditor.ui.TaskEditorScreen

fun EntryProviderScope<NavKey>.taskEditorEntries(
    onBack: () -> Unit
) {
    entry<TaskEditorRoute> { route ->
        val viewModel: TaskEditorViewModel = hiltViewModel()
        TaskEditorScreen(
            taskId = route.taskId.takeIf { it > 0L },
            onBack = onBack,
            viewModel = viewModel
        )
    }
}
