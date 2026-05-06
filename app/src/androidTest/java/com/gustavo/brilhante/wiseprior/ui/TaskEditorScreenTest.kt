package com.gustavo.brilhante.wiseprior.ui

import android.Manifest
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.gustavo.brilhante.ui.TestTags
import com.gustavo.brilhante.wiseprior.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Task Editor screen: new task creation, editing, validation,
 * and priority selection.
 */
@HiltAndroidTest
class TaskEditorScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var addReminderCd: String
    private lateinit var newScreenTitle: String
    private lateinit var editScreenTitle: String
    private lateinit var backCd: String

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.activity.run {
            addReminderCd = getString(com.gustavo.brilhante.tasklist.R.string.add_task_button_description)
            newScreenTitle = getString(com.gustavo.brilhante.taskeditor.R.string.editor_title_new)
            editScreenTitle = getString(com.gustavo.brilhante.taskeditor.R.string.editor_title_edit)
            backCd = getString(com.gustavo.brilhante.taskeditor.R.string.editor_back)
        }
        val emptyTitle = composeTestRule.activity.getString(com.gustavo.brilhante.tasklist.R.string.empty_tasks_title)
        waitUntilDisplayed(emptyTitle)

        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithTag(TestTags.SCREEN_TASK_EDITOR).assertIsDisplayed()
    }

    @Test
    fun newTaskEditor_showsCorrectTitle() {
        composeTestRule.onNodeWithText(newScreenTitle).assertIsDisplayed()
    }

    @Test
    fun doneButton_isVisible() {
        composeTestRule.onNodeWithTag(TestTags.BTN_TASK_EDITOR_DONE).assertIsDisplayed()
    }

    @Test
    fun dateTimeSection_isVisible() {
        composeTestRule.onNodeWithTag(TestTags.SECTION_TASK_EDITOR_DATETIME).assertIsDisplayed()
    }

    @Test
    fun prioritySegmentedButtons_areAllVisible() {
        composeTestRule.onNodeWithTag(TestTags.SEGMENT_PRIORITY_NONE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SEGMENT_PRIORITY_LOW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SEGMENT_PRIORITY_MEDIUM).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SEGMENT_PRIORITY_HIGH).assertIsDisplayed()
    }

    @Test
    fun priorityNone_isSelectedByDefault() {
        composeTestRule.onNodeWithTag(TestTags.SEGMENT_PRIORITY_NONE).assertIsSelected()
    }

    @Test
    fun selectingPriority_updatesSelection() {
        composeTestRule.onNodeWithTag(TestTags.SEGMENT_PRIORITY_HIGH).performClick()
        composeTestRule.onNodeWithTag(TestTags.SEGMENT_PRIORITY_HIGH).assertIsSelected()
    }

    @Test
    fun savingWithBlankTitle_showsValidationError() {
        composeTestRule.onNodeWithTag(TestTags.BTN_TASK_EDITOR_DONE).performClick()
        composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()
        composeTestRule.onNodeWithText(newScreenTitle).assertIsDisplayed()
    }

    @Test
    fun enteringTitle_andSaving_navigatesBack() {
        val emptyTitle = composeTestRule.activity.getString(
            com.gustavo.brilhante.tasklist.R.string.empty_tasks_title
        )
        composeTestRule.onNodeWithTag(TestTags.INPUT_TASK_EDITOR_TITLE).performTextInput("Walk the dog")
        composeTestRule.onNodeWithTag(TestTags.BTN_TASK_EDITOR_DONE).performClick()
        waitUntilDisplayed("Walk the dog")
        composeTestRule.onNodeWithText("Walk the dog").assertIsDisplayed()
        composeTestRule.onAllNodesWithText(emptyTitle).assertCountEquals(0)
    }

    @Test
    fun existingTask_loadsTitleInEditorForEditing() {
        val taskTitle = "Existing task"

        composeTestRule.onNodeWithTag(TestTags.INPUT_TASK_EDITOR_TITLE).performTextInput(taskTitle)
        composeTestRule.onNodeWithTag(TestTags.BTN_TASK_EDITOR_DONE).performClick()

        waitUntilDisplayed(taskTitle)
        composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()

        composeTestRule.onNodeWithText(taskTitle).performClick()
        composeTestRule.onNodeWithText(editScreenTitle).assertIsDisplayed()

        waitUntilTextFieldHasText(taskTitle)
        composeTestRule.onNode(hasTestTag(TestTags.INPUT_TASK_EDITOR_TITLE).and(hasText(taskTitle))).assertIsDisplayed()
    }

    @Test
    fun flagToggle_remainsDisplayedAfterClick() {
        composeTestRule.onNodeWithTag(TestTags.TOGGLE_TASK_FLAGGED).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.TOGGLE_TASK_FLAGGED).performClick()
        composeTestRule.onNodeWithTag(TestTags.TOGGLE_TASK_FLAGGED).assertIsDisplayed()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun waitUntilDisplayed(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitUntilTextFieldHasText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasTestTag(TestTags.INPUT_TASK_EDITOR_TITLE).and(hasText(text)))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
