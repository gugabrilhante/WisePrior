package com.gustavo.brilhante.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.designsystem.theme.FlaggedColor
import com.gustavo.brilhante.designsystem.theme.UrgentColor
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onToggleComplete: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    allTags: List<Tag> = emptyList(),
    formattedDueDate: String? = null,
) {
    val taskTags = allTags.filter { task.tagIds.contains(it.id) }
    val contentAlpha = if (task.isCompleted) 0.6f else 1f

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            // Leading: completion checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onToggleComplete,
                modifier = Modifier.semantics {
                    contentDescription =
                        if (task.isCompleted) "Marcar tarefa como incompleta" else "Marcar tarefa como completa"
                }
            )

            // Middle: content (weighted)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha)
                    .padding(vertical = 12.dp)
            ) {
                // Title row (priority badge + title)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.priority != Priority.NONE) {
                        PriorityBadge(priority = task.priority)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                // Metadata row: date + tags
                if (formattedDueDate != null || taskTags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (formattedDueDate != null) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedDueDate,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        taskTags.take(3).forEach { tag ->
                            TagChip(tag = tag, isSelected = true, isSelectable = false, onClick = null)
                        }
                        if (taskTags.size > 3) {
                            Text(
                                text = "+${taskTags.size - 3}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Trailing: urgent + flagged indicators
            if (task.isUrgent || task.isFlagged) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(contentAlpha)
                ) {
                    if (task.isUrgent) {
                        Text("!", style = MaterialTheme.typography.titleMedium, color = UrgentColor)
                    }
                    if (task.isFlagged) {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = "Sinalizada",
                            tint = FlaggedColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
