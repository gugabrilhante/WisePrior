package com.gustavo.brilhante.storage.database.migration

object TagNameParser {
    fun parse(tagsString: String?): List<String> {
        if (tagsString.isNullOrBlank()) return emptyList()
        return tagsString.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }
}
