package com.gustavo.brilhante.tasklist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavo.brilhante.common.DateFormatter
import com.gustavo.brilhante.domain.usecase.AddTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTagUseCase
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.tasklist.model.TaskCollection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val MAX_TAGS = 5

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val dateFormatter: DateFormatter
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
                getTagsUseCase()
            ) { tasks, collection, tags -> Triple(tasks, collection, tags) }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { (tasks, collection, tags) ->
                    val filteredTasks = tasks.filterByCollection(collection)
                    val formattedDueDates = filteredTasks.mapNotNull { task ->
                        task.dueDate?.let { dueDate ->
                            val formatted = if (task.hasTime) dateFormatter.formatShortDateTime(dueDate)
                                            else dateFormatter.formatShortDate(dueDate)
                            task.id to formatted
                        }
                    }.toMap()
                    _uiState.update {
                        it.copy(
                            tasks = filteredTasks,
                            formattedDueDates = formattedDueDates,
                            selectedCollection = collection,
                            collectionCounts = tasks.toCounts(),
                            tags = tags,
                            tagCounts = tasks.toTagCounts(),
                            isLoading = false
                        )
                    }
                }
        }
    }

    // ── Collection selection ──────────────────────────────────────────────────

    fun onCollectionSelected(collection: TaskCollection) {
        _selectedCollection.value = collection
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
                if (state.tags.size >= MAX_TAGS) return@launch
                addTagUseCase(Tag(name = trimmedName, color = color))
            }
            dismissTagEditor()
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            deleteTagUseCase(tag)
            // If the user was viewing this tag, fall back to All
            if (_selectedCollection.value == TaskCollection.ByTag(tag.id)) {
                _selectedCollection.value = TaskCollection.All
            }
            dismissTagEditor()
        }
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

    private fun List<Task>.filterByCollection(collection: TaskCollection): List<Task> =
        when (collection) {
            TaskCollection.All -> this
            TaskCollection.Today -> filter { task -> task.dueDate?.let { isToday(it) } == true }
            TaskCollection.Scheduled -> filter { it.dueDate != null }
            TaskCollection.Flagged -> filter { it.isFlagged }
            TaskCollection.Completed -> filter { it.isCompleted }
            is TaskCollection.ByTag -> filter { task -> task.tagIds.contains(collection.tagId) }
        }

    private fun List<Task>.toCounts() = CollectionCounts(
        all = size,
        today = count { task -> task.dueDate?.let { isToday(it) } == true },
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

    private fun isToday(timestamp: Long): Boolean {
        val taskCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val now = Calendar.getInstance()
        return taskCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                taskCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    }
}
