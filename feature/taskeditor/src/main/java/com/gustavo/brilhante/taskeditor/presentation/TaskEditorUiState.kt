package com.gustavo.brilhante.taskeditor.presentation

import com.gustavo.brilhante.model.Priority

data class TaskEditorUiState(
    val title: String = "",
    val notes: String = "",
    val url: String = "",
    val hasDate: Boolean = false,
    val hasTime: Boolean = false,
    val dueDate: Long = System.currentTimeMillis(),
    val isUrgent: Boolean = false,
    val priority: Priority = Priority.NONE,
    val tags: List<String> = emptyList(),
    val isFlagged: Boolean = false,
    val titleError: String? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface TaskEditorEvent {
    data class TitleChanged(val title: String) : TaskEditorEvent
    data class NotesChanged(val notes: String) : TaskEditorEvent
    data class UrlChanged(val url: String) : TaskEditorEvent
    data object ToggleDate : TaskEditorEvent
    data object ToggleTime : TaskEditorEvent
    data object ToggleUrgent : TaskEditorEvent
    data object ToggleFlagged : TaskEditorEvent
    data class PriorityChanged(val priority: Priority) : TaskEditorEvent
    data class DueDateChanged(val date: Long) : TaskEditorEvent
    data class TagAdded(val tag: String) : TaskEditorEvent
    data class TagRemoved(val tag: String) : TaskEditorEvent
    data object Save : TaskEditorEvent
}
