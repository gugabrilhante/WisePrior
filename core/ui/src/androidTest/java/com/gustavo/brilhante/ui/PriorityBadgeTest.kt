package com.gustavo.brilhante.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.createComposeRule
import com.gustavo.brilhante.model.Priority
import org.junit.Rule
import org.junit.Test

class PriorityBadgeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun priorityBadge_renders() {
        composeTestRule.setContent {
            Column {
                PriorityBadge(priority = Priority.HIGH)
                PriorityBadge(priority = Priority.MEDIUM)
                PriorityBadge(priority = Priority.LOW)
                PriorityBadge(priority = Priority.NONE)
            }
        }
    }
}
