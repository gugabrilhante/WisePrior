package com.gustavo.brilhante.taskeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavo.brilhante.domain.usecase.AddTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTaskByIdUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskEditorUiState())
    val uiState: StateFlow<TaskEditorUiState> = _uiState.asStateFlow()

    // Derived StateFlows for consumers that prefer granular observation
    val tags: StateFlow<List<Tag>> = uiState
        .map { it.availableTags }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedTagIds: StateFlow<Set<Long>> = uiState
        .map { it.selectedTagIds }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _navigationEvent = Channel<Unit>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var editingTaskId: Long = -1L

    init {
        observeAvailableTags()
    }

    private fun observeAvailableTags() {
        viewModelScope.launch {
            getTagsUseCase().collect { tags ->
                _uiState.update { it.copy(availableTags = tags) }
            }
        }
    }

    fun loadTask(id: Long) {
        if (id <= 0L) {
            _uiState.update { current ->
                TaskEditorUiState(availableTags = current.availableTags)
            }
            editingTaskId = -1L
            return
        }
        if (editingTaskId == id) return
        editingTaskId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getTaskByIdUseCase(id)?.let { task ->
                _uiState.update { current ->
                    TaskEditorUiState(
                        title = task.title,
                        notes = task.notes,
                        url = task.url,
                        hasDate = task.dueDate != null,
                        hasTime = task.hasTime,
                        dueDate = task.dueDate ?: System.currentTimeMillis(),
                        isUrgent = task.isUrgent,
                        priority = task.priority,
                        selectedTagIds = task.tagIds.toSet(),
                        availableTags = current.availableTags,
                        isFlagged = task.isFlagged,
                        isCompleted = task.isCompleted,
                        recurrenceType = task.recurrenceType,
                        isLoading = false
                    )
                }
            } ?: _uiState.update { it.copy(isLoading = false) }
        }
    }

    // ── Tag selection ─────────────────────────────────────────────────────────

    fun onTagSelected(tagId: Long) {
        _uiState.update { it.copy(selectedTagIds = it.selectedTagIds + tagId) }
    }

    fun onTagRemoved(tagId: Long) {
        _uiState.update { it.copy(selectedTagIds = it.selectedTagIds - tagId) }
    }

    // ── Events ────────────────────────────────────────────────────────────────

    fun onEvent(event: TaskEditorEvent) {
        when (event) {
            is TaskEditorEvent.TitleChanged ->
                _uiState.update { it.copy(title = event.title, titleError = null) }
            is TaskEditorEvent.NotesChanged ->
                _uiState.update { it.copy(notes = event.notes) }
            is TaskEditorEvent.UrlChanged ->
                _uiState.update { it.copy(url = event.url) }
            is TaskEditorEvent.ToggleDate -> {
                val turningOff = _uiState.value.hasDate
                _uiState.update {
                    it.copy(
                        hasDate = !it.hasDate,
                        hasTime = if (turningOff) false else it.hasTime,
                        recurrenceType = if (turningOff) com.gustavo.brilhante.model.RecurrenceType.NONE else it.recurrenceType
                    )
                }
            }
            is TaskEditorEvent.ToggleTime ->
                _uiState.update { it.copy(hasTime = !it.hasTime) }
            is TaskEditorEvent.ToggleUrgent ->
                _uiState.update { it.copy(isUrgent = !it.isUrgent) }
            is TaskEditorEvent.ToggleFlagged ->
                _uiState.update { it.copy(isFlagged = !it.isFlagged) }
            is TaskEditorEvent.PriorityChanged ->
                _uiState.update { it.copy(priority = event.priority) }
            is TaskEditorEvent.DueDateChanged ->
                _uiState.update { state ->
                    val prevCal = Calendar.getInstance().apply { timeInMillis = state.dueDate }
                    val hour = prevCal.get(Calendar.HOUR_OF_DAY)
                    val minute = prevCal.get(Calendar.MINUTE)
                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = event.dateMillis
                    }
                    val newCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                        set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    state.copy(dueDate = newCal.timeInMillis, showDatePicker = false)
                }
            is TaskEditorEvent.TimeChanged ->
                _uiState.update { state ->
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = state.dueDate
                        set(Calendar.HOUR_OF_DAY, event.hour)
                        set(Calendar.MINUTE, event.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    state.copy(dueDate = cal.timeInMillis, showTimePicker = false)
                }
            is TaskEditorEvent.RecurrenceChanged ->
                _uiState.update { it.copy(recurrenceType = event.recurrenceType) }
            is TaskEditorEvent.ShowDatePicker ->
                _uiState.update { it.copy(showDatePicker = true) }
            is TaskEditorEvent.HideDatePicker ->
                _uiState.update { it.copy(showDatePicker = false) }
            is TaskEditorEvent.ShowTimePicker ->
                _uiState.update { it.copy(showTimePicker = true) }
            is TaskEditorEvent.HideTimePicker ->
                _uiState.update { it.copy(showTimePicker = false) }
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
                tagIds = state.selectedTagIds.toList(),
                isFlagged = state.isFlagged,
                isCompleted = state.isCompleted,
                recurrenceType = state.recurrenceType
            )

            val savedTask = if (editingTaskId > 0L) {
                updateTaskUseCase(task)
                notificationScheduler.cancel(editingTaskId)
                task
            } else {
                addTaskUseCase(task)
                task
            }

            savedTask.dueDate?.let { due ->
                if (due > System.currentTimeMillis()) notificationScheduler.schedule(savedTask)
            }

            _navigationEvent.send(Unit)
        }
    }
}
