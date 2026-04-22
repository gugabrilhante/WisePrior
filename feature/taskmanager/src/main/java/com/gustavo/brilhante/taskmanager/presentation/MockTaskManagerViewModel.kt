package com.gustavo.brilhante.taskmanager.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavo.brilhante.data.models.Task
import com.gustavo.brilhante.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MockTaskManagerViewModel @Inject constructor() : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        viewModelScope.launch {
            _tasks.value = mockTasks()
        }
    }

    fun mockTasks() = listOf(
        Task("task 1", "description 1", 1L),
        Task("task 2", "very looooooooong description", 1333333L),
        Task("task 3", "veryyyyyy looong", 1444L),
        Task("task 4", "helllooooo", 10495L),
    )

    fun addTask(task: Task) {
        viewModelScope.launch {
        }
    }
}
