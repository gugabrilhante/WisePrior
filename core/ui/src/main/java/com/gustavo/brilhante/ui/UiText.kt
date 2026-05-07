package com.gustavo.brilhante.ui

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    data class PluralResource(
        @PluralsRes val resId: Int,
        val count: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args.toTypedArray())
            is PluralResource -> pluralStringResource(resId, count, *args.toTypedArray())
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args.toTypedArray())
            is PluralResource -> context.resources.getQuantityString(resId, count, *args.toTypedArray())
        }
    }
}
