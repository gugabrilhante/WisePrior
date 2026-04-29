package com.gustavo.brilhante.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val url: String = "",
    val dueDate: Long? = null,
    val hasTime: Boolean = false,
    val isUrgent: Boolean = false,
    val priority: String = "NONE",
    val tagIds: List<Long> = emptyList(),
    val isFlagged: Boolean = false,
    val isCompleted: Boolean = false,
    val recurrenceType: String = "NONE",
    val createdAt: Long = System.currentTimeMillis()
)
