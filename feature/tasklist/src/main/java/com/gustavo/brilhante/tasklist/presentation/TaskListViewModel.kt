package com.gustavo.brilhante.tasklist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavo.brilhante.domain.usecase.AddTagUseCase
import com.gustavo.brilhante.domain.usecase.CalculateTaskPriorityUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTagUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.model.TaskSortOption
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.tasklist.data.SortPreferencesDataStore
import com.gustavo.brilhante.tasklist.model.TaskCollection
import com.gustavo.brilhante.ui.DateFormatter
import com.gustavo.brilhante.ui.UiText
import com.gustavo.brilhante.tasklist.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_TAGS = 5

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val dateFormatter: DateFormatter,
    private val sortPreferences: SortPreferencesDataStore,
    private val calculateTaskPriority: CalculateTaskPriorityUseCase
) : ViewModel() {

    private val _selectedCollection = MutableStateFlow<TaskCollection>(TaskCollection.All)

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            combine(
                getTasksUseCase(),
                _selectedCollection,
                getTagsUseCase(),
                sortPreferences.sortOption
            ) { tasks, collection, tags, sortOption ->
                Quad(tasks, collection, tags, sortOption)
            }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { (tasks, collection, tags, sortOption) ->
                    val filteredTasks = tasks.filterByCollection(collection).sortedWith(sortOption)
                    val formattedDueDates = filteredTasks.mapNotNull { task ->
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

                    val sortOptions = listOf(
                        SortOptionUiModel(
                            TaskSortOption.CREATED_DESC,
                            UiText.StringResource(R.string.sort_created_newest),
                            sortOption == TaskSortOption.CREATED_DESC
                        ),
                        SortOptionUiModel(
                            TaskSortOption.CREATED_ASC,
                            UiText.StringResource(R.string.sort_created_oldest),
                            sortOption == TaskSortOption.CREATED_ASC
                        ),
                        SortOptionUiModel(
                            TaskSortOption.SMART_PRIORITY,
                            UiText.StringResource(R.string.sort_smart_priority),
                            sortOption == TaskSortOption.SMART_PRIORITY
                        )
                    )

                    _uiState.update {
                        it.copy(
                            tasks = filteredTasks,
                            formattedDueDates = formattedDueDates,
                            selectedCollection = collection,
                            collectionCounts = tasks.toCounts(),
                            tags = tags,
                            tagCounts = tasks.toTagCounts(),
                            isLoading = false,
                            sortOption = sortOption,
                            screenTitle = screenTitle,
                            sortOptions = sortOptions,
                            showEmptyState = filteredTasks.isEmpty() && !it.isLoading,
                            canAddTag = tags.size < MAX_TAGS,
                            addTagLabel = if (tags.size < MAX_TAGS) UiText.StringResource(R.string.add_tag_label)
                                          else UiText.PluralResource(R.plurals.tag_limit_message, MAX_TAGS, MAX_TAGS)
                        )
                    }
                }
        }
    }

    // ── Collection selection ──────────────────────────────────────────────────

    fun onCollectionSelected(collection: TaskCollection) {
        _selectedCollection.value = collection
    }

    // ── Sort option ───────────────────────────────────────────────────────────

    fun setSortOption(option: TaskSortOption) {
        viewModelScope.launch {
            sortPreferences.setSortOption(option)
        }
    }

    // ── Task actions ──────────────────────────────────────────────────────────

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            runCatching {
                deleteTaskUseCase(task)
                notificationScheduler.cancel(task.id)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onTaskCheckedChange(task: Task, isChecked: Boolean) {
        viewModelScope.launch {
            runCatching {
                updateTaskUseCase(task.copy(isCompleted = isChecked))
                if (isChecked) notificationScheduler.cancel(task.id)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // ── Card expansion ────────────────────────────────────────────────────────

    fun toggleExpanded(taskId: Long) {
        _uiState.update { state ->
            val updated = if (taskId in state.expandedTaskIds) {
                state.expandedTaskIds - taskId
            } else {
                state.expandedTaskIds + taskId
            }
            state.copy(expandedTaskIds = updated)
        }
    }

    // ── Tag editor UI state ───────────────────────────────────────────────────

    fun showAddTag() {
        _uiState.update { it.copy(showTagEditor = true, editingTag = null) }
    }

    fun showEditTag(tag: Tag) {
        _uiState.update { it.copy(showTagEditor = true, editingTag = tag) }
    }

    fun dismissTagEditor() {
        _uiState.update { it.copy(showTagEditor = false, editingTag = null) }
    }

    fun saveTag(name: String, color: Long) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return
        val state = _uiState.value
        viewModelScope.launch {
            if (state.editingTag != null) {
                updateTagUseCase(state.editingTag.copy(name = trimmedName, color = color))
            } else {
                if (state.tags.size >= MAX_TAGS) {
                    _uiState.update { it.copy(error = "Maximum of $MAX_TAGS tags reached") }
                    dismissTagEditor()
                    return@launch
                }
                addTagUseCase(Tag(name = trimmedName, color = color))
            }
            dismissTagEditor()
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            deleteTagUseCase(tag)
            if (_selectedCollection.value == TaskCollection.ByTag(tag.id)) {
                _selectedCollection.value = TaskCollection.All
            }
            dismissTagEditor()
        }
    }

    // ── Filtering & sorting ───────────────────────────────────────────────────

    private fun List<Task>.filterByCollection(collection: TaskCollection): List<Task> =
        when (collection) {
            TaskCollection.All -> this
            TaskCollection.Today -> filter { task -> task.dueDate?.let { dateFormatter.isToday(it) } == true }
            TaskCollection.Scheduled -> filter { it.dueDate != null }
            TaskCollection.Flagged -> filter { it.isFlagged }
            TaskCollection.Completed -> filter { it.isCompleted }
            is TaskCollection.ByTag -> filter { task -> task.tagIds.contains(collection.tagId) }
        }

    private fun List<Task>.sortedWith(option: TaskSortOption): List<Task> = when (option) {
        TaskSortOption.CREATED_ASC -> sortedBy { it.createdAt }
        TaskSortOption.CREATED_DESC -> sortedByDescending { it.createdAt }
        TaskSortOption.SMART_PRIORITY -> sortedByDescending { calculateTaskPriority(it) }
    }

    private fun List<Task>.toCounts() = CollectionCounts(
        all = size,
        today = count { task -> task.dueDate?.let { dateFormatter.isToday(it) } == true },
        scheduled = count { it.dueDate != null },
        flagged = count { it.isFlagged },
        completed = count { it.isCompleted }
    )

    private fun List<Task>.toTagCounts(): Map<Long, Int> = buildMap {
        this@toTagCounts.forEach { task ->
            task.tagIds.forEach { tagId ->
                put(tagId, (get(tagId) ?: 0) + 1)
            }
        }
    }
}

// Minimal tuple to avoid Pair/Triple nesting for 4 values in combine
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private operator fun <A, B, C, D> Quad<A, B, C, D>.component1() = first
private operator fun <A, B, C, D> Quad<A, B, C, D>.component2() = second
private operator fun <A, B, C, D> Quad<A, B, C, D>.component3() = third
private operator fun <A, B, C, D> Quad<A, B, C, D>.component4() = fourth
