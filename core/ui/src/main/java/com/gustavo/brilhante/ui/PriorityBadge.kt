package com.gustavo.brilhante.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.designsystem.R as DesignR
import com.gustavo.brilhante.model.Priority

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val colorRes = when (priority) {
        Priority.NONE -> DesignR.color.priority_none
        Priority.LOW -> DesignR.color.priority_low
        Priority.MEDIUM -> DesignR.color.priority_medium
        Priority.HIGH -> DesignR.color.priority_high
    }
    Box(
        modifier = modifier
            .size(10.dp)
            .background(colorResource(colorRes), CircleShape)
    )
}
