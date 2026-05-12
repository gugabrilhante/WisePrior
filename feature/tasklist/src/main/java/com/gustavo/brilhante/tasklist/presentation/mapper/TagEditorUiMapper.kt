package com.gustavo.brilhante.tasklist.presentation.mapper

import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.tasklist.R
import com.gustavo.brilhante.ui.UiText
import javax.inject.Inject

data class TagEditorDialogUiModel(
    val title: UiText,
    val initialName: String,
    val initialColor: Long,
    val showDelete: Boolean
)

class TagEditorUiMapper @Inject constructor() {
    fun map(editingTag: Tag?, defaultColor: Long): TagEditorDialogUiModel {
        return TagEditorDialogUiModel(
            title = if (editingTag != null) UiText.StringResource(R.string.edit_tag_title)
                    else UiText.StringResource(R.string.new_tag_title),
            initialName = editingTag?.name ?: "",
            initialColor = editingTag?.color ?: defaultColor,
            showDelete = editingTag != null
        )
    }
}
