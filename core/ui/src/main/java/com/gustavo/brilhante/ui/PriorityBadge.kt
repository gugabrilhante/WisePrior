package com.gustavo.brilhante.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.designsystem.theme.PriorityHigh
import com.gustavo.brilhante.designsystem.theme.PriorityLow
import com.gustavo.brilhante.designsystem.theme.PriorityMedium
import com.gustavo.brilhante.designsystem.theme.PriorityNone
import com.gustavo.brilhante.model.Priority

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val color = when (priority) {
        Priority.NONE -> PriorityNone
        Priority.LOW -> PriorityLow
        Priority.MEDIUM -> PriorityMedium
        Priority.HIGH -> PriorityHigh
    }
    Box(
        modifier = modifier
            .size(10.dp)
            .background(color, CircleShape)
    )
}
