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
}
