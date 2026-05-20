package com.gustavo.brilhante.ui

import androidx.compose.animation.core.updateTransition
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.designsystem.R as DesignR
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
    onToggleChecklistItem: (itemId: Long, isChecked: Boolean) -> Unit = { _, _ -> },
) {
    val uiModel = remember(task, allTags, formattedDueDate) {
        TaskCardUiMapper.map(task, allTags, formattedDueDate)
    }
    
    val checkboxContentDescription = stringResource(uiModel.checkboxDescriptionRes)
    val effectiveExpanded = isExpanded && uiModel.hasExpandableContent

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().testTag(TestTags.CARD_TASK_ITEM),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
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
                        this.contentDescription = checkboxContentDescription
                    }
                )
            }

            // MAIN AREA
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                // Header Area: Title, Priority and Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Title and Priority
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiModel.hasPriority) {
                            PriorityIndicator(
                                priority = uiModel.priority,
                                label = uiModel.priorityTextRes?.let { stringResource(it) } ?: "",
                                color = uiModel.priorityColorRes?.let { colorResource(it) } ?: Color.Unspecified,
                                isExpanded = effectiveExpanded
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
                                .testTag(TestTags.TEXT_TASK_TITLE)
                        )
                    }

                    // Indicators
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        if (uiModel.isFlagged) {
                            StatusIndicator(
                                icon = Icons.Filled.Flag,
                                text = stringResource(R.string.task_card_flagged),
                                color = colorResource(DesignR.color.flagged),
                                isExpanded = effectiveExpanded
                            )
                        }
                        if (uiModel.isUrgent) {
                            StatusIndicator(
                                icon = Icons.Filled.Warning,
                                text = stringResource(R.string.task_card_urgent),
                                color = colorResource(DesignR.color.urgent),
                                isExpanded = effectiveExpanded
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
                if (effectiveExpanded && uiModel.notes.isNotBlank()) {
                    Text(
                        text = uiModel.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp).testTag(TestTags.TEXT_TASK_NOTES)
                    )
                }

                // CHECKLIST
                if (effectiveExpanded && uiModel.checklistItems.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        uiModel.checklistItems.forEachIndexed { index, item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("${TestTags.CHECKLIST_ITEM_ROW}_$index")
                            ) {
                                IconButton(
                                    onClick = { onToggleChecklistItem(item.id, !item.isChecked) },
                                    enabled = !uiModel.isCompleted,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("${TestTags.CHECKLIST_ITEM_CHECKBOX}_$index")
                                ) {
                                    Icon(
                                        imageVector = if (item.isDisplayChecked) Icons.Filled.CheckCircle
                                                      else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = stringResource(item.checkboxDescriptionRes),
                                        modifier = Modifier.size(22.dp),
                                        tint = if (item.isDisplayChecked) Color(0xFF34C759)
                                               else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (item.isDisplayChecked) MaterialTheme.colorScheme.onSurfaceVariant
                                            else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (item.isDisplayChecked) TextDecoration.LineThrough else null,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
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
    isExpanded: Boolean
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PriorityBadge(priority = priority)
        if (isExpanded) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}

@Composable
private fun StatusIndicator(
    icon: ImageVector,
    text: String,
    color: Color,
    isExpanded: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = text
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        if (isExpanded) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
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
