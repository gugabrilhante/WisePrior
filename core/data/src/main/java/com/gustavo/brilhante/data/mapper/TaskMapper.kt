package com.gustavo.brilhante.data.mapper

import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.storage.entity.TaskEntity

fun TaskEntity.toModel() = Task(
    id = id,
    title = title,
    notes = notes,
    url = url,
    dueDate = dueDate,
    hasTime = hasTime,
    isUrgent = isUrgent,
    priority = runCatching { Priority.valueOf(priority) }.getOrDefault(Priority.NONE),
    tagIds = tagIds,
    isFlagged = isFlagged,
    isCompleted = isCompleted,
    recurrenceRule = RecurrenceRule(
        unit = runCatching { RecurrenceUnit.valueOf(recurrenceUnit) }.getOrDefault(RecurrenceUnit.NONE),
        interval = recurrenceInterval.coerceAtLeast(1)
    ),
    createdAt = createdAt
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
