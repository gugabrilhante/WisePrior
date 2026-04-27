package com.gustavo.brilhante.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.model.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagChip(
    tag: Tag,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val tagColor = Color(tag.color)
    val foregroundColor = if (tagColor.luminance() > 0.5f) Color.Black else Color.White
    val isVisuallySelected = if (!isSelectable) true else isSelected

    FilterChip(
        selected = isVisuallySelected,
        onClick = onClick ?: {},
        enabled = isSelectable && onClick != null,
        label = {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        leadingIcon = if (isSelectable && isSelected) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else null,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, tagColor),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = tagColor.copy(alpha = 0.10f),
            labelColor = tagColor,
            selectedContainerColor = tagColor,
            selectedLabelColor = foregroundColor,
            selectedLeadingIconColor = foregroundColor
        ),
        modifier = modifier
    )
}
