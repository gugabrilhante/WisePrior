package com.gustavo.brilhante.domain.time

fun interface ClockProvider {
    fun currentTimeMillis(): Long
}
