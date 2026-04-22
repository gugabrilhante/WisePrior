package com.gustavo.brilhante.taskeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavo.brilhante.domain.usecase.AddTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTaskByIdUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskEditorUiState())
    val uiState: StateFlow<TaskEditorUiState> = _uiState.asStateFlow()

    private var editingTaskId: Long = -1L

    fun loadTask(id: Long) {
        if (id <= 0L) return
        editingTaskId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getTaskByIdUseCase(id)?.let { task ->
                _uiState.update {
                    TaskEditorUiState(
                        title = task.title,
                        notes = task.notes,
                        url = task.url,
                        hasDate = task.dueDate != null,
                        hasTime = task.hasTime,
                        dueDate = task.dueDate ?: System.currentTimeMillis(),
                        isUrgent = task.isUrgent,
                        priority = task.priority,
                        tags = task.tags,
                        isFlagged = task.isFlagged,
                        isLoading = false
                    )
                }
            } ?: _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onEvent(event: TaskEditorEvent) {
        when (event) {
            is TaskEditorEvent.TitleChanged ->
                _uiState.update { it.copy(title = event.title, titleError = null) }
            is TaskEditorEvent.NotesChanged ->
                _uiState.update { it.copy(notes = event.notes) }
            is TaskEditorEvent.UrlChanged ->
                _uiState.update { it.copy(url = event.url) }
            is TaskEditorEvent.ToggleDate ->
                _uiState.update { it.copy(hasDate = !it.hasDate, hasTime = if (it.hasDate) false else it.hasTime) }
            is TaskEditorEvent.ToggleTime ->
                _uiState.update { it.copy(hasTime = !it.hasTime) }
            is TaskEditorEvent.ToggleUrgent ->
                _uiState.update { it.copy(isUrgent = !it.isUrgent) }
            is TaskEditorEvent.ToggleFlagged ->
                _uiState.update { it.copy(isFlagged = !it.isFlagged) }
            is TaskEditorEvent.PriorityChanged ->
                _uiState.update { it.copy(priority = event.priority) }
            is TaskEditorEvent.DueDateChanged ->
                _uiState.update { it.copy(dueDate = event.date) }
            is TaskEditorEvent.TagAdded ->
                _uiState.update { it.copy(tags = (it.tags + event.tag).distinct()) }
            is TaskEditorEvent.TagRemoved ->
                _uiState.update { it.copy(tags = it.tags - event.tag) }
            is TaskEditorEvent.Save -> save()
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            return
        }
        viewModelScope.launch {
            val task = Task(
                id = if (editingTaskId > 0L) editingTaskId else 0L,
                title = state.title.trim(),
                notes = state.notes.trim(),
                url = state.url.trim(),
                dueDate = if (state.hasDate) state.dueDate else null,
                hasTime = state.hasTime,
                isUrgent = state.isUrgent,
                priority = state.priority,
                tags = state.tags,
                isFlagged = state.isFlagged
            )
            if (editingTaskId > 0L) updateTaskUseCase(task) else addTaskUseCase(task)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
