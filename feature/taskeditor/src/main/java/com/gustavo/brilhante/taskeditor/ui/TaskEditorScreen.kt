package com.gustavo.brilhante.taskeditor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.ui.TestTags
import com.gustavo.brilhante.taskeditor.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustavo.brilhante.ui.UiText
import androidx.compose.ui.tooling.preview.Preview
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.taskeditor.presentation.ActiveDialog
import com.gustavo.brilhante.taskeditor.presentation.ChecklistItemUiModel
import com.gustavo.brilhante.taskeditor.presentation.DateSectionUiModel
import com.gustavo.brilhante.taskeditor.presentation.PriorityOptionUiModel
import com.gustavo.brilhante.taskeditor.presentation.RecurrenceUiModel
import com.gustavo.brilhante.taskeditor.presentation.RecurrenceUnitOptionUiModel
import com.gustavo.brilhante.taskeditor.presentation.TagSectionUiModel
import com.gustavo.brilhante.taskeditor.presentation.TaskEditorArgsResolver
import com.gustavo.brilhante.taskeditor.presentation.TaskEditorDialogState
import com.gustavo.brilhante.taskeditor.presentation.TaskEditorEvent
import com.gustavo.brilhante.taskeditor.presentation.TaskEditorUiState
import com.gustavo.brilhante.taskeditor.presentation.TaskEditorViewModel
import com.gustavo.brilhante.ui.SectionHeader
import com.gustavo.brilhante.ui.TagChip
import com.gustavo.brilhante.ui.ToggleRow

@Preview(showBackground = true)
@Composable
private fun TaskEditorContentPreview() {
    MaterialTheme {
        TaskEditorContent(
            uiState = TaskEditorUiState(
                title = "Study for the exam",
                notes = "Chapters 1 to 5",
                screenTitle = UiText.DynamicString("Edit Task"),
                priorityOptions = listOf(
                    PriorityOptionUiModel(Priority.NONE, UiText.DynamicString("None"), true, "none"),
                    PriorityOptionUiModel(Priority.LOW, UiText.DynamicString("Low"), false, "low"),
                    PriorityOptionUiModel(Priority.MEDIUM, UiText.DynamicString("Med"), false, "medium"),
                    PriorityOptionUiModel(Priority.HIGH, UiText.DynamicString("High"), false, "high")
                )
            ),
            onEvent = {},
            onBack = {}
        )
    }
}

