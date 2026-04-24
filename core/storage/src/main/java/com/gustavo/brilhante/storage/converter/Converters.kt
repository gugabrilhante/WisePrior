package com.gustavo.brilhante.storage.converter

import androidx.room.TypeConverter

class Converters {
    // List<Long> — used for tagIds in TaskEntity
    @TypeConverter
    fun fromLongList(ids: List<Long>): String = ids.joinToString(",")

    @TypeConverter
    fun toLongList(ids: String): List<Long> =
        if (ids.isBlank()) emptyList() else ids.split(",").mapNotNull { it.trim().toLongOrNull() }
}
