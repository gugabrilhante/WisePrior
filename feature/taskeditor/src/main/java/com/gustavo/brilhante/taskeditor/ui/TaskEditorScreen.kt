package com.gustavo.brilhante.taskeditor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.taskeditor.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.taskeditor.presentation.TaskEditorEvent
import com.gustavo.brilhante.taskeditor.presentation.TaskEditorViewModel
import com.gustavo.brilhante.ui.SectionHeader
import com.gustavo.brilhante.ui.TagChip
import com.gustavo.brilhante.ui.ToggleRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.datePickerUtcMillis
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onEvent(TaskEditorEvent.HideDatePicker) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onEvent(TaskEditorEvent.DueDateChanged(it))
                    } ?: viewModel.onEvent(TaskEditorEvent.HideDatePicker)
                }) { Text(stringResource(R.string.editor_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TaskEditorEvent.HideDatePicker) }) {
                    Text(stringResource(R.string.editor_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = true)
        }
    }

    if (uiState.showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.timePickerHour,
            initialMinute = uiState.timePickerMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TaskEditorEvent.HideTimePicker) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(
                        TaskEditorEvent.TimeChanged(timePickerState.hour, timePickerState.minute)
                    )
                }) { Text(stringResource(R.string.editor_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TaskEditorEvent.HideTimePicker) }) {
                    Text(stringResource(R.string.editor_cancel))
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (taskId != null) stringResource(R.string.editor_title_edit) else stringResource(R.string.editor_title_new)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.editor_back))
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.onEvent(TaskEditorEvent.Save) }) {
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
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Title + Notes ─────────────────────────────────────────────
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onEvent(TaskEditorEvent.TitleChanged(it)) },
                        placeholder = { Text(stringResource(R.string.editor_placeholder_title)) },
                        isError = uiState.titleError != null,
                        supportingText = uiState.titleError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onEvent(TaskEditorEvent.NotesChanged(it)) },
                        placeholder = { Text(stringResource(R.string.editor_placeholder_notes)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // ── Date & Time ───────────────────────────────────────────────
            SectionHeader(stringResource(R.string.editor_section_datetime))
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    ToggleRow(
                        label = stringResource(R.string.editor_label_date),
                        checked = uiState.hasDate,
                        onCheckedChange = { viewModel.onEvent(TaskEditorEvent.ToggleDate) },
                        icon = Icons.Filled.CalendarMonth,
                        supportingText = if (uiState.hasDate) uiState.formattedDate else null,
                        onRowClick = if (uiState.hasDate) {
                            { viewModel.onEvent(TaskEditorEvent.ShowDatePicker) }
                        } else null
                    )

                    if (uiState.hasDate) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ToggleRow(
                            label = stringResource(R.string.editor_label_time),
                            checked = uiState.hasTime,
                            onCheckedChange = { viewModel.onEvent(TaskEditorEvent.ToggleTime) },
                            icon = Icons.Filled.Schedule,
                            supportingText = if (uiState.hasTime) uiState.formattedTime else null,
                            onRowClick = if (uiState.hasTime) {
                                { viewModel.onEvent(TaskEditorEvent.ShowTimePicker) }
                            } else null
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        RecurrenceSelector(
                            rule = uiState.recurrenceRule,
                            onRuleChange = { viewModel.onEvent(TaskEditorEvent.RecurrenceChanged(it)) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ── Details ───────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader(stringResource(R.string.editor_section_details))
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    ToggleRow(
                        label = stringResource(R.string.editor_label_urgent),
                        checked = uiState.isUrgent,
                        onCheckedChange = { viewModel.onEvent(TaskEditorEvent.ToggleUrgent) },
                        icon = Icons.Filled.Warning
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ToggleRow(
                        label = stringResource(R.string.editor_label_flag),
                        checked = uiState.isFlagged,
                        onCheckedChange = { viewModel.onEvent(TaskEditorEvent.ToggleFlagged) },
                        icon = Icons.Filled.Flag
                    )
                }
            }

            // ── Priority ─────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader(stringResource(R.string.editor_section_priority))
            val priorities = Priority.entries
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                priorities.forEachIndexed { index, priority ->
                    SegmentedButton(
                        selected = uiState.priority == priority,
                        onClick = { viewModel.onEvent(TaskEditorEvent.PriorityChanged(priority)) },
                        shape = SegmentedButtonDefaults.itemShape(index, priorities.size),
                        label = {
                            Text(
                                when (priority) {
                                    Priority.NONE -> stringResource(R.string.none)
                                    Priority.LOW -> stringResource(R.string.low)
                                    Priority.MEDIUM -> stringResource(R.string.medium)
                                    Priority.HIGH -> stringResource(R.string.high)
                                }
                            )
                        }
                    )
                }
            }

            // ── Tags ─────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader(stringResource(R.string.editor_section_tags))
            if (uiState.availableTags.isEmpty() && uiState.selectedTagIds.isEmpty()) {
                Column(
                    modifier = Modifier
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
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    uiState.availableTags.forEach { tag ->
                        val isSelected = uiState.selectedTagIds.contains(tag.id)
                        TagChip(
                            tag = tag,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) viewModel.onTagRemoved(tag.id)
                                else viewModel.onTagSelected(tag.id)
                            }
                        )
                    }
                }
            }

            // ── URL ───────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader(stringResource(R.string.editor_section_url))
            OutlinedTextField(
                value = uiState.url,
                onValueChange = { viewModel.onEvent(TaskEditorEvent.UrlChanged(it)) },
                placeholder = { Text(stringResource(R.string.editor_placeholder_url)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Recurrence selector ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceSelector(
    rule: RecurrenceRule,
    onRuleChange: (RecurrenceRule) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Toggle row
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
                checked = rule.isRecurring,
                onCheckedChange = { enabled ->
                    onRuleChange(
                        if (enabled) RecurrenceRule(RecurrenceUnit.DAYS, 1)
                        else RecurrenceRule.NONE
                    )
                }
            )
        }

        if (rule.isRecurring) {
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
                // Decrement button
                IconButton(
                    onClick = {
                        if (rule.interval > 1) onRuleChange(rule.copy(interval = rule.interval - 1))
                    },
                    enabled = rule.interval > 1
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.editor_recurrence_decrease))
                }
                Text(
                    text = rule.interval.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                // Increment button
                IconButton(
                    onClick = { onRuleChange(rule.copy(interval = rule.interval + 1)) }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.editor_recurrence_increase))
                }
                Spacer(Modifier.width(4.dp))
                RecurrenceUnitDropdown(
                    selected = rule.unit,
                    onSelect = { onRuleChange(rule.copy(unit = it)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceUnitDropdown(
    selected: RecurrenceUnit,
    onSelect: (RecurrenceUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val units = RecurrenceUnit.entries.filter { it != RecurrenceUnit.NONE }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.label(),
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
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.label()) },
                    onClick = { onSelect(unit); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun RecurrenceUnit.label(): String = when (this) {
    RecurrenceUnit.NONE -> ""
    RecurrenceUnit.HOURS -> stringResource(R.string.recurrence_unit_hours)
    RecurrenceUnit.DAYS -> stringResource(R.string.recurrence_unit_days)
    RecurrenceUnit.WEEKS -> stringResource(R.string.recurrence_unit_weeks)
    RecurrenceUnit.MONTHS -> stringResource(R.string.recurrence_unit_months)
}
