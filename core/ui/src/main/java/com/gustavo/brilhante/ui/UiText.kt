package com.gustavo.brilhante.ui

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(@StringRes val resId: Int, vararg val args: Any) : UiText()
    class PluralResource(@PluralsRes val resId: Int, val count: Int, vararg val args: Any) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> if (resId != 0) stringResource(resId, *args) else ""
            is PluralResource -> if (resId != 0) pluralStringResource(resId, count, *args) else ""
        }
    }
}
