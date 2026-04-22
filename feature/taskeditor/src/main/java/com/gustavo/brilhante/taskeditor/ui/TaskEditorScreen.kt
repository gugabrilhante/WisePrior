package com.gustavo.brilhante.taskeditor.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.RecurrenceType
import com.gustavo.brilhante.taskeditor.presentation.TaskEditorEvent
import com.gustavo.brilhante.taskeditor.presentation.TaskEditorViewModel
import com.gustavo.brilhante.ui.SectionHeader
import com.gustavo.brilhante.ui.ToggleRow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditorScreen(
    taskId: Long?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load task or reset state on screen entry
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId ?: -1L)
    }

    // ✅ FIX: Channel-based one-shot navigation — no boolean flag in UiState,
    // no LaunchedEffect(boolean) risk. Collects exactly once per emission.
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { onBack() }
    }

    // DatePicker dialog
    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dueDate
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onEvent(TaskEditorEvent.HideDatePicker) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onEvent(TaskEditorEvent.DueDateChanged(it))
                    } ?: viewModel.onEvent(TaskEditorEvent.HideDatePicker)
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TaskEditorEvent.HideDatePicker) }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = true)
        }
    }

    // TimePicker dialog (Material 3 has no TimePickerDialog, so we wrap in AlertDialog)
    if (uiState.showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = uiState.dueDate }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TaskEditorEvent.HideTimePicker) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(
                        TaskEditorEvent.TimeChanged(timePickerState.hour, timePickerState.minute)
                    )
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TaskEditorEvent.HideTimePicker) }) {
                    Text("Cancel")
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (taskId != null) "Edit Reminder" else "New Reminder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.onEvent(TaskEditorEvent.Save) }) {
                        Text("Done", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
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
                        placeholder = { Text("Title") },
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
                        placeholder = { Text("Notes") },
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
            SectionHeader("Date & Time")
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    ToggleRow(
                        label = "Date",
                        checked = uiState.hasDate,
                        onCheckedChange = { viewModel.onEvent(TaskEditorEvent.ToggleDate) },
                        icon = Icons.Filled.CalendarMonth,
                        supportingText = if (uiState.hasDate) {
                            SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                                .format(Date(uiState.dueDate))
                        } else null,
                        onRowClick = if (uiState.hasDate) {
                            { viewModel.onEvent(TaskEditorEvent.ShowDatePicker) }
                        } else null
                    )

                    if (uiState.hasDate) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ToggleRow(
                            label = "Time",
                            checked = uiState.hasTime,
                            onCheckedChange = { viewModel.onEvent(TaskEditorEvent.ToggleTime) },
                            icon = Icons.Filled.Schedule,
                            supportingText = if (uiState.hasTime) {
                                SimpleDateFormat("HH:mm", Locale.getDefault())
                                    .format(Date(uiState.dueDate))
                            } else null,
                            onRowClick = if (uiState.hasTime) {
                                { viewModel.onEvent(TaskEditorEvent.ShowTimePicker) }
                            } else null
                        )

                        // Recurrence
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        RecurrenceSelector(
                            selected = uiState.recurrenceType,
                            onSelect = { viewModel.onEvent(TaskEditorEvent.RecurrenceChanged(it)) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            // ── Details ───────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader("Details")
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    ToggleRow(
                        label = "Urgent",
                        checked = uiState.isUrgent,
                        onCheckedChange = { viewModel.onEvent(TaskEditorEvent.ToggleUrgent) },
                        icon = Icons.Filled.Warning
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ToggleRow(
                        label = "Flag",
                        checked = uiState.isFlagged,
                        onCheckedChange = { viewModel.onEvent(TaskEditorEvent.ToggleFlagged) },
                        icon = Icons.Filled.Flag
                    )
                }
            }

            // ── Priority ─────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader("Priority")
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
                                priority.name.lowercase().replaceFirstChar { it.uppercase() }
                            )
                        }
                    )
                }
            }

            // ── Tags ─────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader("Tags")
            var tagInput by remember { mutableStateOf("") }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    placeholder = { Text("Add tag") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (tagInput.isNotBlank()) {
                            TextButton(onClick = {
                                viewModel.onEvent(TaskEditorEvent.TagAdded(tagInput.trim()))
                                tagInput = ""
                            }) { Text("Add") }
                        }
                    }
                )
                if (uiState.tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.tags.forEach { tag ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.onEvent(TaskEditorEvent.TagRemoved(tag)) },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }

            // ── URL ───────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionHeader("URL")
            OutlinedTextField(
                value = uiState.url,
                onValueChange = { viewModel.onEvent(TaskEditorEvent.UrlChanged(it)) },
                placeholder = { Text("https://") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceSelector(
    selected: RecurrenceType,
    onSelect: (RecurrenceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Repeat",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        val options = RecurrenceType.entries
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = selected == type,
                    onClick = { onSelect(type) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    label = {
                        Text(
                            when (type) {
                                RecurrenceType.NONE -> "None"
                                RecurrenceType.DAILY -> "Daily"
                                RecurrenceType.WEEKLY -> "Weekly"
                                RecurrenceType.MONTHLY -> "Monthly"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }
    }
}