@Composable
fun TaskEditorScreen(
    taskId: Long?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId ?: -1L)
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { onBack() }
    }

    TaskEditorContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditorContent(
    uiState: TaskEditorUiState,
    onEvent: (TaskEditorEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TaskEditorDialogs(
        dialogState = uiState.dialogState,
        onEvent = onEvent
    )

    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_TASK_EDITOR),
        topBar = {
            TopAppBar(
                title = { Text(uiState.screenTitle.asString()) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(TestTags.BTN_TASK_EDITOR_BACK)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.editor_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onEvent(TaskEditorEvent.Save) },
                        modifier = Modifier.testTag(TestTags.BTN_TASK_EDITOR_DONE)
                    ) {
                        Text(stringResource(R.string.editor_done), style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Title + Notes ─────────────────────────────────────────────
            EditorMainSection(
                title = uiState.title,
                notes = uiState.notes,
                titleError = uiState.titleError,
                onTitleChange = { onEvent(TaskEditorEvent.TitleChanged(it)) },
                onNotesChange = { onEvent(TaskEditorEvent.NotesChanged(it)) }
            )

            // ── Checklist ─────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader(
                stringResource(R.string.editor_section_checklist),
                modifier = Modifier.testTag(TestTags.SECTION_TASK_EDITOR_CHECKLIST)
            )
            ChecklistSection(
                items = uiState.checklistItems,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            // ── Date & Time ───────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader(stringResource(R.string.editor_section_datetime), modifier = Modifier.testTag(TestTags.SECTION_TASK_EDITOR_DATETIME))
            DateTimeSection(
                dateSection = uiState.dateSection,
                onEvent = onEvent
            )

            // ── Details ───────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader(stringResource(R.string.editor_section_details), modifier = Modifier.testTag(TestTags.SECTION_TASK_EDITOR_DETAILS))
            DetailsSection(
                isUrgent = uiState.isUrgent,
                isFlagged = uiState.isFlagged,
                onEvent = onEvent
            )

            // ── Priority ─────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader(stringResource(R.string.editor_section_priority), modifier = Modifier.testTag(TestTags.SECTION_TASK_EDITOR_PRIORITY))
            PrioritySection(
                priorityOptions = uiState.priorityOptions,
                onPriorityChange = { onEvent(TaskEditorEvent.PriorityChanged(it)) }
            )

            // ── Tags ─────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader(stringResource(R.string.editor_section_tags), modifier = Modifier.testTag(TestTags.SECTION_TASK_EDITOR_TAGS))
            TagsSection(
                tagSection = uiState.tagSection,
                onTagClick = { onEvent(TaskEditorEvent.TagClicked(it)) }
            )

            // ── URL ───────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader(stringResource(R.string.editor_section_url), modifier = Modifier.testTag(TestTags.SECTION_TASK_EDITOR_URL))
            OutlinedTextField(
                value = uiState.url,
                onValueChange = { onEvent(TaskEditorEvent.UrlChanged(it)) },
                placeholder = { Text(stringResource(R.string.editor_placeholder_url)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag(TestTags.INPUT_TASK_EDITOR_URL),
                singleLine = true
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorDialogs(
    dialogState: TaskEditorDialogState,
    onEvent: (TaskEditorEvent) -> Unit
) {
    when (dialogState.activeDialog) {
        ActiveDialog.DatePicker -> {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = dialogState.datePickerUtcMillis
            )
            DatePickerDialog(
                modifier = Modifier.testTag(TestTags.DIALOG_DATE_PICKER),
                onDismissRequest = { onEvent(TaskEditorEvent.DismissDialog) },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onEvent(TaskEditorEvent.DueDateChanged(it))
                        } ?: onEvent(TaskEditorEvent.DismissDialog)
                    }) { Text(stringResource(R.string.editor_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TaskEditorEvent.DismissDialog) }) {
                        Text(stringResource(R.string.editor_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState, showModeToggle = true)
            }
        }
        ActiveDialog.TimePicker -> {
            val timePickerState = rememberTimePickerState(
                initialHour = dialogState.timePickerHour,
                initialMinute = dialogState.timePickerMinute,
                is24Hour = true
            )
            AlertDialog(
                modifier = Modifier.testTag(TestTags.DIALOG_TIME_PICKER),
                onDismissRequest = { onEvent(TaskEditorEvent.DismissDialog) },
                confirmButton = {
                    TextButton(onClick = {
                        onEvent(
                            TaskEditorEvent.TimeChanged(timePickerState.hour, timePickerState.minute)
                        )
                    }) { Text(stringResource(R.string.editor_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TaskEditorEvent.DismissDialog) }) {
                        Text(stringResource(R.string.editor_cancel))
                    }
                },
                text = { TimePicker(state = timePickerState) }
            )
        }
        null -> {}
    }
}

@Composable
private fun EditorMainSection(
    title: String,
    notes: String,
    titleError: UiText?,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text(stringResource(R.string.editor_placeholder_title)) },
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it.asString()) } },
                modifier = Modifier.fillMaxWidth().testTag(TestTags.INPUT_TASK_EDITOR_TITLE),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text(stringResource(R.string.editor_placeholder_notes)) },
                modifier = Modifier.fillMaxWidth().testTag(TestTags.INPUT_TASK_EDITOR_NOTES),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun DateTimeSection(
    dateSection: DateSectionUiModel,
    onEvent: (TaskEditorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            ToggleRow(
                label = stringResource(R.string.editor_label_date),
                checked = dateSection.hasDate,
                onCheckedChange = { onEvent(TaskEditorEvent.ToggleDate) },
                icon = Icons.Filled.CalendarMonth,
                supportingText = dateSection.formattedDate,
                onRowClick = if (dateSection.hasDate) {
                    { onEvent(TaskEditorEvent.ShowDialog(ActiveDialog.DatePicker)) }
                } else null,
                testTag = TestTags.TOGGLE_TASK_DATE
            )

            if (dateSection.showTimeToggle) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ToggleRow(
                    label = stringResource(R.string.editor_label_time),
                    checked = dateSection.hasTime,
                    onCheckedChange = { onEvent(TaskEditorEvent.ToggleTime) },
                    icon = Icons.Filled.Schedule,
                    supportingText = dateSection.formattedTime,
                    onRowClick = if (dateSection.hasTime) {
                        { onEvent(TaskEditorEvent.ShowDialog(ActiveDialog.TimePicker)) }
                    } else null,
                    testTag = TestTags.TOGGLE_TASK_TIME
                )
            }

            if (dateSection.showRecurrence) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                RecurrenceSelector(
                    model = dateSection.recurrenceUiModel,
                    onEvent = onEvent,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailsSection(
    isUrgent: Boolean,
    isFlagged: Boolean,
    onEvent: (TaskEditorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            ToggleRow(
                label = stringResource(R.string.editor_label_urgent),
                checked = isUrgent,
                onCheckedChange = { onEvent(TaskEditorEvent.ToggleUrgent) },
                icon = Icons.Filled.Warning,
                testTag = TestTags.TOGGLE_TASK_URGENT
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ToggleRow(
                label = stringResource(R.string.editor_label_flag),
                checked = isFlagged,
                onCheckedChange = { onEvent(TaskEditorEvent.ToggleFlagged) },
                icon = Icons.Filled.Flag,
                testTag = TestTags.TOGGLE_TASK_FLAGGED
            )
        }
    }
}

@Composable
private fun PrioritySection(
    priorityOptions: List<PriorityOptionUiModel>,
    onPriorityChange: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        priorityOptions.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option.isSelected,
                onClick = { onPriorityChange(option.priority) },
                shape = SegmentedButtonDefaults.itemShape(index, priorityOptions.size),
                modifier = Modifier.testTag(option.testTag),
                label = { Text(option.label.asString()) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(
    tagSection: TagSectionUiModel,
    onTagClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tagSection.showEmptyState) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.no_tags_created),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.create_tags_sidebar),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.padding(horizontal = 16.dp)
        ) {
            tagSection.tags.forEach { tag ->
                TagChip(
                    tag = Tag(id = tag.id, name = tag.name, color = tag.color),
                    isSelected = tag.isSelected,
                    onClick = { onTagClick(tag.id) }
                )
            }
        }
    }
}


