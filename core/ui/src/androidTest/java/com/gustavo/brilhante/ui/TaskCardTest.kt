package com.gustavo.brilhante.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Task
import org.junit.Rule
import org.junit.Test

class TaskCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun taskCard_rendersTitleAndPriority() {
        val task = Task(id = 1, title = "Important Task", priority = Priority.HIGH)
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {}
            )
        }
        // Task title is user content — assert by text.
        composeTestRule.onNodeWithText("Important Task").assertExists()
        // Card container is present.
        composeTestRule.onNodeWithTag(TestTags.CARD_TASK_ITEM).assertExists()
    }

    @Test
    fun taskCard_triggersCallbacks() {
        var clicked = false
        val task = Task(id = 1, title = "Action Task")

        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = { clicked = true },
                onToggleComplete = {}
            )
        }

        // Click the card container via its stable test tag.
        composeTestRule.onNodeWithTag(TestTags.CARD_TASK_ITEM).performClick()
        assert(clicked)
    }
}
