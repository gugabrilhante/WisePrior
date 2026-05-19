package com.gustavo.brilhante.ui

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.designsystem.R as DesignR

object TaskCardUiMapper {
    fun map(
        task: Task,
        allTags: List<Tag>,
        formattedDueDate: String?
    ): TaskCardUiModel {
        val taskTags = allTags.filter { task.tagIds.contains(it.id) }
        val hasPriority = task.priority != Priority.NONE
        
        val hasExpandableContent = hasPriority ||
                task.isFlagged ||
                task.isUrgent ||
                task.notes.isNotBlank() ||
                task.checklistItems.isNotEmpty() ||
                taskTags.size > 2

        return TaskCardUiModel(
            id = task.id,
            title = task.title,
            notes = task.notes,
            isCompleted = task.isCompleted,
            isFlagged = task.isFlagged,
            isUrgent = task.isUrgent,
            priority = task.priority,
            tags = taskTags,
            formattedDueDate = formattedDueDate,
            checkboxDescriptionRes = if (task.isCompleted) R.string.task_card_mark_incomplete 
                                     else R.string.task_card_mark_complete,
            hasExpandableContent = hasExpandableContent,
            contentAlpha = if (task.isCompleted) 0.6f else 1f,
            priorityTextRes = when (task.priority) {
                Priority.LOW -> R.string.priority_low
                Priority.MEDIUM -> R.string.priority_medium
                Priority.HIGH -> R.string.priority_high
                Priority.NONE -> null
            },
            priorityColorRes = when (task.priority) {
                Priority.LOW -> DesignR.color.priority_low
                Priority.MEDIUM -> DesignR.color.priority_medium
                Priority.HIGH -> DesignR.color.priority_high
                Priority.NONE -> null
            },
            hasPriority = hasPriority,
            isTitleStrikethrough = task.isCompleted,
            checklistItems = task.checklistItems
        )
    }
}
