package com.gustavo.brilhante.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.designsystem.theme.FlaggedColor
import com.gustavo.brilhante.designsystem.theme.PriorityHigh
import com.gustavo.brilhante.designsystem.theme.PriorityLow
import com.gustavo.brilhante.designsystem.theme.PriorityMedium
import com.gustavo.brilhante.designsystem.theme.UrgentColor
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onToggleComplete: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    allTags: List<Tag> = emptyList(),
    formattedDueDate: String? = null,
) {
    val taskTags = remember(task.tagIds, allTags) {
        allTags.filter { task.tagIds.contains(it.id) }
    }
    val contentAlpha = if (task.isCompleted) 0.6f else 1f
    var isExpanded by remember { mutableStateOf(false) }

    val hasPriority = remember(task.priority) { task.priority != Priority.NONE }
    val contentSizeAnimSpec = remember { tween<IntSize>(70, easing = FastOutLinearInEasing) }
    val checkboxDescription = if (task.isCompleted)
        stringResource(R.string.task_card_mark_incomplete)
    else
        stringResource(R.string.task_card_mark_complete)

    // Single transition drives all coordinated animations, reducing recompositions
    val transition = updateTransition(targetState = isExpanded, label = "task_card_transition")

    val verticalBias by transition.animateFloat(
        transitionSpec = { tween(280, easing = FastOutSlowInEasing) },
        label = "verticalBias"
    ) { expanded -> if (expanded) -1f else 1f }

    val titleTopPadding by transition.animateDp(
        transitionSpec = {
            if (targetState) tween(150, delayMillis = 50, easing = FastOutSlowInEasing)
            else tween(180, easing = FastOutLinearInEasing)
        },
        label = "titleTopPadding"
    ) { expanded -> if (hasPriority && expanded) 24.dp else 0.dp }

    val titleStartPadding by transition.animateDp(
        transitionSpec = {
            if (targetState) tween(150, delayMillis = 50, easing = FastOutSlowInEasing)
            else tween(180, easing = FastOutLinearInEasing)
        },
        label = "titleStartPadding"
    ) { expanded -> if (expanded || !hasPriority) 0.dp else 16.dp }

    val priorityAlpha by transition.animateFloat(
        transitionSpec = {
            if (targetState) tween(220, delayMillis = 80, easing = FastOutLinearInEasing)
            else tween(150, easing = FastOutLinearInEasing)
        },
        label = "priorityAlpha"
    ) { expanded -> if (expanded) 1f else 0f }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = contentSizeAnimSpec),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            // LEFT: Checkbox
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onToggleComplete,
                    modifier = Modifier.semantics {
                        contentDescription = checkboxDescription
                    }
                )
            }

            // CENTER & RIGHT: Main Content Area
            // Box wraps its content; BiasAlignment positions the indicators Row
            // relative to the Column height, achieving the translation effect
            Box(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha)
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                // Moving Indicators & Expand Button (translates between top and bottom)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(BiasAlignment(1f, verticalBias)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(Modifier.weight(1f))

                    if (task.isFlagged) {
                        StatusIndicator(
                            icon = Icons.Filled.Flag,
                            text = stringResource(R.string.task_card_flagged),
                            color = FlaggedColor,
                            transition = transition,
                        )
                    }
                    if (task.isUrgent) {
                        StatusIndicator(
                            icon = Icons.Filled.Warning,
                            text = stringResource(R.string.task_card_urgent),
                            color = UrgentColor,
                            transition = transition,
                        )
                    }

                    ExpandButton(
                        isExpanded = isExpanded,
                        onClick = { isExpanded = !isExpanded }
                    )
                }

                // Main Stack: Title, Notes, Metadata
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (hasPriority) {
                            PriorityIndicator(
                                priority = task.priority,
                                showText = isExpanded,
                                textAlpha = priorityAlpha
                            )
                        }
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = if (isExpanded) 3 else 1,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = titleTopPadding, start = titleStartPadding)
                        )
                    }

                    // NOTES
                    AnimatedVisibility(
                        visible = isExpanded && task.notes.isNotBlank(),
                        enter = expandVertically(
                            animationSpec = tween(50, easing = FastOutSlowInEasing)
                        ) + fadeIn(
                            animationSpec = tween(300, easing = LinearOutSlowInEasing)
                        ),
                        exit = shrinkVertically(
                            animationSpec = tween(300, easing = FastOutLinearInEasing)
                        ) + fadeOut(
                            animationSpec = tween(150, easing = FastOutLinearInEasing)
                        )
                    ) {
                        Text(
                            text = task.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    // METADATA ROW (Date + Tags)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (formattedDueDate != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = formattedDueDate,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!isExpanded && taskTags.isNotEmpty()) {
                            taskTags.take(2).forEach { tag ->
                                key(tag.id) { TaskTagBadge(tag = tag) }
                            }
                            if (taskTags.size > 2) {
                                Text(
                                    text = "+${taskTags.size - 2}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // EXPANDED TAGS (FlowRow)
                    if (isExpanded && taskTags.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            taskTags.forEach { tag ->
                                key(tag.id) { TaskTagBadge(tag = tag) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandButton(
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (isExpanded) stringResource(R.string.task_card_collapse) else stringResource(R.string.task_card_expand),
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PriorityIndicator(
    priority: Priority,
    showText: Boolean,
    textAlpha: Float
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PriorityBadge(priority = priority)
        if (showText || textAlpha > 0f) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = when (priority) {
                    Priority.LOW -> stringResource(R.string.priority_low)
                    Priority.MEDIUM -> stringResource(R.string.priority_medium)
                    Priority.HIGH -> stringResource(R.string.priority_high)
                    else -> ""
                },
                style = MaterialTheme.typography.labelMedium,
                color = when (priority) {
                    Priority.LOW -> PriorityLow
                    Priority.MEDIUM -> PriorityMedium
                    Priority.HIGH -> PriorityHigh
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}

@Composable
private fun StatusIndicator(
    icon: ImageVector,
    text: String,
    color: Color,
    transition: Transition<Boolean>,
) {
    val statusHorizontalBias by transition.animateFloat(
        transitionSpec = {
            if (targetState) tween(280, delayMillis = 0, easing = FastOutSlowInEasing)
            else tween(200, delayMillis = 80, easing = FastOutLinearInEasing)
        },
        label = "statusHorizontalBias"
    ) { expanded -> if (expanded) -1f else 1f }
    val statusTextStartPadding by transition.animateDp(
        transitionSpec = {
            if (targetState) tween(160, delayMillis = 120, easing = FastOutSlowInEasing)
            else tween(100, delayMillis = 0, easing = FastOutLinearInEasing)
        },
        label = "statusTextStartPadding"
    ) { expanded -> if (expanded) 18.dp else 0.dp }
    val statusTextAlpha by transition.animateFloat(
        transitionSpec = {
            if (targetState) tween(200, delayMillis = 150, easing = FastOutLinearInEasing)
            else tween(100, delayMillis = 0, easing = FastOutLinearInEasing)
        },
        label = "statusTextAlpha"
    ) { expanded -> if (expanded) 1f else 0f }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.wrapContentWidth()) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier
                    .size(16.dp)
                    .align(BiasAlignment(statusHorizontalBias, 0f))
            )
            if (statusTextAlpha > 0f) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    modifier = Modifier
                        .alpha(statusTextAlpha)
                        .padding(start = statusTextStartPadding)
                )
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
