package com.gustavo.brilhante.wiseprior.ui

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
 * UI tests for the Task List screen covering task creation, completion toggle,
 * and navigation. Uses an in-memory database via [com.gustavo.brilhante.wiseprior.di.TestDatabaseModule]
 * so each test starts clean.
 */
@HiltAndroidTest
class TaskListScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var addReminderCd: String
    private lateinit var emptyTitle: String
    private lateinit var newReminderScreenTitle: String
    private lateinit var editTitle: String
    private lateinit var markCompleteCd: String
    private lateinit var markIncompleteCd: String

    @Before
    fun setUp() {
        hiltRule.inject()
        // Ensure activity is ready
        composeTestRule.waitForIdle()
        val activity = composeTestRule.activity
        addReminderCd = activity.getString(com.gustavo.brilhante.tasklist.R.string.add_task_button_description)
        emptyTitle = activity.getString(com.gustavo.brilhante.tasklist.R.string.empty_tasks_title)
        newReminderScreenTitle = activity.getString(com.gustavo.brilhante.taskeditor.R.string.editor_title_new)
        editTitle = activity.getString(com.gustavo.brilhante.taskeditor.R.string.editor_title_edit)
        markCompleteCd = activity.getString(com.gustavo.brilhante.ui.R.string.task_card_mark_complete)
        markIncompleteCd = activity.getString(com.gustavo.brilhante.ui.R.string.task_card_mark_incomplete)
    }

    @Test
    fun emptyState_isDisplayed_whenNoTasksExist() {
        waitUntilDisplayed(emptyTitle)
        composeTestRule.onNodeWithText(emptyTitle).assertIsDisplayed()
    }

    @Test
    fun addReminderFab_isAlwaysVisible() {
        composeTestRule.onNodeWithContentDescription(addReminderCd).assertIsDisplayed()
    }

    @Test
    fun clickingFab_navigatesToTaskEditor() {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithTag(TestTags.SCREEN_TASK_EDITOR).assertIsDisplayed()
    }

    @Test
    fun backButtonInEditor_returnsToTaskList() {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithTag(TestTags.SCREEN_TASK_EDITOR).assertIsDisplayed()

        composeTestRule.onNodeWithTag(TestTags.BTN_TASK_EDITOR_BACK).performClick()

        waitUntilDisplayed(emptyTitle)
        composeTestRule.onNodeWithText(emptyTitle).assertIsDisplayed()
    }

    @Test
    fun savedTask_appearsInList() {
        val taskTitle = "Buy groceries"
        createTask(taskTitle)
        composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()
    }

    @Test
    fun checkboxToggle_marksTaskComplete_thenIncomplete() {
        createTask("Pick up package")

        composeTestRule.onNodeWithContentDescription(markCompleteCd).assertIsOff()

        composeTestRule.onNodeWithContentDescription(markCompleteCd).performClick()
        waitUntilCdExists(markIncompleteCd)
        composeTestRule.onNodeWithContentDescription(markIncompleteCd).assertIsOn()

        composeTestRule.onNodeWithContentDescription(markIncompleteCd).performClick()
        waitUntilCdExists(markCompleteCd)
        composeTestRule.onNodeWithContentDescription(markCompleteCd).assertIsOff()
    }

    @Test
    fun taskCard_click_navigatesToEditorInEditMode() {
        val taskTitle = "Review PR"
        createTask(taskTitle)

        composeTestRule.onNodeWithText(taskTitle).performClick()

        composeTestRule.onNodeWithText(editTitle).assertIsDisplayed()
    }

    @Test
    fun sortButton_isVisible() {
        val sortCd = composeTestRule.activity.getString(
            com.gustavo.brilhante.tasklist.R.string.sort_button_description
        )
        composeTestRule.onNodeWithContentDescription(sortCd).assertIsDisplayed()
    }

    @Test
    fun sortDropdown_opensOnClick() {
        val sortCd = composeTestRule.activity.getString(
            com.gustavo.brilhante.tasklist.R.string.sort_button_description
        )
        val newestLabel = composeTestRule.activity.getString(
            com.gustavo.brilhante.tasklist.R.string.sort_created_newest
        )
        composeTestRule.onNodeWithContentDescription(sortCd).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(newestLabel).assertIsDisplayed()
    }

    @Test
    fun existingTask_canBeCheckedAndUnchecked() {
        val taskTitle = "Toggle me"
        createTask(taskTitle)

        // Complete
        composeTestRule.onNodeWithContentDescription(markCompleteCd).performClick()
        waitUntilCdExists(markIncompleteCd)

        // Uncomplete
        composeTestRule.onNodeWithContentDescription(markIncompleteCd).performClick()
        waitUntilCdExists(markCompleteCd)
        composeTestRule.onNodeWithContentDescription(markCompleteCd).assertIsDisplayed()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun createTask(title: String) {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithTag(TestTags.INPUT_TASK_EDITOR_TITLE).performTextInput(title)
        composeTestRule.onNodeWithTag(TestTags.BTN_TASK_EDITOR_DONE).performClick()
        waitUntilDisplayed(title)
    }

    private fun waitUntilDisplayed(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitUntilCdExists(contentDesc: String) {
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule.onAllNodes(hasContentDescription(contentDesc))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
