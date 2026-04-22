package com.gustavo.brilhante.wiseprior.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.gustavo.brilhante.taskeditor.navigation.TaskEditorRoute
import com.gustavo.brilhante.taskeditor.navigation.taskEditorEntries
import com.gustavo.brilhante.tasklist.navigation.TaskListRoute
import com.gustavo.brilhante.tasklist.navigation.taskListEntries

@Composable
fun WisePriorNavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(TaskListRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = entryProvider {
            taskListEntries(
                onAddTask = { backStack.add(TaskEditorRoute()) },
                onEditTask = { task -> backStack.add(TaskEditorRoute(taskId = task.id)) }
            )
            taskEditorEntries(
                onBack = { backStack.removeLastOrNull() }
            )
        }
    )
}
