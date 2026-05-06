package com.gustavo.brilhante.tasklist.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.ui.TestTags
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.model.TaskSortOption
import com.gustavo.brilhante.tasklist.R
import com.gustavo.brilhante.tasklist.model.TaskCollection
import com.gustavo.brilhante.tasklist.presentation.TaskListUiState
import com.gustavo.brilhante.tasklist.presentation.TaskListViewModel
import com.gustavo.brilhante.ui.EmptyState
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import com.gustavo.brilhante.ui.TagPalette
import com.gustavo.brilhante.ui.TaskCard
import kotlinx.coroutines.delay
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
            uiState = uiState,
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
                onDeleteTask = viewModel::deleteTask,
                onToggleComplete = viewModel::onTaskCheckedChange,
                onToggleExpanded = viewModel::toggleExpanded,
                onSortOptionSelected = viewModel::setSortOption
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
                onDeleteTask = viewModel::deleteTask,
                onToggleComplete = viewModel::onTaskCheckedChange,
                onToggleExpanded = viewModel::toggleExpanded,
                onSortOptionSelected = viewModel::setSortOption
            )
        }
    }

    if (uiState.showTagEditor) {
        TagEditorDialog(
            title = if (uiState.editingTag != null) stringResource(R.string.edit_tag_title)
                    else stringResource(R.string.new_tag_title),
            initialName = uiState.editingTag?.name ?: "",
            initialColor = uiState.editingTag?.color ?: colorResource(TagPalette.colors.first().colorResId).toArgb().toLong() and 0xFFFFFFFFL,
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
    onToggleComplete: (Task, Boolean) -> Unit,
    onToggleExpanded: (Long) -> Unit,
    onSortOptionSelected: (TaskSortOption) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.screenTitle.asString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (showMenuButton) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.menu_button_description))
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.Sort,
                                contentDescription = stringResource(R.string.sort_button_description)
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            uiState.sortOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.label.asString()) },
                                    onClick = {
                                        onSortOptionSelected(opt.option)
                                        showSortMenu = false
                                    },
                                    leadingIcon = if (opt.isSelected) {
                                        { Icon(Icons.Filled.Check, contentDescription = null) }
                                    } else null
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                modifier = Modifier.testTag(TestTags.BTN_TASK_LIST_ADD),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_task_button_description))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = uiState.showEmptyState,
                enter = fadeIn(), exit = fadeOut()
            ) {
                EmptyState(
                    title = stringResource(R.string.empty_tasks_title),
                    subtitle = stringResource(R.string.empty_tasks_subtitle)
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
                                formattedDueDate = uiState.formattedDueDates[task.id],
                                onClick = { onEditTask(task) },
                                onToggleComplete = { isChecked -> onToggleComplete(task, isChecked) },
                                isExpanded = task.id in uiState.expandedTaskIds,
                                onToggleExpanded = { onToggleExpanded(task.id) },
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .animateItem(
                                        placementSpec = tween(
                                            durationMillis = 150,
                                            easing = FastOutLinearInEasing
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Swipe-to-delete ───────────────────────────────────────────────────────────

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
            delay(400)
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = when (state.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .background(color, MaterialTheme.shapes.medium)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_swipe_action),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        content = { content() }
    )
}
