package com.gustavo.brilhante.taskeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavo.brilhante.domain.time.ClockProvider
import com.gustavo.brilhante.domain.usecase.AddTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTaskByIdUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.ChecklistItem
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
    private val dateFormatter: DateFormatter,
    private val clockProvider: ClockProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TaskEditorUiState(dueDate = clockProvider.currentTimeMillis())
    )
    val uiState: StateFlow<TaskEditorUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<Unit>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    // Long.MIN_VALUE = "never loaded anything yet"; -1L = "already in new-task draft"
    private var editingTaskId: Long = Long.MIN_VALUE
    // Preserved so saving an edit doesn't overwrite the original creation timestamp
    private var originalCreatedAt: Long = clockProvider.currentTimeMillis()

    private var availableTags: List<Tag> = emptyList()
    private var selectedTagIds: Set<Long> = emptySet()

    private val recurrenceUnitOptions = RecurrenceUnit.entries
        .filter { it != RecurrenceUnit.NONE }
        .map { unit ->
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

    init {
        observeAvailableTags()
    }

    private fun observeAvailableTags() {
        viewModelScope.launch {
            getTagsUseCase().collect { tags ->
                availableTags = tags
                _uiState.update { it.withTags() }
            }
        }
    }

    fun loadTask(id: Long) {
        val resolvedId = TaskEditorArgsResolver.resolveId(id)
        val mode = TaskEditorArgsResolver.resolveMode(id)

        if (resolvedId <= 0L) {
            if (editingTaskId == -1L) return
            originalCreatedAt = clockProvider.currentTimeMillis()
            _uiState.update {
                TaskEditorUiState(
                    dueDate = clockProvider.currentTimeMillis(),
                ).withTags().withDateSection().withPriorityOptions().withScreenTitle(mode)
            }
            editingTaskId = -1L
            return
        }
        if (editingTaskId == resolvedId) return
        editingTaskId = resolvedId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getTaskByIdUseCase(resolvedId)?.let { task ->
                originalCreatedAt = task.createdAt
                selectedTagIds = task.tagIds.toSet()
                _uiState.update {
                    TaskEditorUiState(
                        title = task.title,
                        notes = task.notes,
                        url = task.url,
                        dueDate = task.dueDate ?: clockProvider.currentTimeMillis(),
                        isUrgent = task.isUrgent,
                        priority = task.priority,
                        isFlagged = task.isFlagged,
                        isCompleted = task.isCompleted,
                        recurrenceRule = task.recurrenceRule,
                        isLoading = false,
                        checklistItems = task.checklistItems.map {
                            ChecklistItemUiModel(id = it.id, text = it.text, isChecked = it.isChecked)
                        }
                    ).withTags()
                        .withDateSection(hasDate = task.dueDate != null, hasTime = task.hasTime)
                        .withPriorityOptions()
                        .withScreenTitle(mode)
                }
            } ?: _uiState.update { it.copy(isLoading = false) }
        }
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
                val turningOff = _uiState.value.dateSection.hasDate
                _uiState.update {
                    val newHasDate = !it.dateSection.hasDate
                    val newHasTime = if (turningOff) false else it.dateSection.hasTime
                    val newRecurrenceRule = if (turningOff) RecurrenceRule.NONE else it.recurrenceRule
                    it.copy(recurrenceRule = newRecurrenceRule)
                        .withDateSection(hasDate = newHasDate, hasTime = newHasTime)
                }
            }
            is TaskEditorEvent.ToggleTime ->
                _uiState.update { it.withDateSection(hasTime = !it.dateSection.hasTime) }
            is TaskEditorEvent.ToggleUrgent ->
                _uiState.update { it.copy(isUrgent = !it.isUrgent) }
            is TaskEditorEvent.ToggleFlagged ->
                _uiState.update { it.copy(isFlagged = !it.isFlagged) }
            is TaskEditorEvent.PriorityChanged ->
                _uiState.update { it.copy(priority = event.priority).withPriorityOptions() }
            is TaskEditorEvent.DueDateChanged ->
                _uiState.update { state ->
                    val newDueDate = dateFormatter.updateDate(state.dueDate, event.dateMillis)
                    state.copy(dueDate = newDueDate)
                        .withDateSection()
                        .withDialogState(null)
                }
            is TaskEditorEvent.TimeChanged ->
                _uiState.update { state ->
                    val newDueDate = dateFormatter.updateTime(state.dueDate, event.hour, event.minute)
                    state.copy(dueDate = newDueDate)
                        .withDateSection()
                        .withDialogState(null)
                }
            is TaskEditorEvent.ToggleRecurrence ->
                _uiState.update { state ->
                    val next = if (state.recurrenceRule.isRecurring) RecurrenceRule.NONE
                    else RecurrenceRule(RecurrenceUnit.DAYS, 1)
                    state.copy(recurrenceRule = next).withDateSection()
                }
            is TaskEditorEvent.IncrementInterval ->
                _uiState.update { state ->
                    state.copy(
                        recurrenceRule = state.recurrenceRule.copy(interval = state.recurrenceRule.interval + 1)
                    ).withDateSection()
                }
            is TaskEditorEvent.DecrementInterval ->
                _uiState.update { state ->
                    val r = state.recurrenceRule
                    if (r.interval > 1) state.copy(recurrenceRule = r.copy(interval = r.interval - 1)).withDateSection()
                    else state
                }
            is TaskEditorEvent.RecurrenceUnitSelected ->
                _uiState.update { state ->
                    state.copy(recurrenceRule = state.recurrenceRule.copy(unit = event.unit)).withDateSection()
                }
            is TaskEditorEvent.TagClicked -> {
                val tagId = event.tagId
                selectedTagIds = if (tagId in selectedTagIds) selectedTagIds - tagId
                else selectedTagIds + tagId
                _uiState.update { it.withTags() }
            }
            is TaskEditorEvent.AddChecklistItem ->
                _uiState.update { it.copy(checklistItems = it.checklistItems + ChecklistItemUiModel()) }
            is TaskEditorEvent.RemoveChecklistItem ->
                _uiState.update {
                    if (event.index in it.checklistItems.indices) {
                        it.copy(checklistItems = it.checklistItems.toMutableList().also { list -> list.removeAt(event.index) })
                    } else {
                        it
                    }
                }
            is TaskEditorEvent.ChecklistItemTextChanged ->
                _uiState.update {
                    it.copy(checklistItems = it.checklistItems.mapIndexed { i, item ->
                        if (i == event.index) item.copy(text = event.text) else item
                    })
                }
            is TaskEditorEvent.ChecklistItemChecked ->
                _uiState.update {
                    it.copy(checklistItems = it.checklistItems.mapIndexed { i, item ->
                        if (i == event.index) item.copy(isChecked = event.isChecked) else item
                    })
                }
            is TaskEditorEvent.ShowDialog ->
                _uiState.update { it.withDialogState(event.dialog) }
            is TaskEditorEvent.DismissDialog ->
                _uiState.update { it.withDialogState(null) }
            is TaskEditorEvent.Save -> save()
        }
    }

    private fun TaskEditorUiState.withDateSection(
        hasDate: Boolean = dateSection.hasDate,
        hasTime: Boolean = dateSection.hasTime
    ): TaskEditorUiState {
        return copy(
            dateSection = DateSectionUiModel(
                hasDate = hasDate,
                formattedDate = if (hasDate) dateFormatter.formatDate(dueDate) else null,
                showTimeToggle = hasDate,
                hasTime = hasTime,
                formattedTime = if (hasTime) dateFormatter.formatTime(dueDate) else null,
                showRecurrence = hasDate,
                recurrenceUiModel = RecurrenceUiMapper.map(recurrenceRule, recurrenceUnitOptions)
            )
        )
    }

    private fun TaskEditorUiState.withTags(): TaskEditorUiState {
        return copy(
            tagSection = TagSectionUiModel(
                tags = availableTags.map { tag ->
                    TagItemUiModel(
                        id = tag.id,
                        name = tag.name,
                        color = tag.color,
                        isSelected = tag.id in selectedTagIds
                    )
                },
                showEmptyState = availableTags.isEmpty()
            )
        )
    }

    private fun TaskEditorUiState.withPriorityOptions(): TaskEditorUiState {
        return copy(
            priorityOptions = Priority.entries.map { p ->
                PriorityOptionUiModel(
                    priority = p,
                    label = UiText.StringResource(when (p) {
                        Priority.NONE -> R.string.none
                        Priority.LOW -> R.string.low
                        Priority.MEDIUM -> R.string.medium
                        Priority.HIGH -> R.string.high
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

    private fun TaskEditorUiState.withDialogState(activeDialog: ActiveDialog?): TaskEditorUiState {
        return copy(
            dialogState = TaskEditorDialogState(
                activeDialog = activeDialog,
                datePickerUtcMillis = dateFormatter.toUtcMidnight(dueDate),
                timePickerHour = dateFormatter.getHour(dueDate),
                timePickerMinute = dateFormatter.getMinute(dueDate)
            )
        )
    }

    private fun TaskEditorUiState.withScreenTitle(mode: TaskEditorMode): TaskEditorUiState {
        return copy(
            screenTitle = UiText.StringResource(
                if (mode == TaskEditorMode.EDIT) R.string.editor_title_edit
                else R.string.editor_title_new
            )
        )
    }

    private fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = UiText.StringResource(R.string.error_title_required)) }
            return
        }
        viewModelScope.launch {
            val isEditing = editingTaskId > 0L
            val now = clockProvider.currentTimeMillis()
            val task = state.toTask(
                id = if (isEditing) editingTaskId else 0L,
                createdAt = if (isEditing) originalCreatedAt else now,
                tagIds = selectedTagIds.toList()
            )

            if (isEditing) {
                updateTaskUseCase(task)
                notificationScheduler.cancel(editingTaskId)
            } else {
                addTaskUseCase(task)
            }

            task.dueDate?.let { due ->
                if (due > now) notificationScheduler.schedule(task)
            }

            _navigationEvent.send(Unit)
        }
    }
}
