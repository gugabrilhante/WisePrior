package com.gustavo.brilhante.tasklist.presentation.mapper

import com.gustavo.brilhante.model.TaskSortOption
import com.gustavo.brilhante.tasklist.R
import com.gustavo.brilhante.tasklist.presentation.SortOptionUiModel
import com.gustavo.brilhante.ui.UiText
import javax.inject.Inject

class SortOptionUiMapper @Inject constructor() {
    fun map(selectedOption: TaskSortOption): List<SortOptionUiModel> {
        return listOf(
            SortOptionUiModel(
                TaskSortOption.CREATED_DESC,
                UiText.StringResource(R.string.sort_created_newest),
                selectedOption == TaskSortOption.CREATED_DESC
            ),
            SortOptionUiModel(
                TaskSortOption.CREATED_ASC,
                UiText.StringResource(R.string.sort_created_oldest),
                selectedOption == TaskSortOption.CREATED_ASC
            ),
            SortOptionUiModel(
                TaskSortOption.SMART_PRIORITY,
                UiText.StringResource(R.string.sort_smart_priority),
                selectedOption == TaskSortOption.SMART_PRIORITY
            )
        )
    }
}
