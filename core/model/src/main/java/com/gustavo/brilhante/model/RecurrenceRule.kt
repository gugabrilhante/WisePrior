package com.gustavo.brilhante.model

data class RecurrenceRule(
    val unit: RecurrenceUnit,
    val interval: Int = 1
) {
    val isRecurring: Boolean get() = unit != RecurrenceUnit.NONE

    companion object {
        val NONE = RecurrenceRule(RecurrenceUnit.NONE)
    }
}
