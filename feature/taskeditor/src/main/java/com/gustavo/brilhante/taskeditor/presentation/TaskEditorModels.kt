package com.gustavo.brilhante.taskeditor.presentation

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.ui.UiText
import com.gustavo.brilhante.taskeditor.R

enum class TaskEditorMode {
    NEW, EDIT
}

object TaskEditorArgsResolver {
    fun resolveMode(taskId: Long?): TaskEditorMode = if (taskId != null && taskId > 0) TaskEditorMode.EDIT else TaskEditorMode.NEW
    fun resolveId(taskId: Long?): Long = taskId ?: -1L
}

sealed interface ActiveDialog {
    data object DatePicker : ActiveDialog
    data object TimePicker : ActiveDialog
}

data class TaskEditorDialogState(
    val activeDialog: ActiveDialog? = null,
    val datePickerUtcMillis: Long = 0L,
    val timePickerHour: Int = 0,
    val timePickerMinute: Int = 0,
    val okLabel: UiText = UiText.DynamicString(""),
    val cancelLabel: UiText = UiText.DynamicString("")
)

data class DateSectionUiModel(
    val hasDate: Boolean = false,
    val dateLabel: UiText = UiText.DynamicString(""),
    val formattedDate: String? = null,
    val showTimeToggle: Boolean = false,
    val hasTime: Boolean = false,
    val timeLabel: UiText = UiText.DynamicString(""),
    val formattedTime: String? = null,
    val showRecurrence: Boolean = false,
    val recurrenceUiModel: RecurrenceUiModel = RecurrenceUiModel()
)

data class TagItemUiModel(
    val id: Long,
    val name: String,
    val color: Long,
    val isSelected: Boolean
)

data class ChecklistItemUiModel(
    val id: Long = 0,
    val text: String = "",
    val isChecked: Boolean = false,
    val checkContentDescription: UiText = UiText.DynamicString(""),
    val showDivider: Boolean = false,
    val placeholder: UiText = UiText.DynamicString(""),
    val deleteContentDescription: UiText = UiText.DynamicString(""),
    val isStrikethrough: Boolean = false,
    val isPrimaryTint: Boolean = false
)

data class TagSectionUiModel(
    val tags: List<TagItemUiModel> = emptyList(),
    val showEmptyState: Boolean = false,
    val emptyStateTitle: UiText = UiText.DynamicString(""),
    val emptyStateSubtitle: UiText = UiText.DynamicString("")
)

data class RecurrenceUiModel(
    val isRecurring: Boolean = false,
    val interval: Int = 1,
    val unit: RecurrenceUnit = RecurrenceUnit.DAYS,
    val intervalLabel: String = "1",
    val canDecrement: Boolean = false,
    val selectedUnitLabel: UiText = UiText.DynamicString(""),
    val unitOptions: List<RecurrenceUnitOptionUiModel> = emptyList(),
    val repeatLabel: UiText = UiText.DynamicString(""),
    val everyLabel: UiText = UiText.DynamicString(""),
    val decreaseContentDescription: UiText = UiText.DynamicString(""),
    val increaseContentDescription: UiText = UiText.DynamicString("")
)

object RecurrenceUiMapper {
    fun map(
        rule: RecurrenceRule,
        unitOptions: List<RecurrenceUnitOptionUiModel>
    ): RecurrenceUiModel {
        return RecurrenceUiModel(
            isRecurring = rule.isRecurring,
            interval = rule.interval,
            unit = rule.unit,
            intervalLabel = rule.interval.toString(),
            canDecrement = rule.interval > 1,
            selectedUnitLabel = unitOptions.find { it.unit == rule.unit }?.label ?: UiText.DynamicString(""),
            unitOptions = unitOptions,
            repeatLabel = UiText.StringResource(R.string.editor_recurrence_repeat),
            everyLabel = UiText.StringResource(R.string.editor_recurrence_every),
            decreaseContentDescription = UiText.StringResource(R.string.editor_recurrence_decrease),
            increaseContentDescription = UiText.StringResource(R.string.editor_recurrence_increase)
        )
    }
}
