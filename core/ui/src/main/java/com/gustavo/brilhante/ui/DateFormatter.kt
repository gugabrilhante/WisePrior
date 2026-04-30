package com.gustavo.brilhante.ui

interface DateFormatter {
    fun formatDate(timestamp: Long): String
    fun formatTime(timestamp: Long): String
    fun formatDateTime(timestamp: Long): String
    fun formatShortDate(timestamp: Long): String
    fun formatShortDateTime(timestamp: Long): String
    fun isToday(timestamp: Long): Boolean
    fun toUtcMidnight(timestamp: Long): Long
    fun getYear(timestamp: Long): Int
    fun getMonth(timestamp: Long): Int
    fun getDayOfMonth(timestamp: Long): Int
    fun getHour(timestamp: Long): Int
    fun getMinute(timestamp: Long): Int
    fun updateDate(timestamp: Long, dateMillis: Long): Long
    fun updateTime(timestamp: Long, hour: Int, minute: Int): Long
}
