package com.gustavo.brilhante.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import com.gustavo.brilhante.designsystem.R as DesignR
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    isExpanded: Boolean = false,
    onToggleExpanded: () -> Unit = {},
) {
    val uiModel = remember(task, allTags, formattedDueDate) {
        TaskCardUiMapper.map(task, allTags, formattedDueDate)
    }
    
    val checkboxContentDescription = stringResource(uiModel.checkboxDescriptionRes)
    val effectiveExpanded = isExpanded && uiModel.hasExpandableContent
    val transition = updateTransition(targetState = effectiveExpanded, label = "task_card_transition")

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
    ) { expanded -> if (uiModel.hasPriority && expanded) 24.dp else 0.dp }

    val titleStartPadding by transition.animateDp(
        transitionSpec = {
            if (targetState) tween(150, delayMillis = 50, easing = FastOutSlowInEasing)
            else tween(180, easing = FastOutLinearInEasing)
        },
        label = "titleStartPadding"
    ) { expanded -> if (expanded || !uiModel.hasPriority) 0.dp else 16.dp }

    val priorityAlpha by transition.animateFloat(
        transitionSpec = {
            if (targetState) tween(220, delayMillis = 80, easing = FastOutLinearInEasing)
            else tween(150, easing = FastOutLinearInEasing)
        },
        label = "priorityAlpha"
    ) { expanded -> if (expanded) 1f else 0f }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().testTag(TestTags.CARD_TASK_ITEM),
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
                    checked = uiModel.isCompleted,
                    onCheckedChange = onToggleComplete,
                    modifier = Modifier.semantics {
                        contentDescription = checkboxContentDescription
                    }
                )
            }

            // MAIN AREA
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(uiModel.contentAlpha)
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                // Header Area: Title, Priority and Indicators
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Title Stack
                    if (uiModel.hasPriority) {
                        PriorityIndicator(
                            priority = uiModel.priority,
                            label = uiModel.priorityTextRes?.let { stringResource(it) } ?: "",
                            color = uiModel.priorityColorRes?.let { colorResource(it) } ?: Color.Unspecified,
                            showText = effectiveExpanded,
                            textAlpha = priorityAlpha
                        )
                    }
                    Text(
                        text = uiModel.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = if (effectiveExpanded) 3 else 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (uiModel.isTitleStrikethrough) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = titleTopPadding, start = titleStartPadding)
                            .testTag(TestTags.TEXT_TASK_TITLE)
                    )

                    // Indicators
                    Row(
                        modifier = Modifier
                            .align(BiasAlignment(1f, verticalBias)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiModel.isFlagged) {
                            StatusIndicator(
                                icon = Icons.Filled.Flag,
                                text = stringResource(R.string.task_card_flagged),
                                color = colorResource(DesignR.color.flagged),
                                transition = transition,
                            )
                        }
                        if (uiModel.isUrgent) {
                            StatusIndicator(
                                icon = Icons.Filled.Warning,
                                text = stringResource(R.string.task_card_urgent),
                                color = colorResource(DesignR.color.urgent),
                                transition = transition,
                            )
                        }

                        if (uiModel.hasExpandableContent) {
                            ExpandButton(
                                isExpanded = effectiveExpanded,
                                onClick = onToggleExpanded
                            )
                        }
                    }
                }

                // NOTES
                AnimatedVisibility(
                    visible = effectiveExpanded && uiModel.notes.isNotBlank(),
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
                        text = uiModel.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 2.dp).testTag(TestTags.TEXT_TASK_NOTES)
                    )
                }

                // METADATA
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    if (uiModel.formattedDueDate != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = uiModel.formattedDueDate,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!effectiveExpanded && uiModel.tags.isNotEmpty()) {
                        uiModel.tags.take(1).forEach { tag ->
                            key(tag.id) { TaskTagBadge(tag = tag) }
                        }
                        if (uiModel.tags.size > 1) {
                            Text(
                                text = "+${uiModel.tags.size - 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // FLOW ROW
                if (effectiveExpanded && uiModel.tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiModel.tags.forEach { tag ->
                            key(tag.id) { TaskTagBadge(tag = tag) }
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
    label: String,
    color: Color,
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
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
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
