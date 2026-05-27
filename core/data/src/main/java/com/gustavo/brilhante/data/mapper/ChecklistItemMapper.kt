package com.gustavo.brilhante.data.mapper

import com.gustavo.brilhante.model.ChecklistItem
import com.gustavo.brilhante.storage.entity.ChecklistItemEntity

fun ChecklistItemEntity.toModel() = ChecklistItem(
    id = id,
    text = text,
    isChecked = isChecked
)

fun ChecklistItem.toEntity(taskId: Long) = ChecklistItemEntity(
    id = id,
    taskId = taskId,
    text = text,
    isChecked = isChecked
)
