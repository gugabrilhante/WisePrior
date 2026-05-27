package com.gustavo.brilhante.data.mapper

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.storage.entity.TaskEntity
import com.gustavo.brilhante.storage.entity.TaskWithChecklistItems

fun TaskWithChecklistItems.toModel() = Task(
    id = task.id,
    title = task.title,
    notes = task.notes,
    url = task.url,
    dueDate = task.dueDate,
    hasTime = task.hasTime,
    isUrgent = task.isUrgent,
    priority = runCatching { Priority.valueOf(task.priority) }.getOrDefault(Priority.NONE),
    tagIds = task.tagIds,
    isFlagged = task.isFlagged,
    isCompleted = task.isCompleted,
    recurrenceRule = RecurrenceRule(
        unit = runCatching { RecurrenceUnit.valueOf(task.recurrenceUnit) }.getOrDefault(RecurrenceUnit.NONE),
        interval = task.recurrenceInterval.coerceAtLeast(1)
    ),
    createdAt = task.createdAt,
    checklistItems = checklistItems.sortedBy { it.id }.map { it.toModel() }
)

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    notes = notes,
    url = url,
    dueDate = dueDate,
    hasTime = hasTime,
    isUrgent = isUrgent,
    priority = priority.name,
    tagIds = tagIds,
    isFlagged = isFlagged,
    isCompleted = isCompleted,
    recurrenceUnit = recurrenceRule.unit.name,
    recurrenceInterval = recurrenceRule.interval,
    createdAt = createdAt
)

fun Task.toChecklistEntities() = checklistItems.map { it.toEntity(taskId = id) }
