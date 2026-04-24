package com.gustavo.brilhante.tasklist.presentation

import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.tasklist.model.TaskCollection

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val selectedCollection: TaskCollection = TaskCollection.All,
    val collectionCounts: CollectionCounts = CollectionCounts(),
    val tags: List<Tag> = emptyList(),
    val tagCounts: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Tag editor sheet state
    val showTagEditor: Boolean = false,
    val editingTag: Tag? = null
)

data class CollectionCounts(
    val all: Int = 0,
    val today: Int = 0,
    val scheduled: Int = 0,
    val flagged: Int = 0,
    val completed: Int = 0
) {
    fun forCollection(collection: TaskCollection): Int = when (collection) {
        TaskCollection.All -> all
        TaskCollection.Today -> today
        TaskCollection.Scheduled -> scheduled
        TaskCollection.Flagged -> flagged
        TaskCollection.Completed -> completed
        is TaskCollection.ByTag -> 0 // tag counts live in TaskListUiState.tagCounts
    }
}

sealed interface TaskListEvent {
    data class DeleteTask(val task: Task) : TaskListEvent
    data class EditTask(val task: Task) : TaskListEvent
    data object AddTask : TaskListEvent
}
