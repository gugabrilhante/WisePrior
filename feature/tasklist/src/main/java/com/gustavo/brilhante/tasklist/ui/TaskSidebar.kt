package com.gustavo.brilhante.tasklist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.tasklist.model.TaskCollection
import com.gustavo.brilhante.tasklist.presentation.CollectionCounts

private const val MAX_TAGS = 5

@Stable
private data class SidebarItem(
    val collection: TaskCollection,
    val label: String,
    val icon: ImageVector
)

private val defaultCollections = listOf(
    SidebarItem(TaskCollection.Today, "Hoje", Icons.Rounded.Today),
    SidebarItem(TaskCollection.Scheduled, "Programados", Icons.Rounded.CalendarMonth),
    SidebarItem(TaskCollection.All, "Todos", Icons.Rounded.Inbox),
    SidebarItem(TaskCollection.Flagged, "Sinalizados", Icons.Rounded.Flag),
    SidebarItem(TaskCollection.Completed, "Concluídos", Icons.Rounded.CheckCircle),
)

@Composable
fun TaskSidebarContent(
    selectedCollection: TaskCollection,
    counts: CollectionCounts,
    tags: List<Tag>,
    tagCounts: Map<Long, Int>,
    onCollectionSelected: (TaskCollection) -> Unit,
    onAddTag: () -> Unit,
    onEditTag: (Tag) -> Unit,
    modifier: Modifier = Modifier
) {
    Spacer(Modifier.height(16.dp))

    Text(
        text = "WisePrior",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp)
    )

    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

    // ── Default collections ───────────────────────────────────────────────────
    defaultCollections.forEach { item ->
        val count = counts.forCollection(item.collection)
        NavigationDrawerItem(
            icon = { Icon(item.icon, contentDescription = null) },
            label = { Text(item.label) },
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
            onClick = { onCollectionSelected(item.collection) },
            colors = NavigationDrawerItemDefaults.colors(),
            modifier = modifier.padding(horizontal = 12.dp)
        )
    }

    // ── Tags section ──────────────────────────────────────────────────────────
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

    Text(
        text = "Tags",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp)
    )

    tags.forEach { tag ->
        val tagTaskCount = tagCounts[tag.id] ?: 0
        val isSelected = selectedCollection == TaskCollection.ByTag(tag.id)

        NavigationDrawerItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(tag.color.toInt()), CircleShape)
                )
            },
            label = { Text(tag.name) },
            badge = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (tagTaskCount > 0) {
                        Text(
                            text = tagTaskCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = { onEditTag(tag) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Editar tag",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            selected = isSelected,
            onClick = { onCollectionSelected(TaskCollection.ByTag(tag.id)) },
            colors = NavigationDrawerItemDefaults.colors(),
            modifier = modifier.padding(horizontal = 12.dp)
        )
    }

    // ── Add tag button ────────────────────────────────────────────────────────
    val atLimit = tags.size >= MAX_TAGS
    NavigationDrawerItem(
        icon = {
            Icon(
                Icons.Rounded.Add,
                contentDescription = null,
                tint = if (atLimit) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary
            )
        },
        label = {
            Text(
                text = if (atLimit) "Limite de $MAX_TAGS tags atingido" else "Nova Tag",
                color = if (atLimit) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary
            )
        },
        selected = false,
        onClick = { if (!atLimit) onAddTag() },
        colors = NavigationDrawerItemDefaults.colors(),
        modifier = modifier
            .padding(horizontal = 12.dp)
            .alpha(if (atLimit) 0.5f else 1f)
    )
}
