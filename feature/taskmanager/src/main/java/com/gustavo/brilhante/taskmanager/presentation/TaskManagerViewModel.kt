package com.gustavo.brilhante.taskmanager.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavo.brilhante.ui.DateFormatter
import com.gustavo.brilhante.data.models.Task
import com.gustavo.brilhante.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class TaskManagerViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val dateFormatter: DateFormatter
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    val formattedDates: StateFlow<Map<Int, String>> = _tasks.map { list ->
        list.mapIndexed { index, task -> index to dateFormatter.formatShortDate(task.dueDate) }.toMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch {
            repository.getAllTasksFlow().collect { tasks ->
                _tasks.value = tasks
            }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }
}
