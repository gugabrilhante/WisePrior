package com.gustavo.brilhante.storage.database.migration

import com.google.common.truth.Truth.assertThat
import com.gustavo.brilhante.model.DefaultTagColorPalette
import org.junit.Test

class TagColorGeneratorTest {

    @Test
    fun `getColorForIndex returns color at index when inside palette`() {
        val generator = TagColorGenerator()
        assertThat(generator.getColorForIndex(0)).isEqualTo(DefaultTagColorPalette.RED)
        assertThat(generator.getColorForIndex(1)).isEqualTo(DefaultTagColorPalette.BLUE)
    }

    @Test
    fun `getColorForIndex cycles through palette when index overflows`() {
        val generator = TagColorGenerator()
        val paletteSize = DefaultTagColorPalette.colors.size
        assertThat(generator.getColorForIndex(paletteSize)).isEqualTo(DefaultTagColorPalette.RED)
    }

    @Test
    fun `getColorForIndex returns 0 when palette is empty`() {
        val emptyGenerator = TagColorGenerator(emptyList())
        assertThat(emptyGenerator.getColorForIndex(0)).isEqualTo(0L)
    }

    @Test
    fun `getColorForIndex handles negative index`() {
        val generator = TagColorGenerator()
        assertThat(generator.getColorForIndex(-1)).isEqualTo(DefaultTagColorPalette.RED)
    }
}
