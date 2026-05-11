package com.gustavo.brilhante.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DefaultTagColorPaletteTest {

    @Test
    fun `colorAt returns first color for index 0`() {
        assertThat(DefaultTagColorPalette.colorAt(0)).isEqualTo(DefaultTagColorPalette.RED)
    }

    @Test
    fun `colorAt returns last color for index 4`() {
        assertThat(DefaultTagColorPalette.colorAt(4)).isEqualTo(DefaultTagColorPalette.PURPLE)
    }

    @Test
    fun `colorAt cycles when index overflows`() {
        assertThat(DefaultTagColorPalette.colorAt(5)).isEqualTo(DefaultTagColorPalette.RED)
        assertThat(DefaultTagColorPalette.colorAt(10)).isEqualTo(DefaultTagColorPalette.RED)
    }

    @Test
    fun `colorAt handles large index values`() {
        assertThat(DefaultTagColorPalette.colorAt(1000)).isEqualTo(DefaultTagColorPalette.RED)
    }

    @Test
    fun `colorAt returns first color for negative index`() {
        assertThat(DefaultTagColorPalette.colorAt(-1)).isEqualTo(DefaultTagColorPalette.RED)
    }
}
