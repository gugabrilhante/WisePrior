package com.gustavo.brilhante.model

object DefaultTagColorPalette {
    const val RED = 0xFFEF4444L
    const val BLUE = 0xFF3B82F6L
    const val GREEN = 0xFF22C55EL
    const val YELLOW = 0xFFEAB308L
    const val PURPLE = 0xFF8B5CF6L

    val colors = listOf(
        RED,
        BLUE,
        GREEN,
        YELLOW,
        PURPLE
    )

    fun colorAt(index: Int): Long {
        if (colors.isEmpty()) return 0L
        // Use Math.floorMod for negative indices or just handle positive
        val safeIndex = if (index < 0) 0 else index
        return colors[safeIndex % colors.size]
    }
}
