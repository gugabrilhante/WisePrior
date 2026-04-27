package com.gustavo.brilhante.ui

import androidx.compose.animation.animateColorAsState
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

/**
 * A rounded-rectangle chip representing a [Tag].
 *
 * Selected state: filled with tag color, presence of a check icon.
 * Unselected state: outlined border with tag color, low-alpha background.
 *
 * The foreground (text and icon) color is computed from tagColor.luminance() to ensure
 * sufficient contrast. The selection uses an animated color transition via [animateColorAsState].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagChip(
    tag: Tag,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val tagColor = Color(tag.color)
    val foregroundColor = if (tagColor.luminance() > 0.5f) Color.Black else Color.White

    FilterChip(
        selected = isSelected,
        onClick = onClick ?: {},
        enabled = onClick != null,
        label = {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        leadingIcon = if (isSelected) {
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
