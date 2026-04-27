package com.gustavo.brilhante.tasklist.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.tasklist.R

internal data class TagPaletteEntry(
    val value: Long,
    @StringRes val nameResId: Int
)

internal val tagPalette = listOf(
    TagPaletteEntry(0xFFEF4444L, R.string.color_name_red),
    TagPaletteEntry(0xFFF97316L, R.string.color_name_orange),
    TagPaletteEntry(0xFFEAB308L, R.string.color_name_yellow),
    TagPaletteEntry(0xFF22C55EL, R.string.color_name_green),
    TagPaletteEntry(0xFF3B82F6L, R.string.color_name_blue),
    TagPaletteEntry(0xFF8B5CF6L, R.string.color_name_purple),
    TagPaletteEntry(0xFFEC4899L, R.string.color_name_pink),
    TagPaletteEntry(0xFF6B7280L, R.string.color_name_gray),
)

@Composable
fun TagEditorDialog(
    title: String,
    initialName: String,
    initialColor: Long,
    onSave: (name: String, color: Long) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableLongStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.color_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    tagPalette.forEach { entry ->
                        val colorValue = entry.value
                        val isSelected = selectedColor == colorValue
                        val colorName = stringResource(entry.nameResId)
                        val description = stringResource(R.string.color_name_format, colorName)
                        
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorValue))
                                .then(
                                    if (isSelected) Modifier.border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    ) else Modifier
                                )
                                .semantics {
                                    contentDescription = description
                                    role = Role.RadioButton
                                    selected = isSelected
                                }
                                .clickable { selectedColor = colorValue }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), selectedColor) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save_button))
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = stringResource(R.string.delete_button),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(onClick = onDismiss) { 
                    Text(stringResource(R.string.cancel_button)) 
                }
            }
        }
    )
}
