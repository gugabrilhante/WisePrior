package com.gustavo.brilhante.taskeditor.presentation

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.ui.UiText

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
    val recurrenceRule: RecurrenceRule = RecurrenceRule.NONE,
    val titleError: String? = null,
    val isLoading: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val datePickerUtcMillis: Long = 0L,
    val timePickerHour: Int = 0,
    val timePickerMinute: Int = 0,
    val screenTitle: UiText = UiText.DynamicString(""),
    val priorityOptions: List<PriorityOptionUiModel> = emptyList(),
    val recurrenceUnitOptions: List<RecurrenceUnitOptionUiModel> = emptyList(),
    val canDecrementInterval: Boolean = false,
)

data class PriorityOptionUiModel(
    val priority: Priority,
    val label: UiText,
    val isSelected: Boolean,
    val testTag: String
)

data class RecurrenceUnitOptionUiModel(
    val unit: RecurrenceUnit,
    val label: UiText
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
    data class RecurrenceChanged(val rule: RecurrenceRule) : TaskEditorEvent
    data object ToggleRecurrence : TaskEditorEvent
    data object IncrementInterval : TaskEditorEvent
    data object DecrementInterval : TaskEditorEvent
    data class RecurrenceUnitSelected(val unit: RecurrenceUnit) : TaskEditorEvent
    data class TagClicked(val tagId: Long) : TaskEditorEvent
    data object ShowDatePicker : TaskEditorEvent
    data object HideDatePicker : TaskEditorEvent
    data object ShowTimePicker : TaskEditorEvent
    data object HideTimePicker : TaskEditorEvent
    data object Save : TaskEditorEvent
}
