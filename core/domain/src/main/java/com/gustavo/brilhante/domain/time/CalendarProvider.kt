package com.gustavo.brilhante.domain.time

import java.util.Calendar
import java.util.TimeZone

interface CalendarProvider {
    fun getInstance(): Calendar
    fun getInstance(timeZone: TimeZone): Calendar
}
