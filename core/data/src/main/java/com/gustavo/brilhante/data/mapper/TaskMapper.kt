package com.gustavo.brilhante.data.mapper

import com.gustavo.brilhante.model.Priority
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
    tags = tags,
    isFlagged = isFlagged,
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
    tags = tags,
    isFlagged = isFlagged,
    createdAt = createdAt
)
