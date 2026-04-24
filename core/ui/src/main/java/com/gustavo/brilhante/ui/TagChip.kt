package com.gustavo.brilhante.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.model.Tag

/**
 * A rounded-rectangle chip representing a [Tag].
 *
 * Selected state: filled with [tag.color], white text and a check icon.
 * Unselected state: outlined border with [tag.color], low-alpha background.
 *
 * Animates smoothly between states via [animateColorAsState].
 */
@Composable
fun TagChip(
    tag: Tag,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = Color(tag.color.toInt())

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) tagColor else tagColor.copy(alpha = 0.10f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tagChipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else tagColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tagChipContent"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = BorderStroke(1.5.dp, tagColor),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = tag.name,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
