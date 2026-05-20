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
import com.gustavo.brilhante.tasklist.data.SortPreferences
import com.gustavo.brilhante.tasklist.model.TaskCollection
import com.gustavo.brilhante.ui.UiText
import com.gustavo.brilhante.tasklist.R
import com.gustavo.brilhante.domain.usecase.SwipeDismissUseCase
import com.gustavo.brilhante.tasklist.presentation.mapper.TagEditorUiMapper
import com.gustavo.brilhante.tasklist.presentation.mapper.TaskListUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val sortPreferences: SortPreferences,
    private val taskListUiMapper: TaskListUiMapper,
    private val tagEditorUiMapper: TagEditorUiMapper,
    private val swipeDismissUseCase: SwipeDismissUseCase
) : ViewModel() {

    private val _selectedCollection = MutableStateFlow<TaskCollection>(TaskCollection.All)

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<TaskListEvent>()
    val navigationEvent: SharedFlow<TaskListEvent> = _navigationEvent.asSharedFlow()

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
                TaskData(tasks, collection, tags, sortOption)
            }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") } }
                .collect { data ->
                    _uiState.update { currentState ->
                        taskListUiMapper.map(
                            tasks = data.tasks,
                            collection = data.collection,
                            tags = data.tags,
                            sortOption = data.sortOption,
                            currentUiState = currentState
                        )
                    }
                }
        }
    }

    // ── Collection selection ──────────────────────────────────────────────────

    fun onEvent(event: TaskListEvent) {
        when (event) {
            is TaskListEvent.DeleteTask -> deleteTask(event.task)
            is TaskListEvent.SelectCollection -> onCollectionSelected(event.collection)
            is TaskListEvent.SetSortOption -> setSortOption(event.option)
            is TaskListEvent.ToggleTaskChecked -> onTaskCheckedChange(event.task, event.isChecked)
            is TaskListEvent.ToggleChecklistItem -> onChecklistItemToggled(event.task, event.itemId, event.isChecked)
            is TaskListEvent.ToggleTaskExpanded -> toggleExpanded(event.taskId)
            is TaskListEvent.ShowAddTag -> showAddTag(event.defaultColor)
            is TaskListEvent.ShowEditTag -> showEditTag(event.tag)
            TaskListEvent.DismissTagEditor -> dismissTagEditor()
            is TaskListEvent.SaveTag -> saveTag(event.name, event.color)
            is TaskListEvent.DeleteTag -> deleteTag(event.tag)
            TaskListEvent.ErrorDismissed -> _uiState.update { it.copy(error = null) }
            TaskListEvent.AddTask, is TaskListEvent.EditTask -> {
                viewModelScope.launch { _navigationEvent.emit(event) }
            }
        }
    }

    private fun onCollectionSelected(collection: TaskCollection) {
        _selectedCollection.value = collection
    }

    // ── Sort option ───────────────────────────────────────────────────────────

    private fun setSortOption(option: TaskSortOption) {
        viewModelScope.launch {
            sortPreferences.setSortOption(option)
        }
    }

    // ── Task actions ──────────────────────────────────────────────────────────

    private fun deleteTask(task: Task) {
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

    private fun onTaskCheckedChange(task: Task, isChecked: Boolean) {
        viewModelScope.launch {
            runCatching {
                updateTaskUseCase(task.copy(isCompleted = isChecked))
                if (isChecked) notificationScheduler.cancel(task.id)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun onChecklistItemToggled(task: Task, itemId: Long, isChecked: Boolean) {
        viewModelScope.launch {
            val updatedItems = task.checklistItems.map {
                if (it.id == itemId) it.copy(isChecked = isChecked) else it
            }
            val allChecked = updatedItems.isNotEmpty() && updatedItems.all { it.isChecked }
            val updatedTask = task.copy(
                checklistItems = updatedItems,
                isCompleted = allChecked
            )
            runCatching {
                updateTaskUseCase(updatedTask)
                if (allChecked && !task.isCompleted) notificationScheduler.cancel(task.id)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // ── Card expansion ────────────────────────────────────────────────────────

    private fun toggleExpanded(taskId: Long) {
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

    private fun showAddTag(defaultColor: Long) {
        _uiState.update { 
            it.copy(
                showTagEditor = true, 
                editingTag = null,
                tagEditorDialog = tagEditorUiMapper.map(null, defaultColor)
            ) 
        }
    }

    private fun showEditTag(tag: Tag) {
        _uiState.update { 
            it.copy(
                showTagEditor = true, 
                editingTag = tag,
                tagEditorDialog = tagEditorUiMapper.map(tag, 0L)
            ) 
        }
    }

    private fun dismissTagEditor() {
        _uiState.update { it.copy(showTagEditor = false, editingTag = null, tagEditorDialog = null) }
    }

    private fun saveTag(name: String, color: Long) {
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

    private fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            deleteTagUseCase(tag)
            if (_selectedCollection.value == TaskCollection.ByTag(tag.id)) {
                _selectedCollection.value = TaskCollection.All
            }
            dismissTagEditor()
        }
    }
}

private data class TaskData(
    val tasks: List<Task>,
    val collection: TaskCollection,
    val tags: List<Tag>,
    val sortOption: TaskSortOption
)