// ── Checklist section ─────────────────────────────────────────────────────────

@Composable
private fun ChecklistSection(
    items: List<ChecklistItemUiModel>,
    onEvent: (TaskEditorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column {
            items.forEachIndexed { index, item ->
                ChecklistItemRow(
                    item = item,
                    onCheckedChange = { onEvent(TaskEditorEvent.ChecklistItemChecked(index, it)) },
                    onTextChange = { onEvent(TaskEditorEvent.ChecklistItemTextChanged(index, it)) },
                    onDelete = { onEvent(TaskEditorEvent.RemoveChecklistItem(index)) },
                    modifier = Modifier.testTag("${TestTags.CHECKLIST_ITEM_ROW}_$index")
                )
                if (index < items.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
            if (items.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            TextButton(
                onClick = { onEvent(TaskEditorEvent.AddChecklistItem) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.BTN_CHECKLIST_ADD_ITEM)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.editor_checklist_add_item))
            }
        }
    }
}

@Composable
private fun ChecklistItemRow(
    item: ChecklistItemUiModel,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onCheckedChange(!item.isChecked) },
            modifier = Modifier.testTag(TestTags.CHECKLIST_ITEM_CHECKBOX)
        ) {
            Icon(
                imageVector = if (item.isChecked) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = stringResource(
                    if (item.isChecked) R.string.editor_checklist_item_mark_incomplete
                    else R.string.editor_checklist_item_mark_complete
                ),
                tint = if (item.isChecked) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedTextField(
            value = item.text,
            onValueChange = onTextChange,
            placeholder = { Text(stringResource(R.string.editor_checklist_item_placeholder)) },
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.CHECKLIST_ITEM_TEXT_INPUT),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            textStyle = if (item.isChecked)
                MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            else MaterialTheme.typography.bodyLarge
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag(TestTags.BTN_CHECKLIST_ITEM_DELETE)
        ) {
            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.editor_checklist_item_delete))
        }
    }
}

// ── Recurrence selector ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceSelector(
    model: RecurrenceUiModel,
    onEvent: (TaskEditorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.editor_recurrence_repeat),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Switch(
                checked = model.isRecurring,
                onCheckedChange = { onEvent(TaskEditorEvent.ToggleRecurrence) },
                modifier = Modifier.testTag(TestTags.TOGGLE_TASK_RECURRENCE)
            )
        }

        if (model.isRecurring) {
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.editor_recurrence_every),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = { onEvent(TaskEditorEvent.DecrementInterval) },
                    enabled = model.canDecrement,
                    modifier = Modifier.testTag(TestTags.BTN_TASK_EDITOR_RECURRENCE_DECREMENT)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.editor_recurrence_decrease))
                }
                Text(
                    text = model.intervalLabel,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(
                    onClick = { onEvent(TaskEditorEvent.IncrementInterval) },
                    modifier = Modifier.testTag(TestTags.BTN_TASK_EDITOR_RECURRENCE_INCREMENT)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.editor_recurrence_increase))
                }
                Spacer(Modifier.width(4.dp))
                RecurrenceUnitDropdown(
                    selectedLabel = model.selectedUnitLabel,
                    options = model.unitOptions,
                    onSelect = { onEvent(TaskEditorEvent.RecurrenceUnitSelected(it)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceUnitDropdown(
    selectedLabel: UiText,
    options: List<RecurrenceUnitOptionUiModel>,
    onSelect: (RecurrenceUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.testTag(TestTags.DROPDOWN_RECURRENCE_UNIT)
    ) {
        OutlinedTextField(
            value = selectedLabel.asString(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .width(140.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label.asString()) },
                    onClick = { onSelect(option.unit); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
