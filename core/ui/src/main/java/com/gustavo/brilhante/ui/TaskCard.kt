package com.gustavo.brilhante.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
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
    var isExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        // IntrinsicSize.Min lets the trailing column fill the row height
        // so Arrangement.SpaceBetween can push the chevron to the bottom
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(end = 4.dp)
                .height(IntrinsicSize.Min)
        ) {
            // LEFT: Checkbox in fixed-size touch-target zone
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
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

            // CENTER: Content with smooth expand/collapse animation
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha)
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title (with optional priority badge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.priority != Priority.NONE) {
                        PriorityBadge(priority = task.priority)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = if (isExpanded) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                // Notes — visible only when expanded
                if (isExpanded && task.notes.isNotBlank()) {
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                // Metadata: date + tags (collapsed → max 2 tags, expanded → all)
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
                        // Tags visibility handled by animateContentSize on the parent Column
                        taskTags.forEachIndexed { index, tag ->
                            if (isExpanded || index < 2) {
                                TaskTagBadge(tag = tag)
                            }
                        }
                        if (!isExpanded && taskTags.size > 2) {
                            Text(
                                text = "+${taskTags.size - 2}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // RIGHT: Fixed-width column — indicators at top, chevron at bottom
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .alpha(contentAlpha)
            ) {
                Column(horizontalAlignment = Alignment.End) {
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
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Recolher" else "Expandir",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        color = tagColor.copy(alpha = 0.25f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, tagColor)
    ) {
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelMedium,
            color = tagColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
