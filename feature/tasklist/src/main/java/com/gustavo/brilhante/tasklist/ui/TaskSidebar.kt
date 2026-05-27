package com.gustavo.brilhante.tasklist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.gustavo.brilhante.ui.TestTags
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.tasklist.R
import com.gustavo.brilhante.tasklist.model.TaskCollection
import com.gustavo.brilhante.tasklist.presentation.TaskListEvent
import com.gustavo.brilhante.tasklist.presentation.TaskListUiState

// Public so the app-module UI tests can reference it without hardcoding the string.
const val SIDEBAR_LIST_TEST_TAG = "sidebar_list"
const val ADD_TAG_BUTTON_TEST_TAG = "add_tag_button"

@Stable
private data class SidebarItem(
    val collection: TaskCollection,
    val labelResId: Int,
    val icon: ImageVector,
    val testTag: String
)

private val defaultCollections = listOf(
    SidebarItem(TaskCollection.Today, R.string.sidebar_today, Icons.Rounded.Today, TestTags.SIDEBAR_ITEM_TODAY),
    SidebarItem(TaskCollection.Scheduled, R.string.sidebar_scheduled, Icons.Rounded.CalendarMonth, TestTags.SIDEBAR_ITEM_SCHEDULED),
    SidebarItem(TaskCollection.All, R.string.sidebar_all, Icons.Rounded.Inbox, TestTags.SIDEBAR_ITEM_ALL),
    SidebarItem(TaskCollection.Flagged, R.string.sidebar_flagged, Icons.Rounded.Flag, TestTags.SIDEBAR_ITEM_FLAGGED),
    SidebarItem(TaskCollection.Completed, R.string.sidebar_completed, Icons.Rounded.CheckCircle, TestTags.SIDEBAR_ITEM_COMPLETED),
)

@Composable
fun TaskSidebarContent(
    uiState: TaskListUiState,
    onEvent: (TaskListEvent) -> Unit,
    defaultColor: Long,
    modifier: Modifier = Modifier
) {
    val counts = uiState.collectionCounts
    val tags = uiState.tags
    val tagCounts = uiState.tagCounts
    val selectedCollection = uiState.selectedCollection

    // LazyColumn provides native vertical scrolling
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .testTag(SIDEBAR_LIST_TEST_TAG)
    ) {
        item(key = "header_spacer") {
            Spacer(Modifier.height(16.dp))
        }

        item(key = "title") {
            Text(
                text = stringResource(R.string.app_name_sidebar),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp)
            )
        }

        item(key = "divider_top") {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        // ── Default collections ───────────────────────────────────────────────
        items(
            items = defaultCollections,
            // labelResId is unique per collection and stable across recompositions
            key = { it.labelResId }
        ) { item ->
            val count = counts.forCollection(item.collection)
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(stringResource(item.labelResId)) },
                badge = if (count > 0) {
                    {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else null,
                selected = selectedCollection == item.collection,
                onClick = { onEvent(TaskListEvent.SelectCollection(item.collection)) },
                colors = NavigationDrawerItemDefaults.colors(),
                modifier = Modifier.padding(horizontal = 12.dp).testTag(item.testTag)
            )
        }

        // ── Tags section ──────────────────────────────────────────────────────
        item(key = "divider_tags") {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        item(key = "tags_header") {
            Text(
                text = stringResource(R.string.sidebar_tags_header),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp).testTag(TestTags.SIDEBAR_TAGS_HEADER)
            )
        }

        items(items = tags, key = { tag -> "tag_${tag.id}" }) { tag ->
            val tagTaskCount = tagCounts[tag.id] ?: 0
            val isSelected = selectedCollection == TaskCollection.ByTag(tag.id)

            NavigationDrawerItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(tag.color), CircleShape)
                    )
                },
                label = { Text(tag.name) },
                badge = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (tagTaskCount > 0) {
                            Text(
                                text = tagTaskCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        IconButton(
                            onClick = { onEvent(TaskListEvent.ShowEditTag(tag)) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.edit_tag_description),
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                selected = isSelected,
                onClick = { onEvent(TaskListEvent.SelectCollection(TaskCollection.ByTag(tag.id))) },
                colors = NavigationDrawerItemDefaults.colors(),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // ── Add tag button ────────────────────────────────────────────────────
        item(key = "add_tag") {
            val enabled = uiState.canAddTag
            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = uiState.addTagLabel.asString(),
                        color = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                selected = false,
                onClick = { if (enabled) onEvent(TaskListEvent.ShowAddTag(defaultColor)) },
                colors = NavigationDrawerItemDefaults.colors(),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .testTag(ADD_TAG_BUTTON_TEST_TAG)
                    .alpha(if (enabled) 1f else 0.5f)
                    .semantics { if (!enabled) disabled() }
            )
        }
    }
}
