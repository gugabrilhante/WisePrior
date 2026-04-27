package com.gustavo.brilhante.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class DateFormatterImpl @Inject constructor() : DateFormatter {

    private val dateFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    }
    private val timeFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    private val shortDateFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("MMM d", Locale.getDefault())
    }
    private val shortDateTimeFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    }

    override fun formatDate(timestamp: Long): String = 
        dateFormat.get()?.format(Date(timestamp)) ?: ""

    override fun formatTime(timestamp: Long): String = 
        timeFormat.get()?.format(Date(timestamp)) ?: ""

    override fun formatDateTime(timestamp: Long): String =
        "${formatDate(timestamp)} ${formatTime(timestamp)}"

    override fun formatShortDate(timestamp: Long): String = 
        shortDateFormat.get()?.format(Date(timestamp)) ?: ""

    override fun formatShortDateTime(timestamp: Long): String =
        shortDateTimeFormat.get()?.format(Date(timestamp)) ?: ""
}
