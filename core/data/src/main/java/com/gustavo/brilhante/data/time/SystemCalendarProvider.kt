package com.gustavo.brilhante.data.time

import com.gustavo.brilhante.domain.time.CalendarProvider
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

class SystemCalendarProvider @Inject constructor() : CalendarProvider {
    override fun getInstance(): Calendar = Calendar.getInstance()
    override fun getInstance(timeZone: TimeZone): Calendar = Calendar.getInstance(timeZone)
}
