package com.gustavo.brilhante.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
            .heightIn(min = 100.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            // LEFT: Checkbox in fixed-size touch-target zone
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onToggleComplete,
                    modifier = Modifier.semantics {
                        contentDescription =
                            if (task.isCompleted) "Marcar tarefa como incompleta" else "Marcar tarefa como completa"
                    }
                )
            }

            // CENTER: Content (weighted)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha)
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title with optional priority badge
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

                // Metadata: date + tags
                if (formattedDueDate != null || taskTags.isNotEmpty()) {
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
                            TaskTagBadge(tag = tag)
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

            // RIGHT: Indicators in fixed-width column (always rendered for stable layout)
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .width(40.dp)
                    .padding(top = 12.dp)
                    .alpha(contentAlpha)
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

@Composable
private fun TaskTagBadge(tag: Tag) {
    val tagColor = Color(tag.color)
    Surface(
        color = tagColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelMedium,
            color = tagColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
