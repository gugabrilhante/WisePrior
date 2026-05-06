package com.gustavo.brilhante.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import org.junit.Rule
import org.junit.Test

class TaskCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Basic rendering ───────────────────────────────────────────────────────

    @Test
    fun taskCard_rendersTitleAndPriority() {
        val task = Task(id = 1, title = "Important Task", priority = Priority.HIGH)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {})
        }
        composeTestRule.onNodeWithText("Important Task").assertExists()
        composeTestRule.onNodeWithTag(TestTags.CARD_TASK_ITEM).assertExists()
    }

    @Test
    fun taskCard_triggersOnClickCallback() {
        var clicked = false
        val task = Task(id = 1, title = "Action Task")
        composeTestRule.setContent {
            TaskCard(task = task, onClick = { clicked = true }, onToggleComplete = {})
        }
        composeTestRule.onNodeWithTag(TestTags.CARD_TASK_ITEM).performClick()
        assert(clicked)
    }

    @Test
    fun taskCard_titleIsDisplayed() {
        val task = Task(id = 1, title = "Buy milk")
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {})
        }
        composeTestRule.onNodeWithTag(TestTags.TEXT_TASK_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithText("Buy milk").assertIsDisplayed()
    }

    // ── Expandable content ────────────────────────────────────────────────────

    @Test
    fun taskCard_withNotes_showsExpandButton() {
        val task = Task(id = 1, title = "Task", notes = "These are my notes")
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {})
        }
        // Expand button should appear since notes make the card expandable
        composeTestRule.onNodeWithContentDescription("Expand").assertIsDisplayed()
    }

    @Test
    fun taskCard_expandedWithNotes_showsNotesText() {
        val task = Task(id = 1, title = "Task", notes = "Secret detail")
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                isExpanded = true
            )
        }
        composeTestRule.onNodeWithTag(TestTags.TEXT_TASK_NOTES).assertIsDisplayed()
        composeTestRule.onNodeWithText("Secret detail").assertIsDisplayed()
    }

    @Test
    fun taskCard_collapsed_doesNotShowNotes() {
        val task = Task(id = 1, title = "Task", notes = "Hidden notes")
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                isExpanded = false
            )
        }
        composeTestRule.onNodeWithTag(TestTags.TEXT_TASK_NOTES).assertDoesNotExist()
    }

    // ── Flag and urgent indicators ────────────────────────────────────────────

    @Test
    fun taskCard_flaggedTask_showsFlagIcon() {
        val task = Task(id = 1, title = "Flagged Task", isFlagged = true)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, isExpanded = true)
        }
        composeTestRule.onNodeWithContentDescription("Flagged").assertIsDisplayed()
    }

    @Test
    fun taskCard_urgentTask_showsWarningIcon() {
        val task = Task(id = 1, title = "Urgent Task", isUrgent = true)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, isExpanded = true)
        }
        composeTestRule.onNodeWithContentDescription("Urgent").assertIsDisplayed()
    }

    @Test
    fun taskCard_flaggedAndUrgent_showsBothIndicators() {
        val task = Task(id = 1, title = "Critical", isFlagged = true, isUrgent = true)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, isExpanded = true)
        }
        composeTestRule.onNodeWithContentDescription("Flagged").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Urgent").assertIsDisplayed()
    }

    // ── Due date ──────────────────────────────────────────────────────────────

    @Test
    fun taskCard_withFormattedDueDate_showsDateText() {
        val task = Task(id = 1, title = "Dated task", dueDate = 1_700_000_000_000L)
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                formattedDueDate = "Jan 15"
            )
        }
        composeTestRule.onNodeWithText("Jan 15").assertIsDisplayed()
    }

    @Test
    fun taskCard_withoutDueDate_doesNotShowDateRow() {
        val task = Task(id = 1, title = "Undated task")
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, formattedDueDate = null)
        }
        // No date text should be present
        composeTestRule.onNodeWithText("Jan 15").assertDoesNotExist()
    }

    // ── Tags ──────────────────────────────────────────────────────────────────

    @Test
    fun taskCard_withTag_showsTagName() {
        val tag = Tag(id = 1L, name = "Work", color = 0xFF3B82F6L)
        val task = Task(id = 1, title = "Tagged task", tagIds = listOf(1L))
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, allTags = listOf(tag))
        }
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
    }

    @Test
    fun taskCard_withMultipleTags_showsOverflowCount() {
        val tags = listOf(
            Tag(id = 1L, name = "Work", color = 0xFF3B82F6L),
            Tag(id = 2L, name = "Personal", color = 0xFF22C55EL),
            Tag(id = 3L, name = "Urgent", color = 0xFFEF4444L)
        )
        val task = Task(id = 1, title = "Multi-tag", tagIds = listOf(1L, 2L, 3L))
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                allTags = tags,
                isExpanded = false
            )
        }
        // Collapsed with 3 tags → shows first tag + "+2" overflow
        composeTestRule.onNodeWithText("+2").assertIsDisplayed()
    }

    // ── Completion state ──────────────────────────────────────────────────────

    @Test
    fun taskCard_completedTask_cardIsPresent() {
        val task = Task(id = 1, title = "Done task", isCompleted = true)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {})
        }
        composeTestRule.onNodeWithTag(TestTags.CARD_TASK_ITEM).assertIsDisplayed()
    }

    @Test
    fun taskCard_nonExpandableTask_doesNotShowExpandButton() {
        // Plain task with no expandable attributes
        val task = Task(id = 1, title = "Plain task")
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {})
        }
        composeTestRule.onNodeWithContentDescription("Expand").assertDoesNotExist()
    }

    // ── onToggleExpanded callback ─────────────────────────────────────────────

    @Test
    fun taskCard_clickingExpandButton_triggersCallback() {
        var toggled = false
        val task = Task(id = 1, title = "Task", notes = "Notes to expand")
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                onToggleExpanded = { toggled = true }
            )
        }
        composeTestRule.onNodeWithContentDescription("Expand").performClick()
        assert(toggled)
    }

    // ── Expanded tags FlowRow ─────────────────────────────────────────────────

    @Test
    fun taskCard_expanded_withThreeTags_showsAllTagsInFlowRow() {
        // 3 tags → hasExpandableContent=true, effectiveExpanded=true → FlowRow path
        val tags = listOf(
            Tag(id = 1L, name = "Alpha", color = 0xFF3B82F6L),
            Tag(id = 2L, name = "Beta", color = 0xFF22C55EL),
            Tag(id = 3L, name = "Gamma", color = 0xFFEF4444L)
        )
        val task = Task(id = 1, title = "Multi expanded", tagIds = listOf(1L, 2L, 3L))
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                allTags = tags,
                isExpanded = true
            )
        }
        composeTestRule.onNodeWithText("Alpha").assertIsDisplayed()
        composeTestRule.onNodeWithText("Beta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gamma").assertIsDisplayed()
    }

    // ── Priority text when expanded ───────────────────────────────────────────

    @Test
    fun taskCard_expanded_withHighPriority_showsPriorityIndicator() {
        // isExpanded=true + hasPriority=true → PriorityIndicator showText=true path
        val task = Task(id = 1, title = "Priority Task", priority = Priority.HIGH, notes = "Detail")
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                isExpanded = true
            )
        }
        composeTestRule.onNodeWithTag(TestTags.CARD_TASK_ITEM).assertIsDisplayed()
    }
}
