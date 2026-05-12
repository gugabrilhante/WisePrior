package com.gustavo.brilhante.tasklist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavo.brilhante.domain.usecase.AddTagUseCase
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
import com.gustavo.brilhante.ui.UiText
import com.gustavo.brilhante.tasklist.R
import com.gustavo.brilhante.domain.usecase.SwipeDismissUseCase
import com.gustavo.brilhante.tasklist.presentation.mapper.TagEditorUiMapper
import com.gustavo.brilhante.tasklist.presentation.mapper.TaskListUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val sortPreferences: SortPreferencesDataStore,
    private val taskListUiMapper: TaskListUiMapper,
    private val tagEditorUiMapper: TagEditorUiMapper,
    private val swipeDismissUseCase: SwipeDismissUseCase
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
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") } }
                .collect { (tasks, collection, tags, sortOption) ->
                    _uiState.update { currentState ->
                        taskListUiMapper.map(
                            tasks = tasks,
                            collection = collection,
                            tags = tags,
                            sortOption = sortOption,
                            currentUiState = currentState
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
            swipeDismissUseCase {
                runCatching {
                    deleteTaskUseCase(task)
                    notificationScheduler.cancel(task.id)
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
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

    fun showAddTag(defaultColor: Long) {
        _uiState.update { 
            it.copy(
                showTagEditor = true, 
                editingTag = null,
                tagEditorDialog = tagEditorUiMapper.map(null, defaultColor)
            ) 
        }
    }

    fun showEditTag(tag: Tag) {
        _uiState.update { 
            it.copy(
                showTagEditor = true, 
                editingTag = tag,
                tagEditorDialog = tagEditorUiMapper.map(tag, 0L)
            ) 
        }
    }

    fun dismissTagEditor() {
        _uiState.update { it.copy(showTagEditor = false, editingTag = null, tagEditorDialog = null) }
    }

    fun saveTag(name: String, color: Long) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return
        val state = _uiState.value
        viewModelScope.launch {
            if (state.editingTag != null) {
                updateTagUseCase(state.editingTag.copy(name = trimmedName, color = color))
            } else {
                if (state.tags.size >= 5) { // MAX_TAGS was 5
                    _uiState.update { it.copy(error = "Maximum of 5 tags reached") }
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
}

// Minimal tuple to avoid Pair/Triple nesting for 4 values in combine
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private operator fun <A, B, C, D> Quad<A, B, C, D>.component1() = first
private operator fun <A, B, C, D> Quad<A, B, C, D>.component2() = second
private operator fun <A, B, C, D> Quad<A, B, C, D>.component3() = third
private operator fun <A, B, C, D> Quad<A, B, C, D>.component4() = fourth
