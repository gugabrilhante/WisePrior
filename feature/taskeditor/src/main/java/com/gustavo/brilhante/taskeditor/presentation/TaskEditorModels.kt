package com.gustavo.brilhante.taskeditor.presentation

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.ui.UiText

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
    val timePickerMinute: Int = 0
)

data class DateSectionUiModel(
    val hasDate: Boolean = false,
    val formattedDate: String? = null,
    val showTimeToggle: Boolean = false,
    val hasTime: Boolean = false,
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

data class TagSectionUiModel(
    val tags: List<TagItemUiModel> = emptyList(),
    val showEmptyState: Boolean = false
)

data class RecurrenceUiModel(
    val isRecurring: Boolean = false,
    val interval: Int = 1,
    val unit: RecurrenceUnit = RecurrenceUnit.DAYS,
    val intervalLabel: String = "1",
    val canDecrement: Boolean = false,
    val selectedUnitLabel: UiText = UiText.DynamicString(""),
    val unitOptions: List<RecurrenceUnitOptionUiModel> = emptyList()
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
            unitOptions = unitOptions
        )
    }
}
