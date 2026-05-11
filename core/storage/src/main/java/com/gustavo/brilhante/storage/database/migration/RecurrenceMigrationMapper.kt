package com.gustavo.brilhante.storage.database.migration

object RecurrenceMigrationMapper {
    fun map(oldType: String?): String {
        return when (oldType) {
            "DAILY" -> "DAYS"
            "WEEKLY" -> "WEEKS"
            "MONTHLY" -> "MONTHS"
            else -> "NONE"
        }
    }
}
