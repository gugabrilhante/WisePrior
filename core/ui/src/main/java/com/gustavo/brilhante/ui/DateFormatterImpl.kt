package com.gustavo.brilhante.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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

    override fun isToday(timestamp: Long): Boolean {
        val taskCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val now = Calendar.getInstance()
        return taskCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                taskCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    }

    override fun toUtcMidnight(timestamp: Long): Long {
        val localCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, localCal.get(Calendar.YEAR))
            set(Calendar.MONTH, localCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, localCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    override fun getYear(timestamp: Long): Int =
        Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.YEAR)

    override fun getMonth(timestamp: Long): Int =
        Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.MONTH)

    override fun getDayOfMonth(timestamp: Long): Int =
        Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.DAY_OF_MONTH)

    override fun getHour(timestamp: Long): Int =
        Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.HOUR_OF_DAY)

    override fun getMinute(timestamp: Long): Int =
        Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.MINUTE)

    override fun updateDate(timestamp: Long, dateMillis: Long): Long {
        val prevCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = prevCal.get(Calendar.HOUR_OF_DAY)
        val minute = prevCal.get(Calendar.MINUTE)
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = dateMillis
        }
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
            set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    override fun updateTime(timestamp: Long, hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
