package com.gustavo.brilhante.ui

import com.gustavo.brilhante.domain.time.CalendarProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DateFormatterImplTest {

    private val calendarProvider = mockk<CalendarProvider>()
    private lateinit var dateFormatter: DateFormatterImpl
    private lateinit var originalLocale: Locale

    @Before
    fun setup() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
        
        every { calendarProvider.getInstance() } answers { Calendar.getInstance() }
        every { calendarProvider.getInstance(any<TimeZone>()) } answers { Calendar.getInstance(it.invocation.args[0] as TimeZone) }
        
        dateFormatter = DateFormatterImpl(calendarProvider)
    }

    // ─── formatDate ───────────────────────────────────────────────────────────

    @Test
    fun `formatDate returns correctly formatted string`() {
        val calendar = Calendar.getInstance().apply {
            set(2023, Calendar.JANUARY, 15)
        }
        val result = dateFormatter.formatDate(calendar.timeInMillis)
        assertTrue(result.contains("Jan"))
        assertTrue(result.contains("15"))
        assertTrue(result.contains("2023"))
    }

    @Test
    fun `formatDate includes day of week`() {
        // Jan 15 2023 was a Sunday
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.JANUARY, 15) }
        assertTrue(dateFormatter.formatDate(calendar.timeInMillis).startsWith("Sun"))
    }

    @Test
    fun `formatDate on epoch timestamp`() {
        val result = dateFormatter.formatDate(0L)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `formatDate on negative timestamp`() {
        val result = dateFormatter.formatDate(-1L)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `formatDate on leap day`() {
        val calendar = Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 29) }
        val result = dateFormatter.formatDate(calendar.timeInMillis)
        assertTrue(result.contains("Feb"))
        assertTrue(result.contains("29"))
        assertTrue(result.contains("2024"))
    }

    @Test
    fun `formatDate on year boundary Dec 31`() {
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.DECEMBER, 31) }
        val result = dateFormatter.formatDate(calendar.timeInMillis)
        assertTrue(result.contains("Dec"))
        assertTrue(result.contains("31"))
        assertTrue(result.contains("2023"))
    }

    @Test
    fun `formatDate respects French locale on fresh instance`() {
        Locale.setDefault(Locale.FRENCH)
        // Use a fresh instance so ThreadLocal initializes with the French locale
        val frenchFormatter = DateFormatterImpl(calendarProvider)
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.JANUARY, 15) }
        val result = frenchFormatter.formatDate(calendar.timeInMillis)
        // French month abbreviation for January is "janv."
        assertFalse("French locale should not produce 'Jan'", result.contains("Jan"))
        Locale.setDefault(Locale.US)
    }

    // ─── formatTime ───────────────────────────────────────────────────────────

    @Test
    fun `formatTime returns correctly formatted string`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
        }
        assertEquals("14:30", dateFormatter.formatTime(calendar.timeInMillis))
    }

    @Test
    fun `formatTime midnight returns 00 00`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        assertEquals("00:00", dateFormatter.formatTime(calendar.timeInMillis))
    }

    @Test
    fun `formatTime noon returns 12 00`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }
        assertEquals("12:00", dateFormatter.formatTime(calendar.timeInMillis))
    }

    @Test
    fun `formatTime last minute of day`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }
        assertEquals("23:59", dateFormatter.formatTime(calendar.timeInMillis))
    }

    @Test
    fun `formatTime zero-pads single-digit minutes`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 5)
        }
        assertEquals("09:05", dateFormatter.formatTime(calendar.timeInMillis))
    }

    // ─── formatDateTime ───────────────────────────────────────────────────────

    @Test
    fun `formatDateTime returns date and time combined`() {
        val calendar = Calendar.getInstance().apply {
            set(2023, Calendar.JANUARY, 15, 14, 30)
        }
        val result = dateFormatter.formatDateTime(calendar.timeInMillis)
        assertTrue(result.contains("Jan 15, 2023"))
        assertTrue(result.contains("14:30"))
    }

    @Test
    fun `formatDateTime separates date and time with space`() {
        val calendar = Calendar.getInstance().apply {
            set(2023, Calendar.JUNE, 1, 8, 0)
        }
        val result = dateFormatter.formatDateTime(calendar.timeInMillis)
        val parts = result.split(" ")
        assertTrue("Expected at least date and time parts", parts.size >= 2)
    }

    // ─── formatShortDate ──────────────────────────────────────────────────────

    @Test
    fun `formatShortDate returns MMM d format`() {
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.MARCH, 7) }
        val result = dateFormatter.formatShortDate(calendar.timeInMillis)
        assertEquals("Mar 7", result)
    }

    @Test
    fun `formatShortDate omits year`() {
        val calendar = Calendar.getInstance().apply { set(2024, Calendar.DECEMBER, 25) }
        val result = dateFormatter.formatShortDate(calendar.timeInMillis)
        assertFalse("Short date should not contain year", result.contains("2024"))
    }

    @Test
    fun `formatShortDate on epoch`() {
        val result = dateFormatter.formatShortDate(0L)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `formatShortDate on leap day`() {
        val calendar = Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 29) }
        assertEquals("Feb 29", dateFormatter.formatShortDate(calendar.timeInMillis))
    }

    @Test
    fun `formatShortDate uses locale month name on fresh instance`() {
        Locale.setDefault(Locale.FRENCH)
        val frenchFormatter = DateFormatterImpl(calendarProvider)
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.MARCH, 7) }
        val result = frenchFormatter.formatShortDate(calendar.timeInMillis)
        assertFalse("French locale should not produce 'Mar'", result.contains("Mar"))
        Locale.setDefault(Locale.US)
    }

    // ─── formatShortDateTime ─────────────────────────────────────────────────

    @Test
    fun `formatShortDateTime returns MMM d HH mm format`() {
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.AUGUST, 4, 9, 5) }
        val result = dateFormatter.formatShortDateTime(calendar.timeInMillis)
        assertEquals("Aug 4, 09:05", result)
    }

    @Test
    fun `formatShortDateTime midnight time`() {
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.JANUARY, 1, 0, 0) }
        val result = dateFormatter.formatShortDateTime(calendar.timeInMillis)
        assertTrue(result.contains("00:00"))
    }

    @Test
    fun `formatShortDateTime last minute of day`() {
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.JANUARY, 1, 23, 59) }
        val result = dateFormatter.formatShortDateTime(calendar.timeInMillis)
        assertTrue(result.contains("23:59"))
    }

    @Test
    fun `formatShortDateTime omits year`() {
        val calendar = Calendar.getInstance().apply { set(2024, Calendar.JULY, 4, 10, 0) }
        assertFalse(dateFormatter.formatShortDateTime(calendar.timeInMillis).contains("2024"))
    }

    // ─── isToday ──────────────────────────────────────────────────────────────

    @Test
    fun `isToday returns true for current time`() {
        val now = Calendar.getInstance()
        every { calendarProvider.getInstance() } returns now
        assertTrue(dateFormatter.isToday(now.timeInMillis))
    }

    @Test
    fun `isToday returns false for yesterday`() {
        val now = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        every { calendarProvider.getInstance() } returnsMany listOf(yesterday, now)
        assertFalse(dateFormatter.isToday(yesterday.timeInMillis))
    }

    @Test
    fun `isToday returns false for tomorrow`() {
        val now = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        every { calendarProvider.getInstance() } returnsMany listOf(tomorrow, now)
        assertFalse(dateFormatter.isToday(tomorrow.timeInMillis))
    }

    @Test
    fun `isToday returns false for far past`() {
        val now = Calendar.getInstance()
        val past = Calendar.getInstance().apply { set(2000, Calendar.JANUARY, 1) }
        every { calendarProvider.getInstance() } returnsMany listOf(past, now)
        assertFalse(dateFormatter.isToday(past.timeInMillis))
    }

    @Test
    fun `isToday returns false for far future`() {
        val now = Calendar.getInstance()
        val future = Calendar.getInstance().apply { set(2099, Calendar.DECEMBER, 31) }
        every { calendarProvider.getInstance() } returnsMany listOf(future, now)
        assertFalse(dateFormatter.isToday(future.timeInMillis))
    }

    @Test
    fun `isToday returns true at start of today`() {
        val now = Calendar.getInstance()
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        every { calendarProvider.getInstance() } returnsMany listOf(startOfDay, now)
        assertTrue(dateFormatter.isToday(startOfDay.timeInMillis))
    }

    @Test
    fun `isToday returns true at end of today`() {
        val now = Calendar.getInstance()
        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        every { calendarProvider.getInstance() } returnsMany listOf(endOfDay, now)
        assertTrue(dateFormatter.isToday(endOfDay.timeInMillis))
    }

    // ─── toUtcMidnight ────────────────────────────────────────────────────────

    @Test
    fun `toUtcMidnight returns correct date components`() {
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.MARCH, 10, 15, 45) }
        val result = dateFormatter.toUtcMidnight(calendar.timeInMillis)

        val resultCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = result
        }
        assertEquals(2023, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, resultCal.get(Calendar.MONTH))
        assertEquals(10, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `toUtcMidnight zeros out time components`() {
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.MARCH, 10, 23, 59, 58) }
        val result = dateFormatter.toUtcMidnight(calendar.timeInMillis)

        val resultCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = result
        }
        assertEquals(0, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
        assertEquals(0, resultCal.get(Calendar.SECOND))
        assertEquals(0, resultCal.get(Calendar.MILLISECOND))
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    @Test
    fun `getters return correct components`() {
        val calendar = Calendar.getInstance().apply { set(2024, Calendar.MAY, 20, 11, 55) }
        val ts = calendar.timeInMillis

        assertEquals(2024, dateFormatter.getYear(ts))
        assertEquals(Calendar.MAY, dateFormatter.getMonth(ts))
        assertEquals(20, dateFormatter.getDayOfMonth(ts))
        assertEquals(11, dateFormatter.getHour(ts))
        assertEquals(55, dateFormatter.getMinute(ts))
    }

    @Test
    fun `getYear returns correct year for leap year`() {
        val calendar = Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 29) }
        assertEquals(2024, dateFormatter.getYear(calendar.timeInMillis))
    }

    @Test
    fun `getMonth returns 0-based month index`() {
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.JANUARY, 1) }
        assertEquals(0, dateFormatter.getMonth(calendar.timeInMillis))
    }

    @Test
    fun `getDayOfMonth returns correct day at month boundary`() {
        val calendar = Calendar.getInstance().apply { set(2023, Calendar.JANUARY, 31) }
        assertEquals(31, dateFormatter.getDayOfMonth(calendar.timeInMillis))
    }

    @Test
    fun `getHour returns 0 for midnight`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        assertEquals(0, dateFormatter.getHour(calendar.timeInMillis))
    }

    @Test
    fun `getMinute returns 0 for top of hour`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
        }
        assertEquals(0, dateFormatter.getMinute(calendar.timeInMillis))
    }

    // ─── updateDate ───────────────────────────────────────────────────────────

    @Test
    fun `updateDate preserves time but changes date`() {
        val original = Calendar.getInstance().apply {
            set(2023, Calendar.JANUARY, 1, 10, 20)
        }.timeInMillis

        val newDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2023, Calendar.DECEMBER, 25, 0, 0)
        }.timeInMillis

        val updated = dateFormatter.updateDate(original, newDate)
        val updatedCal = Calendar.getInstance().apply { timeInMillis = updated }

        assertEquals(2023, updatedCal.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, updatedCal.get(Calendar.MONTH))
        assertEquals(25, updatedCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(10, updatedCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(20, updatedCal.get(Calendar.MINUTE))
    }

    @Test
    fun `updateDate zeros out seconds and milliseconds`() {
        val original = Calendar.getInstance().apply {
            set(2023, Calendar.JANUARY, 1, 10, 20, 45)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val newDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2023, Calendar.JUNE, 10, 0, 0, 0)
        }.timeInMillis

        val updated = dateFormatter.updateDate(original, newDate)
        val updatedCal = Calendar.getInstance().apply { timeInMillis = updated }

        assertEquals(0, updatedCal.get(Calendar.SECOND))
        assertEquals(0, updatedCal.get(Calendar.MILLISECOND))
    }

    @Test
    fun `updateDate with midnight time preserves zero hour and minute`() {
        val original = Calendar.getInstance().apply {
            set(2023, Calendar.JANUARY, 1, 0, 0)
        }.timeInMillis

        val newDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2024, Calendar.FEBRUARY, 29, 0, 0)
        }.timeInMillis

        val updated = dateFormatter.updateDate(original, newDate)
        val updatedCal = Calendar.getInstance().apply { timeInMillis = updated }

        assertEquals(0, updatedCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, updatedCal.get(Calendar.MINUTE))
    }

    // ─── updateTime ───────────────────────────────────────────────────────────

    @Test
    fun `updateTime changes only time`() {
        val original = Calendar.getInstance().apply {
            set(2023, Calendar.JANUARY, 1, 10, 20)
        }.timeInMillis

        val updated = dateFormatter.updateTime(original, 18, 45)
        val updatedCal = Calendar.getInstance().apply { timeInMillis = updated }

        assertEquals(2023, updatedCal.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, updatedCal.get(Calendar.MONTH))
        assertEquals(1, updatedCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(18, updatedCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(45, updatedCal.get(Calendar.MINUTE))
    }

    @Test
    fun `updateTime zeros out seconds and milliseconds`() {
        val original = Calendar.getInstance().apply {
            set(2023, Calendar.JANUARY, 1, 10, 20, 55)
            set(Calendar.MILLISECOND, 500)
        }.timeInMillis

        val updated = dateFormatter.updateTime(original, 8, 0)
        val updatedCal = Calendar.getInstance().apply { timeInMillis = updated }

        assertEquals(0, updatedCal.get(Calendar.SECOND))
        assertEquals(0, updatedCal.get(Calendar.MILLISECOND))
    }

    @Test
    fun `updateTime to midnight`() {
        val original = Calendar.getInstance().apply {
            set(2023, Calendar.JUNE, 15, 14, 30)
        }.timeInMillis

        val updated = dateFormatter.updateTime(original, 0, 0)
        val updatedCal = Calendar.getInstance().apply { timeInMillis = updated }

        assertEquals(0, updatedCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, updatedCal.get(Calendar.MINUTE))
        assertEquals(Calendar.JUNE, updatedCal.get(Calendar.MONTH))
        assertEquals(15, updatedCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `updateTime to end of day`() {
        val original = Calendar.getInstance().apply {
            set(2023, Calendar.JUNE, 15, 0, 0)
        }.timeInMillis

        val updated = dateFormatter.updateTime(original, 23, 59)
        val updatedCal = Calendar.getInstance().apply { timeInMillis = updated }

        assertEquals(23, updatedCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, updatedCal.get(Calendar.MINUTE))
    }
}
