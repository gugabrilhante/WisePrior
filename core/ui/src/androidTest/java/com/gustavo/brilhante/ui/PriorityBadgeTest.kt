package com.gustavo.brilhante.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.gustavo.brilhante.model.Priority
import org.junit.Rule
import org.junit.Test

class PriorityBadgeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ── All four priority cases (branch coverage) ─────────────────────────────

    @Test
    fun priorityBadge_high_renders() {
        composeTestRule.setContent {
            PriorityBadge(priority = Priority.HIGH, modifier = Modifier.testTag("badge_high"))
        }
        composeTestRule.onNodeWithTag("badge_high").assertIsDisplayed()
    }

    @Test
    fun priorityBadge_medium_renders() {
        composeTestRule.setContent {
            PriorityBadge(priority = Priority.MEDIUM, modifier = Modifier.testTag("badge_medium"))
        }
        composeTestRule.onNodeWithTag("badge_medium").assertIsDisplayed()
    }

    @Test
    fun priorityBadge_low_renders() {
        composeTestRule.setContent {
            PriorityBadge(priority = Priority.LOW, modifier = Modifier.testTag("badge_low"))
        }
        composeTestRule.onNodeWithTag("badge_low").assertIsDisplayed()
    }

    @Test
    fun priorityBadge_none_renders() {
        composeTestRule.setContent {
            PriorityBadge(priority = Priority.NONE, modifier = Modifier.testTag("badge_none"))
        }
        composeTestRule.onNodeWithTag("badge_none").assertIsDisplayed()
    }

    @Test
    fun priorityBadge_allFourInColumn_render() {
        composeTestRule.setContent {
            Column {
                PriorityBadge(priority = Priority.HIGH)
                PriorityBadge(priority = Priority.MEDIUM)
                PriorityBadge(priority = Priority.LOW)
                PriorityBadge(priority = Priority.NONE)
            }
        }
        // No assertion needed — just verifying no crash
    }
}
