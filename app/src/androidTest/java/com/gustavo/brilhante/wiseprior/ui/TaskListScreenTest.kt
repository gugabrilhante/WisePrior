package com.gustavo.brilhante.wiseprior.ui

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
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
        // isLoading starts as true; wait for Room's first emission to clear it.
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
        composeTestRule.onNodeWithText(newReminderScreenTitle).assertIsDisplayed()
    }

    @Test
    fun backButtonInEditor_returnsToTaskList() {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithText(newReminderScreenTitle).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(backCd).performClick()

        // Empty state is visible (isLoading resolved while the editor was open).
        waitUntilDisplayed(emptyTitle)
        composeTestRule.onNodeWithText(emptyTitle).assertIsDisplayed()
    }

    @Test
    fun savedTask_appearsInList() {
        val taskTitle = "Buy groceries"
        createTask(taskTitle)
        // createTask already waits; assert for clarity.
        composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()
    }

    @Test
    fun checkboxToggle_marksTaskComplete_thenIncomplete() {
        createTask("Pick up package")

        // Starts unchecked.
        composeTestRule.onNodeWithContentDescription(markCompleteCd).assertIsOff()

        // Mark as complete; wait for Room update + Flow re-emit.
        composeTestRule.onNodeWithContentDescription(markCompleteCd).performClick()
        waitUntilCdExists(markIncompleteCd)
        composeTestRule.onNodeWithContentDescription(markIncompleteCd).assertIsOn()

        // Mark as incomplete again.
        composeTestRule.onNodeWithContentDescription(markIncompleteCd).performClick()
        waitUntilCdExists(markCompleteCd)
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

    /**
     * Creates a task and waits until it appears in the list before returning.
     * Guards against the race between Room's insert, Flow re-emit, and the assertion.
     */
    private fun createTask(title: String) {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput(title)
        composeTestRule.onNodeWithText(doneLabel).performClick()
        waitUntilDisplayed(title)
    }

    /** Waits until at least one node with the given text exists in the semantic tree. */
    private fun waitUntilDisplayed(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** Waits until at least one node with the given content description exists. */
    private fun waitUntilCdExists(contentDesc: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasContentDescription(contentDesc))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
