package com.gustavo.brilhante.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    supportingText: String? = null,
    onRowClick: (() -> Unit)? = null,
    testTag: String? = null
) {
    val rowModifier = modifier
        .padding(horizontal = 4.dp)
        .clickable {
            if (onRowClick != null) {
                onRowClick()
            } else {
                onCheckedChange(!checked)
            }
        }
        .then(testTag?.let { Modifier.testTag(it) } ?: Modifier)

    ListItem(
        headlineContent = { Text(label) },
        supportingContent = supportingText?.let { { Text(it) } },
        leadingContent = icon?.let { { Icon(it, contentDescription = null) } },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = rowModifier
    )
}
