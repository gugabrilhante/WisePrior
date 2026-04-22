package com.gustavo.brilhante.taskmanager.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustavo.brilhante.data.models.Task
import com.gustavo.brilhante.data.repository.MockTaskRepository
import com.gustavo.brilhante.taskmanager.presentation.TaskManagerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Preview
@Composable
fun PreviewTaskScreen() {
    TaskManagerScreen(taskViewModel = TaskManagerViewModel(MockTaskRepository()))
}

@Composable
fun TaskManagerScreen(taskViewModel: TaskManagerViewModel) {
    val tasks by taskViewModel.tasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lembretes", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                backgroundColor = Color(0xFF6200EE),
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TaskList(tasks = tasks)
            Spacer(modifier = Modifier.height(16.dp))
            AddTaskForm(onAddTask = { taskViewModel.addTask(it) })
        }
    }
}

@Composable
fun TaskList(tasks: List<Task>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tasks) { task ->
            TaskCard(task)
        }
    }
}

@Composable
fun TaskCard(task: Task) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp,
        backgroundColor = Color(0xFFEDE7F6)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = task.description, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Prazo: ${formatDate(task.dueDate)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AddTaskForm(onAddTask: (Task) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val dueDate = System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (title.isNotEmpty() && description.isNotEmpty()) {
                        val newTask = Task(title, description, dueDate)
                        onAddTask(newTask)
                        title = ""
                        description = ""
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Adicionar")
            }
        }
    }
}

fun formatDate(timeMillis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timeMillis))
}

