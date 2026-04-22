package com.gustavo.brilhante.storage.converter

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTagsList(tags: List<String>): String = tags.joinToString(",")

    @TypeConverter
    fun toTagsList(tags: String): List<String> =
        if (tags.isBlank()) emptyList() else tags.split(",")
}
