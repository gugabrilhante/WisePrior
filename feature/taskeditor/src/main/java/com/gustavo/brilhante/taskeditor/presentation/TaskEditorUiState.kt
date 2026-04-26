package com.gustavo.brilhante.taskeditor.presentation

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceType
import com.gustavo.brilhante.model.Tag

data class TaskEditorUiState(
    val title: String = "",
    val notes: String = "",
    val url: String = "",
    val hasDate: Boolean = false,
    val hasTime: Boolean = false,
    val dueDate: Long = System.currentTimeMillis(),
    val formattedDate: String? = null,
    val formattedTime: String? = null,
    val isUrgent: Boolean = false,
    val priority: Priority = Priority.NONE,
    val selectedTagIds: Set<Long> = emptySet(),
    val availableTags: List<Tag> = emptyList(),
    val isFlagged: Boolean = false,
    val isCompleted: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val titleError: String? = null,
    val isLoading: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false
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
    data class DueDateChanged(val dateMillis: Long) : TaskEditorEvent
    data class TimeChanged(val hour: Int, val minute: Int) : TaskEditorEvent
    data class RecurrenceChanged(val recurrenceType: RecurrenceType) : TaskEditorEvent
    data object ShowDatePicker : TaskEditorEvent
    data object HideDatePicker : TaskEditorEvent
    data object ShowTimePicker : TaskEditorEvent
    data object HideTimePicker : TaskEditorEvent
    data object Save : TaskEditorEvent
}
