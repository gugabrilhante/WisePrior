package com.gustavo.brilhante.taskmanager.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.data.models.Task
import com.gustavo.brilhante.taskmanager.presentation.TaskManagerViewModel

@Composable
fun TaskManagerScreen(taskViewModel: TaskManagerViewModel) {
    val tasks by taskViewModel.tasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Task Manager") })
        },
        content = { padding ->
            Column (
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                TaskList(tasks = tasks)
                Spacer(modifier = Modifier.height(16.dp))
                AddTaskForm(onAddTask = { taskViewModel.addTask(it) })
            }
        }
    )
}

@Composable
fun TaskList(tasks: List<Task>) {
    LazyColumn {
        items(items = tasks) { task ->
            Text(text = task.title)
        }
    }
}

@Composable
fun AddTaskForm(onAddTask: (Task) -> Unit) {
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val dueDate = remember { mutableStateOf(System.currentTimeMillis()) }

    Column {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Title") }
        )
        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Description") }
        )
        Button(
            onClick = {
                val newTask = Task(
                    title = title.value,
                    description = description.value,
                    dueDate = dueDate.value
                )
                onAddTask(newTask)
                title.value = ""
                description.value = ""
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add Task")
        }
    }
}