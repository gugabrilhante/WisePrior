package com.gustavo.brilhante.common

interface DateFormatter {
    fun formatDate(timestamp: Long): String
    fun formatTime(timestamp: Long): String
    fun formatDateTime(timestamp: Long): String
    fun formatShortDate(timestamp: Long): String
    fun formatShortDateTime(timestamp: Long): String
}
