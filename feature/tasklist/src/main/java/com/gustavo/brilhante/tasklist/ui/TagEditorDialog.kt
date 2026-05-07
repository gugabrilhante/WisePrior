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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.gustavo.brilhante.ui.TagPalette
import com.gustavo.brilhante.ui.TestTags
import com.gustavo.brilhante.ui.R as CoreR
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gustavo.brilhante.tasklist.R

@Composable
fun TagEditorDialog(
    title: String,
    initialName: String,
    initialColor: Long,
    onSave: (name: String, color: Long) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var selectedColor by rememberSaveable { mutableLongStateOf(initialColor) }

    AlertDialog(
        modifier = Modifier.testTag(TestTags.DIALOG_TAG_EDITOR),
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.INPUT_TAG_EDITOR_NAME)
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
                    TagPalette.colors.forEach { entry ->
                        val color = colorResource(entry.colorResId)
                        val colorValue = color.toArgb().toLong() and 0xFFFFFFFFL
                        val isSelected = selectedColor == colorValue
                        val colorName = stringResource(entry.nameResId)
                        val description = stringResource(CoreR.string.color_name_format, colorName)
                        
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
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
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag(TestTags.BTN_TAG_EDITOR_SAVE)
            ) {
                Text(stringResource(R.string.save_button))
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag(TestTags.BTN_TAG_EDITOR_DELETE)
                    ) {
                        Text(
                            text = stringResource(R.string.delete_button),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag(TestTags.BTN_TAG_EDITOR_CANCEL)
                ) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        }
    )
}
