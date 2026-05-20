package com.gustavo.brilhante.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gustavo.brilhante.model.ChecklistItem
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TaskCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val defaultCreatedAt = 1000L

    // ── Basic rendering ───────────────────────────────────────────────────────

    @Test
    fun taskCard_rendersTitleAndPriority() {
        val task = Task(id = 1, title = "Important Task", priority = Priority.HIGH, createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {})
        }
        composeTestRule.onNodeWithText("Important Task").assertExists()
        composeTestRule.onNodeWithTag(TestTags.CARD_TASK_ITEM).assertExists()
    }

    @Test
    fun taskCard_triggersOnClickCallback() {
        var clicked = false
        val task = Task(id = 1, title = "Action Task", createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = { clicked = true }, onToggleComplete = {})
        }
        composeTestRule.onNodeWithTag(TestTags.CARD_TASK_ITEM).performClick()
        assert(clicked)
    }

    @Test
    fun taskCard_titleIsDisplayed() {
        val task = Task(id = 1, title = "Buy milk", createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {})
        }
        composeTestRule.onNodeWithTag(TestTags.TEXT_TASK_TITLE, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Buy milk").assertExists()
    }

    // ── Expandable content ────────────────────────────────────────────────────

    @Test
    fun taskCard_withNotes_showsExpandButton() {
        val task = Task(id = 1, title = "Task", notes = "These are my notes", createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {})
        }
        composeTestRule.onNodeWithContentDescription("Expand").assertIsDisplayed()
    }

    @Test
    fun taskCard_expandedWithNotes_showsNotesText() {
        val task = Task(id = 1, title = "Task", notes = "Secret detail", createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                isExpanded = true
            )
        }
        composeTestRule.onNodeWithTag(TestTags.TEXT_TASK_NOTES, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Secret detail").assertExists()
    }

    @Test
    fun taskCard_collapsed_doesNotShowNotes() {
        val task = Task(id = 1, title = "Task", notes = "Hidden notes", createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                isExpanded = false
            )
        }
        composeTestRule.onNodeWithTag(TestTags.TEXT_TASK_NOTES, useUnmergedTree = true).assertDoesNotExist()
    }

    // ── Flag and urgent indicators ────────────────────────────────────────────

    @Test
    fun taskCard_flaggedTask_showsFlagIcon() {
        val task = Task(id = 1, title = "Flagged Task", isFlagged = true, createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, isExpanded = true)
        }
        composeTestRule.onAllNodesWithContentDescription("Flagged").onFirst().assertIsDisplayed()
    }

    @Test
    fun taskCard_urgentTask_showsWarningIcon() {
        val task = Task(id = 1, title = "Urgent Task", isUrgent = true, createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, isExpanded = true)
        }
        composeTestRule.onAllNodesWithContentDescription("Urgent").onFirst().assertIsDisplayed()
    }

    @Test
    fun taskCard_flaggedAndUrgent_showsBothIndicators() {
        val task = Task(id = 1, title = "Critical", isFlagged = true, isUrgent = true, createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, isExpanded = true)
        }
        composeTestRule.onAllNodesWithContentDescription("Flagged").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("Urgent").onFirst().assertIsDisplayed()
    }

    // ── Due date ──────────────────────────────────────────────────────────────

    @Test
    fun taskCard_withFormattedDueDate_showsDateText() {
        val task = Task(id = 1, title = "Dated task", dueDate = 1_700_000_000_000L, createdAt = defaultCreatedAt)
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
        val task = Task(id = 1, title = "Undated task", createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, formattedDueDate = null)
        }
        composeTestRule.onNodeWithText("Jan 15").assertDoesNotExist()
    }

    // ── Tags ──────────────────────────────────────────────────────────────────

    @Test
    fun taskCard_withTag_showsTagName() {
        val tag = Tag(id = 1L, name = "Work", color = 0xFF3B82F6L)
        val task = Task(id = 1, title = "Tagged task", tagIds = listOf(1L), createdAt = defaultCreatedAt)
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
        val task = Task(id = 1, title = "Multi-tag", tagIds = listOf(1L, 2L, 3L), createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                allTags = tags,
                isExpanded = false
            )
        }
        composeTestRule.onNodeWithText("+2").assertIsDisplayed()
    }

    // ── Completion state ──────────────────────────────────────────────────────

    @Test
    fun taskCard_completedTask_cardIsPresent() {
        val task = Task(id = 1, title = "Done task", isCompleted = true, createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {})
        }
        composeTestRule.onNodeWithTag(TestTags.CARD_TASK_ITEM).assertIsDisplayed()
    }

    @Test
    fun taskCard_nonExpandableTask_doesNotShowExpandButton() {
        val task = Task(id = 1, title = "Plain task", createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {})
        }
        composeTestRule.onNodeWithContentDescription("Expand").assertDoesNotExist()
    }

    // ── onToggleExpanded callback ─────────────────────────────────────────────

    @Test
    fun taskCard_clickingExpandButton_triggersCallback() {
        var toggled = false
        val task = Task(id = 1, title = "Task", notes = "Notes to expand", createdAt = defaultCreatedAt)
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
        val tags = listOf(
            Tag(id = 1L, name = "Alpha", color = 0xFF3B82F6L),
            Tag(id = 2L, name = "Beta", color = 0xFF22C55EL),
            Tag(id = 3L, name = "Gamma", color = 0xFFEF4444L)
        )
        val task = Task(id = 1, title = "Multi expanded", tagIds = listOf(1L, 2L, 3L), createdAt = defaultCreatedAt)
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
        val task = Task(id = 1, title = "Priority Task", priority = Priority.HIGH, notes = "Detail", createdAt = defaultCreatedAt)
        val highPriorityLabel = "High priority"
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                isExpanded = true
            )
        }
        composeTestRule.onNodeWithText(highPriorityLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText("Priority Task").assertIsDisplayed()
    }

    // ── Checklist ─────────────────────────────────────────────────────────────

    @Test
    fun taskCard_expanded_withChecklist_displaysItems() {
        val items = listOf(
            ChecklistItem(id = 1, text = "Subtask 1"),
            ChecklistItem(id = 2, text = "Subtask 2")
        )
        val task = Task(id = 1, title = "Parent", checklistItems = items, createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, isExpanded = true)
        }
        composeTestRule.onNodeWithText("Subtask 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Subtask 2").assertIsDisplayed()
    }

    @Test
    fun taskCard_clickingChecklistItem_triggersCallback() {
        var toggledId = -1L
        var toggledChecked = false
        val items = listOf(ChecklistItem(id = 42, text = "Click me", isChecked = false))
        val task = Task(id = 1, title = "Task", checklistItems = items, createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(
                task = task,
                onClick = {},
                onToggleComplete = {},
                isExpanded = true,
                onToggleChecklistItem = { id, checked ->
                    toggledId = id
                    toggledChecked = checked
                }
            )
        }
        
        composeTestRule.onNodeWithTag("${TestTags.CHECKLIST_ITEM_CHECKBOX}_0").performClick()
        
        assertEquals(42L, toggledId)
        assertTrue(toggledChecked)
    }

    @Test
    fun taskCard_completedTask_checklistCheckboxesAreDisabled() {
        val items = listOf(ChecklistItem(id = 1, text = "Subtask"))
        val task = Task(id = 1, title = "Done", isCompleted = true, checklistItems = items, createdAt = defaultCreatedAt)
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, isExpanded = true)
        }
        
        composeTestRule.onNodeWithTag("${TestTags.CHECKLIST_ITEM_CHECKBOX}_0").assertIsNotEnabled()
    }

    @Test
    fun taskCard_checkedItem_showsMarkIncompleteContentDescription() {
        val items = listOf(ChecklistItem(id = 1, text = "Checked", isChecked = true))
        val task = Task(id = 1, title = "Task", checklistItems = items, createdAt = defaultCreatedAt)
        val markIncompleteCd = "Mark task as incomplete" // R.string.task_card_mark_incomplete
        
        composeTestRule.setContent {
            TaskCard(task = task, onClick = {}, onToggleComplete = {}, isExpanded = true)
        }
        
        composeTestRule.onNodeWithContentDescription(markIncompleteCd).assertIsDisplayed()
    }
}
