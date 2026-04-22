package com.gustavo.brilhante.notifications

// Notification channel
const val CHANNEL_ID = "wiseprior_reminders"
const val CHANNEL_NAME = "Task Reminders"

// Intent extras — shared between scheduler and receiver
const val EXTRA_TASK_ID = "extra_task_id"
const val EXTRA_TASK_TITLE = "extra_task_title"
const val EXTRA_TASK_NOTES = "extra_task_notes"
const val EXTRA_DUE_DATE = "extra_due_date"
const val EXTRA_HAS_TIME = "extra_has_time"
const val EXTRA_RECURRENCE = "extra_recurrence"
