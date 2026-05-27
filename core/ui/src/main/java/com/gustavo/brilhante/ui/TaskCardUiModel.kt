package com.gustavo.brilhante.ui

import androidx.compose.ui.graphics.Color
import com.gustavo.brilhante.model.ChecklistItem
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task

data class TaskCardUiModel(
    val id: Long,
    val title: String,
    val notes: String,
    val isCompleted: Boolean,
    val isFlagged: Boolean,
    val isUrgent: Boolean,
    val priority: Priority,
    val tags: List<Tag>,
    val formattedDueDate: String?,
    val checkboxDescriptionRes: Int,
    val hasExpandableContent: Boolean,
    val contentAlpha: Float,
    val priorityTextRes: Int?,
    val priorityColorRes: Int?,
    val hasPriority: Boolean,
    val isTitleStrikethrough: Boolean,
    val checklistItems: List<ChecklistItemUiModel> = emptyList()
)

data class ChecklistItemUiModel(
    val id: Long,
    val text: String,
    val isChecked: Boolean,
    val isDisplayChecked: Boolean,
    val checkboxDescriptionRes: Int
)
