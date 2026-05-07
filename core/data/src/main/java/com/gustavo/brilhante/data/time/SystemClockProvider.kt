package com.gustavo.brilhante.data.time

import com.gustavo.brilhante.domain.time.ClockProvider
import javax.inject.Inject

class SystemClockProvider @Inject constructor() : ClockProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
