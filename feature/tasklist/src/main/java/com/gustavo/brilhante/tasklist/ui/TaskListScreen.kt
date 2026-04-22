package com.gustavo.brilhante.tasklist.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.tasklist.presentation.TaskListViewModel
import com.gustavo.brilhante.ui.EmptyState
import com.gustavo.brilhante.ui.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Reminders",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = uiState.tasks.isEmpty() && !uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyState(
                    title = "No Reminders",
                    subtitle = "Tap + to add your first reminder"
                )
            }

            AnimatedVisibility(
                visible = uiState.tasks.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.tasks,
                        key = { task -> task.id }
                    ) { task ->
                        SwipeToDeleteContainer(
                            task = task,
                            onDelete = { viewModel.deleteTask(task) }
                        ) {
                            TaskCard(
                                task = task,
                                onClick = { onEditTask(task) },
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteContainer(
    task: Task,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState()
    val currentValue = state.currentValue

    LaunchedEffect(currentValue) {
        if (currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = state,
        backgroundContent = {},
        content = { content() }
    )
}
