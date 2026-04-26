package com.gustavo.brilhante.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class DateFormatterImpl @Inject constructor() : DateFormatter {

    private val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val shortDateTimeFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    override fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    override fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    override fun formatDateTime(timestamp: Long): String =
        "${formatDate(timestamp)} ${formatTime(timestamp)}"

    override fun formatShortDate(timestamp: Long): String = shortDateFormat.format(Date(timestamp))

    override fun formatShortDateTime(timestamp: Long): String =
        shortDateTimeFormat.format(Date(timestamp))
}
