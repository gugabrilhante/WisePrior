package com.gustavo.brilhante.tasklist.presentation.mapper

import com.gustavo.brilhante.domain.usecase.CalculateTaskPriorityUseCase
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.model.TaskSortOption
import com.gustavo.brilhante.tasklist.R
import com.gustavo.brilhante.tasklist.model.TaskCollection
import com.gustavo.brilhante.tasklist.presentation.CollectionCounts
import com.gustavo.brilhante.tasklist.presentation.TaskListUiState
import com.gustavo.brilhante.ui.DateFormatter
import com.gustavo.brilhante.ui.UiText
import javax.inject.Inject

class TaskListUiMapper @Inject constructor(
    private val dateFormatter: DateFormatter,
    private val calculateTaskPriority: CalculateTaskPriorityUseCase,
    private val sortOptionUiMapper: SortOptionUiMapper
) {
    private val MAX_TAGS = 5

    fun map(
        tasks: List<Task>,
        collection: TaskCollection,
        tags: List<Tag>,
        sortOption: TaskSortOption,
        currentUiState: TaskListUiState
    ): TaskListUiState {
        val filteredTasks = filterByCollection(tasks, collection)
        val sortedTasks = sortedWith(filteredTasks, sortOption)
        
        val formattedDueDates = sortedTasks.mapNotNull { task ->
            task.dueDate?.let { dueDate ->
                val formatted = if (task.hasTime) dateFormatter.formatShortDateTime(dueDate)
                else dateFormatter.formatShortDate(dueDate)
                task.id to formatted
            }
        }.toMap()

        val screenTitle = when (collection) {
            TaskCollection.All -> UiText.StringResource(R.string.collection_all)
            TaskCollection.Today -> UiText.StringResource(R.string.collection_today)
            TaskCollection.Scheduled -> UiText.StringResource(R.string.collection_scheduled)
            TaskCollection.Flagged -> UiText.StringResource(R.string.collection_flagged)
            TaskCollection.Completed -> UiText.StringResource(R.string.collection_completed)
            is TaskCollection.ByTag -> {
                val tagName = tags.find { it.id == collection.tagId }?.name
                if (tagName != null) UiText.DynamicString(tagName)
                else UiText.StringResource(R.string.collection_tag_fallback)
            }
        }

        return currentUiState.copy(
            tasks = sortedTasks,
            formattedDueDates = formattedDueDates,
            selectedCollection = collection,
            collectionCounts = toCounts(tasks),
            tags = tags,
            tagCounts = toTagCounts(tasks),
            isLoading = false,
            sortOption = sortOption,
            screenTitle = screenTitle,
            sortOptions = sortOptionUiMapper.map(sortOption),
            showEmptyState = sortedTasks.isEmpty() && !currentUiState.isLoading,
            canAddTag = tags.size < MAX_TAGS,
            addTagLabel = if (tags.size < MAX_TAGS) UiText.StringResource(R.string.add_tag_label)
            else UiText.PluralResource(R.plurals.tag_limit_message, MAX_TAGS, listOf(MAX_TAGS))
        )
    }

    private fun filterByCollection(tasks: List<Task>, collection: TaskCollection): List<Task> =
        when (collection) {
            TaskCollection.All -> tasks
            TaskCollection.Today -> tasks.filter { task -> task.dueDate?.let { dateFormatter.isToday(it) } == true }
            TaskCollection.Scheduled -> tasks.filter { it.dueDate != null }
            TaskCollection.Flagged -> tasks.filter { it.isFlagged }
            TaskCollection.Completed -> tasks.filter { it.isCompleted }
            is TaskCollection.ByTag -> tasks.filter { task -> task.tagIds.contains(collection.tagId) }
        }

    private fun sortedWith(tasks: List<Task>, option: TaskSortOption): List<Task> = when (option) {
        TaskSortOption.CREATED_ASC -> tasks.sortedBy { it.createdAt }
        TaskSortOption.CREATED_DESC -> tasks.sortedByDescending { it.createdAt }
        TaskSortOption.SMART_PRIORITY -> tasks.sortedByDescending { calculateTaskPriority(it) }
    }

    private fun toCounts(tasks: List<Task>) = CollectionCounts(
        all = tasks.size,
        today = tasks.count { task -> task.dueDate?.let { dateFormatter.isToday(it) } == true },
        scheduled = tasks.count { it.dueDate != null },
        flagged = tasks.count { it.isFlagged },
        completed = tasks.count { it.isCompleted }
    )

    private fun toTagCounts(tasks: List<Task>): Map<Long, Int> = buildMap {
        tasks.forEach { task ->
            task.tagIds.forEach { tagId ->
                put(tagId, (get(tagId) ?: 0) + 1)
            }
        }
    }
}
