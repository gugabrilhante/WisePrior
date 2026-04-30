package com.gustavo.brilhante.common

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DateFormatterImplTest {

    private lateinit var formatter: DateFormatterImpl

    private val defaultLocale = Locale.getDefault()
    private val defaultTimeZone = TimeZone.getDefault()

    // Timestamp for 2024-01-15 14:30:00 UTC (Monday)
    private val timestamp: Long by lazy {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2024, Calendar.JANUARY, 15, 14, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Before
    fun setup() {
        Locale.setDefault(Locale.ENGLISH)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        formatter = DateFormatterImpl()
    }

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
        TimeZone.setDefault(defaultTimeZone)
    }

    @Test
    fun `formatDate returns full date string`() {
        assertEquals("Mon, Jan 15, 2024", formatter.formatDate(timestamp))
    }

    @Test
    fun `formatTime returns HHmm string`() {
        assertEquals("14:30", formatter.formatTime(timestamp))
    }

    @Test
    fun `formatDateTime combines date and time`() {
        assertEquals("Mon, Jan 15, 2024 14:30", formatter.formatDateTime(timestamp))
    }

    @Test
    fun `formatShortDate returns short month-day string`() {
        assertEquals("Jan 15", formatter.formatShortDate(timestamp))
    }

    @Test
    fun `formatShortDateTime returns short date with time`() {
        assertEquals("Jan 15, 14:30", formatter.formatShortDateTime(timestamp))
    }

    @Test
    fun `isToday returns true for current date`() {
        val now = System.currentTimeMillis()
        assertEquals(true, formatter.isToday(now))
    }

    @Test
    fun `toUtcMidnight strips time and returns UTC millis`() {
        val midnight = formatter.toUtcMidnight(timestamp)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = midnight
        }
        assertEquals(2024, cal.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH))
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
    }

    @Test
    fun `get components returns correct values`() {
        assertEquals(2024, formatter.getYear(timestamp))
        assertEquals(Calendar.JANUARY, formatter.getMonth(timestamp))
        assertEquals(15, formatter.getDayOfMonth(timestamp))
        assertEquals(14, formatter.getHour(timestamp))
        assertEquals(30, formatter.getMinute(timestamp))
    }

    @Test
    fun `updateDate changes only date parts`() {
        val newDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2025, Calendar.FEBRUARY, 20)
        }.timeInMillis
        val result = formatter.updateDate(timestamp, newDate)
        assertEquals(2025, formatter.getYear(result))
        assertEquals(Calendar.FEBRUARY, formatter.getMonth(result))
        assertEquals(20, formatter.getDayOfMonth(result))
        assertEquals(14, formatter.getHour(result))
        assertEquals(30, formatter.getMinute(result))
    }

    @Test
    fun `updateTime changes only time parts`() {
        val result = formatter.updateTime(timestamp, 10, 45)
        assertEquals(2024, formatter.getYear(result))
        assertEquals(Calendar.JANUARY, formatter.getMonth(result))
        assertEquals(15, formatter.getDayOfMonth(result))
        assertEquals(10, formatter.getHour(result))
        assertEquals(45, formatter.getMinute(result))
    }
}
