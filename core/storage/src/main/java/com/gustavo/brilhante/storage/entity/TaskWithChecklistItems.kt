package com.gustavo.brilhante.storage.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithChecklistItems(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val checklistItems: List<ChecklistItemEntity>
)
