package com.gustavo.brilhante.taskeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavo.brilhante.domain.usecase.AddTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTaskByIdUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.ui.DateFormatter
import com.gustavo.brilhante.ui.TestTags
import com.gustavo.brilhante.ui.UiText
import com.gustavo.brilhante.taskeditor.R
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
import javax.inject.Inject

@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val dateFormatter: DateFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskEditorUiState())
    val uiState: StateFlow<TaskEditorUiState> = _uiState.asStateFlow()

    val tags: StateFlow<List<Tag>> = uiState
        .map { it.availableTags }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedTagIds: StateFlow<Set<Long>> = uiState
        .map { it.selectedTagIds }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _navigationEvent = Channel<Unit>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    // Long.MIN_VALUE = "never loaded anything yet"; -1L = "already in new-task draft"
    private var editingTaskId: Long = Long.MIN_VALUE
    // Preserved so saving an edit doesn't overwrite the original creation timestamp
    private var originalCreatedAt: Long = System.currentTimeMillis()

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
            if (editingTaskId == -1L) return
            originalCreatedAt = System.currentTimeMillis()
            _uiState.update { current ->
                TaskEditorUiState(availableTags = current.availableTags).withFormattedDates().withStaticOptions(false)
            }
            editingTaskId = -1L
            return
        }
        if (editingTaskId == id) return
        editingTaskId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getTaskByIdUseCase(id)?.let { task ->
                originalCreatedAt = task.createdAt
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
                        recurrenceRule = task.recurrenceRule,
                        isLoading = false
                    ).withFormattedDates().withStaticOptions(true)
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
                        recurrenceRule = if (turningOff) RecurrenceRule.NONE else it.recurrenceRule
                    ).withFormattedDates()
                }
            }
            is TaskEditorEvent.ToggleTime ->
                _uiState.update { it.copy(hasTime = !it.hasTime).withFormattedDates() }
            is TaskEditorEvent.ToggleUrgent ->
                _uiState.update { it.copy(isUrgent = !it.isUrgent) }
            is TaskEditorEvent.ToggleFlagged ->
                _uiState.update { it.copy(isFlagged = !it.isFlagged) }
            is TaskEditorEvent.PriorityChanged ->
                _uiState.update { it.copy(priority = event.priority).withPriorityOptions() }
            is TaskEditorEvent.DueDateChanged ->
                _uiState.update { state ->
                    val newDueDate = dateFormatter.updateDate(state.dueDate, event.dateMillis)
                    state.copy(dueDate = newDueDate, showDatePicker = false).withFormattedDates()
                }
            is TaskEditorEvent.TimeChanged ->
                _uiState.update { state ->
                    val newDueDate = dateFormatter.updateTime(state.dueDate, event.hour, event.minute)
                    state.copy(dueDate = newDueDate, showTimePicker = false).withFormattedDates()
                }
            is TaskEditorEvent.RecurrenceChanged ->
                _uiState.update { it.copy(recurrenceRule = event.rule).withDerivedFlags() }
            is TaskEditorEvent.ToggleRecurrence ->
                _uiState.update { state ->
                    val next = if (state.recurrenceRule.isRecurring) RecurrenceRule.NONE
                               else RecurrenceRule(RecurrenceUnit.DAYS, 1)
                    state.copy(recurrenceRule = next).withDerivedFlags()
                }
            is TaskEditorEvent.IncrementInterval ->
                _uiState.update { state ->
                    state.copy(
                        recurrenceRule = state.recurrenceRule.copy(interval = state.recurrenceRule.interval + 1)
                    ).withDerivedFlags()
                }
            is TaskEditorEvent.DecrementInterval ->
                _uiState.update { state ->
                    val r = state.recurrenceRule
                    if (r.interval > 1) state.copy(recurrenceRule = r.copy(interval = r.interval - 1)).withDerivedFlags()
                    else state
                }
            is TaskEditorEvent.RecurrenceUnitSelected ->
                _uiState.update { state ->
                    state.copy(recurrenceRule = state.recurrenceRule.copy(unit = event.unit)).withDerivedFlags()
                }
            is TaskEditorEvent.TagClicked -> {
                val tagId = event.tagId
                _uiState.update { state ->
                    val updated = if (tagId in state.selectedTagIds) state.selectedTagIds - tagId
                                  else state.selectedTagIds + tagId
                    state.copy(selectedTagIds = updated)
                }
            }
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

    private fun TaskEditorUiState.withDerivedFlags(): TaskEditorUiState =
        copy(canDecrementInterval = recurrenceRule.interval > 1)

    private fun TaskEditorUiState.withFormattedDates(): TaskEditorUiState {
        return copy(
            formattedDate = if (hasDate) dateFormatter.formatDate(dueDate) else null,
            formattedTime = if (hasTime) dateFormatter.formatTime(dueDate) else null,
            datePickerUtcMillis = dateFormatter.toUtcMidnight(dueDate),
            timePickerHour = dateFormatter.getHour(dueDate),
            timePickerMinute = dateFormatter.getMinute(dueDate),
        )
    }

    private fun TaskEditorUiState.withPriorityOptions(): TaskEditorUiState {
        return copy(
            priorityOptions = Priority.entries.map { p ->
                PriorityOptionUiModel(
                    priority = p,
                    label = UiText.StringResource(when (p) {
                        Priority.NONE -> R.string.priority_none
                        Priority.LOW -> R.string.priority_low
                        Priority.MEDIUM -> R.string.priority_medium
                        Priority.HIGH -> R.string.priority_high
                    }),
                    isSelected = priority == p,
                    testTag = when (p) {
                        Priority.NONE -> TestTags.SEGMENT_PRIORITY_NONE
                        Priority.LOW -> TestTags.SEGMENT_PRIORITY_LOW
                        Priority.MEDIUM -> TestTags.SEGMENT_PRIORITY_MEDIUM
                        Priority.HIGH -> TestTags.SEGMENT_PRIORITY_HIGH
                    }
                )
            }
        )
    }

    private fun TaskEditorUiState.withStaticOptions(isEditing: Boolean): TaskEditorUiState {
        return copy(
            screenTitle = UiText.StringResource(if (isEditing) R.string.editor_title_edit else R.string.editor_title_new),
            recurrenceUnitOptions = RecurrenceUnit.entries.filter { it != RecurrenceUnit.NONE }.map { unit ->
                RecurrenceUnitOptionUiModel(
                    unit = unit,
                    label = UiText.StringResource(when (unit) {
                        RecurrenceUnit.NONE -> 0 // Won't happen
                        RecurrenceUnit.HOURS -> R.string.recurrence_unit_hours
                        RecurrenceUnit.DAYS -> R.string.recurrence_unit_days
                        RecurrenceUnit.WEEKS -> R.string.recurrence_unit_weeks
                        RecurrenceUnit.MONTHS -> R.string.recurrence_unit_months
                    })
                )
            }
        ).withPriorityOptions()
    }

    private fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            return
        }
        viewModelScope.launch {
            val isEditing = editingTaskId > 0L
            val task = Task(
                id = if (isEditing) editingTaskId else 0L,
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
                recurrenceRule = state.recurrenceRule,
                createdAt = if (isEditing) originalCreatedAt else System.currentTimeMillis()
            )

            if (isEditing) {
                updateTaskUseCase(task)
                notificationScheduler.cancel(editingTaskId)
            } else {
                addTaskUseCase(task)
            }

            task.dueDate?.let { due ->
                if (due > System.currentTimeMillis()) notificationScheduler.schedule(task)
            }

            _navigationEvent.send(Unit)
        }
    }
}
