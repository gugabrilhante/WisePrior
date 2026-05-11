package com.gustavo.brilhante.storage.database.migration

object TagMigrationMapper {
    fun mapToIdString(oldTags: String?, nameToId: Map<String, Long>): String {
        if (oldTags.isNullOrBlank()) return ""
        return oldTags.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { nameToId[it] }
            .joinToString(",")
    }
}
