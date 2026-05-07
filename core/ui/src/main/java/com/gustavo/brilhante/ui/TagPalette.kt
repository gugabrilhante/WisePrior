package com.gustavo.brilhante.ui

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.gustavo.brilhante.designsystem.R as DesignR

data class TagPaletteEntry(
    @ColorRes val colorResId: Int,
    @StringRes val nameResId: Int
)

object TagPalette {
    val colors = listOf(
        TagPaletteEntry(DesignR.color.tag_red, R.string.color_name_red),
        TagPaletteEntry(DesignR.color.tag_orange, R.string.color_name_orange),
        TagPaletteEntry(DesignR.color.tag_yellow, R.string.color_name_yellow),
        TagPaletteEntry(DesignR.color.tag_green, R.string.color_name_green),
        TagPaletteEntry(DesignR.color.tag_blue, R.string.color_name_blue),
        TagPaletteEntry(DesignR.color.tag_purple, R.string.color_name_purple),
        TagPaletteEntry(DesignR.color.tag_pink, R.string.color_name_pink),
        TagPaletteEntry(DesignR.color.tag_gray, R.string.color_name_gray),
    )
}
