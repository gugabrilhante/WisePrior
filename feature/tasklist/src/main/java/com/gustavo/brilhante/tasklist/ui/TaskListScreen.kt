package com.gustavo.brilhante.tasklist.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.tasklist.model.TaskCollection
import com.gustavo.brilhante.tasklist.presentation.TaskListUiState
import com.gustavo.brilhante.tasklist.presentation.TaskListViewModel
import com.gustavo.brilhante.ui.EmptyState
import com.gustavo.brilhante.ui.TaskCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isExpandedWidth = LocalConfiguration.current.screenWidthDp >= 600
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(isExpandedWidth) {
        if (isExpandedWidth) drawerState.close()
    }

    val onCollectionSelected: (TaskCollection) -> Unit = { collection ->
        viewModel.onCollectionSelected(collection)
        if (!isExpandedWidth) scope.launch { drawerState.close() }
    }

    val sidebarContent: @Composable () -> Unit = {
        TaskSidebarContent(
            selectedCollection = uiState.selectedCollection,
            counts = uiState.collectionCounts,
            tags = uiState.tags,
            tagCounts = uiState.tagCounts,
            onCollectionSelected = onCollectionSelected,
            onAddTag = viewModel::showAddTag,
            onEditTag = viewModel::showEditTag
        )
    }

    if (isExpandedWidth) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(modifier = Modifier.width(260.dp)) { sidebarContent() }
            },
            modifier = modifier
        ) {
            TaskListContent(
                uiState = uiState,
                showMenuButton = false,
                onMenuClick = {},
                onAddTask = onAddTask,
                onEditTask = onEditTask,
                onDeleteTask = viewModel::deleteTask
            )
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { ModalDrawerSheet { sidebarContent() } },
            modifier = modifier
        ) {
            TaskListContent(
                uiState = uiState,
                showMenuButton = true,
                onMenuClick = { scope.launch { drawerState.open() } },
                onAddTask = onAddTask,
                onEditTask = onEditTask,
                onDeleteTask = viewModel::deleteTask
            )
        }
    }

    // Tag editor dialog — shown on top of either layout
    if (uiState.showTagEditor) {
        TagEditorDialog(
            title = if (uiState.editingTag != null) "Editar Tag" else "Nova Tag",
            initialName = uiState.editingTag?.name ?: "",
            initialColor = uiState.editingTag?.color ?: tagPalette.first(),
            onSave = { name, color -> viewModel.saveTag(name, color) },
            onDismiss = viewModel::dismissTagEditor,
            onDelete = uiState.editingTag?.let { tag -> { viewModel.deleteTag(tag) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListContent(
    uiState: TaskListUiState,
    showMenuButton: Boolean,
    onMenuClick: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.selectedCollection.label(uiState.tags),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (showMenuButton) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menu")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar lembrete")
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
                enter = fadeIn(), exit = fadeOut()
            ) {
                EmptyState(
                    title = "Sem lembretes",
                    subtitle = "Toque em + para adicionar seu primeiro lembrete"
                )
            }

            AnimatedVisibility(
                visible = uiState.tasks.isNotEmpty(),
                enter = fadeIn(), exit = fadeOut()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.tasks,
                        key = { task -> task.id }
                    ) { task ->
                        SwipeToDeleteContainer(task = task, onDelete = { onDeleteTask(task) }) {
                            TaskCard(
                                task = task,
                                allTags = uiState.tags,
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

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun TaskCollection.label(tags: List<Tag>): String = when (this) {
    TaskCollection.All -> "Todos"
    TaskCollection.Today -> "Hoje"
    TaskCollection.Scheduled -> "Programados"
    TaskCollection.Flagged -> "Sinalizados"
    TaskCollection.Completed -> "Concluídos"
    is TaskCollection.ByTag -> tags.find { it.id == tagId }?.name ?: "Tag"
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
        if (currentValue == SwipeToDismissBoxValue.EndToStart) onDelete()
    }
    SwipeToDismissBox(state = state, backgroundContent = {}, content = { content() })
}
