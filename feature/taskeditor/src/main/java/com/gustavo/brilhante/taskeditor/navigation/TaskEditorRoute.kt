package com.gustavo.brilhante.taskeditor.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class TaskEditorRoute(val taskId: Long = -1L) : NavKey
