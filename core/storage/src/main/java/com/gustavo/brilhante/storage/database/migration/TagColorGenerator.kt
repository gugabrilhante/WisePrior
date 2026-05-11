package com.gustavo.brilhante.storage.database.migration

import com.gustavo.brilhante.model.DefaultTagColorPalette

class TagColorGenerator(private val colors: List<Long> = DefaultTagColorPalette.colors) {
    fun getColorForIndex(index: Int): Long {
        if (colors.isEmpty()) return 0L
        return colors[if (index < 0) 0 else index % colors.size]
    }
}
