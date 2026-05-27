package com.gustavo.brilhante.taskeditor.presentation

import com.gustavo.brilhante.model.ChecklistItem
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.ui.UiText

data class TaskEditorUiState(
    val title: String = "",
    val notes: String = "",
    val url: String = "",
    val dueDate: Long = 0L,
    val isUrgent: Boolean = false,
    val priority: Priority = Priority.NONE,
    val isFlagged: Boolean = false,
    val isCompleted: Boolean = false,
    val recurrenceRule: RecurrenceRule = RecurrenceRule.NONE,
    val titleError: UiText? = null,
    val isLoading: Boolean = false,
    val screenTitle: UiText = UiText.DynamicString(""),
    val priorityOptions: List<PriorityOptionUiModel> = emptyList(),
    val dialogState: TaskEditorDialogState = TaskEditorDialogState(),
    val dateSection: DateSectionUiModel = DateSectionUiModel(),
    val tagSection: TagSectionUiModel = TagSectionUiModel(),
    val checklistItems: List<ChecklistItemUiModel> = emptyList(),
    val titlePlaceholder: UiText = UiText.DynamicString(""),
    val notesPlaceholder: UiText = UiText.DynamicString(""),
    val urlPlaceholder: UiText = UiText.DynamicString(""),
    val urgentLabel: UiText = UiText.DynamicString(""),
    val flagLabel: UiText = UiText.DynamicString(""),
    val backContentDescription: UiText = UiText.DynamicString(""),
    val doneLabel: UiText = UiText.DynamicString(""),
    val checklistSectionLabel: UiText = UiText.DynamicString(""),
    val datetimeSectionLabel: UiText = UiText.DynamicString(""),
    val detailsSectionLabel: UiText = UiText.DynamicString(""),
    val prioritySectionLabel: UiText = UiText.DynamicString(""),
    val tagsSectionLabel: UiText = UiText.DynamicString(""),
    val urlSectionLabel: UiText = UiText.DynamicString(""),
    val addChecklistItemLabel: UiText = UiText.DynamicString("")
)

fun TaskEditorUiState.toTask(id: Long, createdAt: Long, tagIds: List<Long>): Task {
    return Task(
        id = id,
        title = title.trim(),
        notes = notes.trim(),
        url = url.trim(),
        dueDate = if (dateSection.hasDate) dueDate else null,
        hasTime = dateSection.hasTime,
        isUrgent = isUrgent,
        priority = priority,
        tagIds = tagIds,
        isFlagged = isFlagged,
        isCompleted = isCompleted,
        recurrenceRule = recurrenceRule,
        createdAt = createdAt,
        checklistItems = checklistItems
            .filter { it.text.isNotBlank() }
            .map { ChecklistItem(id = it.id, text = it.text.trim(), isChecked = it.isChecked) }
    )
}

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
    data object ToggleRecurrence : TaskEditorEvent
    data object IncrementInterval : TaskEditorEvent
    data object DecrementInterval : TaskEditorEvent
    data class RecurrenceUnitSelected(val unit: RecurrenceUnit) : TaskEditorEvent
    data class TagClicked(val tagId: Long) : TaskEditorEvent
    data class ShowDialog(val dialog: ActiveDialog) : TaskEditorEvent
    data object DismissDialog : TaskEditorEvent
    data object Save : TaskEditorEvent
    data object AddChecklistItem : TaskEditorEvent
    data class RemoveChecklistItem(val index: Int) : TaskEditorEvent
    data class ChecklistItemTextChanged(val index: Int, val text: String) : TaskEditorEvent
    data class ChecklistItemChecked(val index: Int, val isChecked: Boolean) : TaskEditorEvent
}
