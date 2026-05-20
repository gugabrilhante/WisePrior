package com.gustavo.brilhante.tasklist.presentation

import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.model.TaskSortOption
import com.gustavo.brilhante.tasklist.model.TaskCollection
import com.gustavo.brilhante.tasklist.presentation.mapper.TagEditorDialogUiModel
import com.gustavo.brilhante.ui.UiText

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val formattedDueDates: Map<Long, String> = emptyMap(),
    val selectedCollection: TaskCollection = TaskCollection.All,
    val collectionCounts: CollectionCounts = CollectionCounts(),
    val tags: List<Tag> = emptyList(),
    val tagCounts: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showTagEditor: Boolean = false,
    val editingTag: Tag? = null,
    val tagEditorDialog: TagEditorDialogUiModel? = null,
    val expandedTaskIds: Set<Long> = emptySet(),
    val sortOption: TaskSortOption = TaskSortOption.SMART_PRIORITY,
    val screenTitle: UiText = UiText.DynamicString(""),
    val sortOptions: List<SortOptionUiModel> = emptyList(),
    val showEmptyState: Boolean = true,
    val canAddTag: Boolean = true,
    val addTagLabel: UiText = UiText.DynamicString(""),
)

data class SortOptionUiModel(
    val option: TaskSortOption,
    val label: UiText,
    val isSelected: Boolean
)

data class CollectionCounts(
    val all: Int = 0,
    val today: Int = 0,
    val scheduled: Int = 0,
    val flagged: Int = 0,
    val completed: Int = 0
) {
    fun forCollection(collection: TaskCollection): Int = when (collection) {
        TaskCollection.All -> all
        TaskCollection.Today -> today
        TaskCollection.Scheduled -> scheduled
        TaskCollection.Flagged -> flagged
        TaskCollection.Completed -> completed
        is TaskCollection.ByTag -> 0
    }
}

sealed interface TaskListEvent {
    data class DeleteTask(val task: Task) : TaskListEvent
    data class EditTask(val task: Task) : TaskListEvent
    data object AddTask : TaskListEvent
    data class SelectCollection(val collection: TaskCollection) : TaskListEvent
    data class SetSortOption(val option: TaskSortOption) : TaskListEvent
    data class ToggleTaskChecked(val task: Task, val isChecked: Boolean) : TaskListEvent
    data class ToggleChecklistItem(val task: Task, val itemId: Long, val isChecked: Boolean) : TaskListEvent
    data class ToggleTaskExpanded(val taskId: Long) : TaskListEvent
    data class ShowAddTag(val defaultColor: Long) : TaskListEvent
    data class ShowEditTag(val tag: Tag) : TaskListEvent
    data object DismissTagEditor : TaskListEvent
    data class SaveTag(val name: String, val color: Long) : TaskListEvent
    data class DeleteTag(val tag: Tag) : TaskListEvent
    data object ErrorDismissed : TaskListEvent
}
