package com.gustavo.brilhante.wiseprior.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var addReminderCd: String
    private lateinit var emptyTitle: String
    private lateinit var newReminderScreenTitle: String
    private lateinit var backCd: String
    private lateinit var doneLabel: String
    private lateinit var markCompleteCd: String
    private lateinit var markIncompleteCd: String

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.activity.run {
            addReminderCd = getString(com.gustavo.brilhante.tasklist.R.string.add_task_button_description)
            emptyTitle = getString(com.gustavo.brilhante.tasklist.R.string.empty_tasks_title)
            newReminderScreenTitle = getString(com.gustavo.brilhante.taskeditor.R.string.editor_title_new)
            backCd = getString(com.gustavo.brilhante.taskeditor.R.string.editor_back)
            doneLabel = getString(com.gustavo.brilhante.taskeditor.R.string.editor_done)
            markCompleteCd = getString(com.gustavo.brilhante.ui.R.string.task_card_mark_complete)
            markIncompleteCd = getString(com.gustavo.brilhante.ui.R.string.task_card_mark_incomplete)
        }
    }

    @Test
    fun emptyState_isDisplayed_whenNoTasksExist() {
        composeTestRule.onNodeWithText(emptyTitle).assertIsDisplayed()
    }

    @Test
    fun addReminderFab_isAlwaysVisible() {
        composeTestRule.onNodeWithContentDescription(addReminderCd).assertIsDisplayed()
    }

    @Test
    fun clickingFab_navigatesToTaskEditor() {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithText(newReminderScreenTitle).assertIsDisplayed()
    }

    @Test
    fun backButtonInEditor_returnsToTaskList() {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithText(newReminderScreenTitle).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(backCd).performClick()

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

        // Starts unchecked
        composeTestRule.onNodeWithContentDescription(markCompleteCd).assertIsOff()

        // Mark as complete
        composeTestRule.onNodeWithContentDescription(markCompleteCd).performClick()
        composeTestRule.onNodeWithContentDescription(markIncompleteCd).assertIsOn()

        // Mark as incomplete again
        composeTestRule.onNodeWithContentDescription(markIncompleteCd).performClick()
        composeTestRule.onNodeWithContentDescription(markCompleteCd).assertIsOff()
    }

    @Test
    fun taskCard_click_navigatesToEditorInEditMode() {
        val taskTitle = "Review PR"
        val editTitle = composeTestRule.activity.getString(
            com.gustavo.brilhante.taskeditor.R.string.editor_title_edit
        )
        createTask(taskTitle)

        composeTestRule.onNodeWithText(taskTitle).performClick()

        composeTestRule.onNodeWithText(editTitle).assertIsDisplayed()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun createTask(title: String) {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        // The first editable text field is the title field
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput(title)
        composeTestRule.onNodeWithText(doneLabel).performClick()
    }
}
