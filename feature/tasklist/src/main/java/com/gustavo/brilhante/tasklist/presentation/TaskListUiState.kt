package com.gustavo.brilhante.tasklist.presentation

import com.gustavo.brilhante.model.Task

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface TaskListEvent {
    data class DeleteTask(val task: Task) : TaskListEvent
    data class EditTask(val task: Task) : TaskListEvent
    data object AddTask : TaskListEvent
}
