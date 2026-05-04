package com.gustavo.brilhante.ui

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
            PriorityBadge(priority = Priority.HIGH)
        }
        // Minimal test to ensure no crashes during rendering of all priority types
        composeTestRule.setContent {
            PriorityBadge(priority = Priority.MEDIUM)
        }
        composeTestRule.setContent {
            PriorityBadge(priority = Priority.LOW)
        }
        composeTestRule.setContent {
            PriorityBadge(priority = Priority.NONE)
        }
    }
}
