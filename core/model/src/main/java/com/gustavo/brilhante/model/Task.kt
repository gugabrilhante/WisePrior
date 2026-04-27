package com.gustavo.brilhante.model

data class Task(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val url: String = "",
    val dueDate: Long? = null,
    val hasTime: Boolean = false,
    val isUrgent: Boolean = false,
    val priority: Priority = Priority.NONE,
    val tagIds: List<Long> = emptyList(),
    val isFlagged: Boolean = false,
    val isCompleted: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val createdAt: Long = System.currentTimeMillis()
)
