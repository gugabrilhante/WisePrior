package com.gustavo.brilhante.data.models

data class Task(
    val title: String,
    val description: String,
    val dueDate: Long,
    val reminderEnabled: Boolean = false
)