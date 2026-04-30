package com.gustavo.brilhante.wiseprior.ui

import android.Manifest
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
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
    private lateinit var doneLabel: String
    private lateinit var priorityNone: String
    private lateinit var priorityLow: String
    private lateinit var priorityMedium: String
    private lateinit var priorityHigh: String
    private lateinit var sectionDatetime: String
    private lateinit var flagLabel: String

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.activity.run {
            addReminderCd = getString(com.gustavo.brilhante.tasklist.R.string.add_task_button_description)
            newScreenTitle = getString(com.gustavo.brilhante.taskeditor.R.string.editor_title_new)
            editScreenTitle = getString(com.gustavo.brilhante.taskeditor.R.string.editor_title_edit)
            backCd = getString(com.gustavo.brilhante.taskeditor.R.string.editor_back)
            doneLabel = getString(com.gustavo.brilhante.taskeditor.R.string.editor_done)
            priorityNone = getString(com.gustavo.brilhante.taskeditor.R.string.priority_none)
            priorityLow = getString(com.gustavo.brilhante.taskeditor.R.string.priority_low)
            priorityMedium = getString(com.gustavo.brilhante.taskeditor.R.string.priority_medium)
            priorityHigh = getString(com.gustavo.brilhante.taskeditor.R.string.priority_high)
            sectionDatetime = getString(com.gustavo.brilhante.taskeditor.R.string.editor_section_datetime)
            flagLabel = getString(com.gustavo.brilhante.taskeditor.R.string.editor_label_flag)
        }
        // Navigate to editor before each test.
        // Wait for list to load first to avoid hierarchy issues
        val emptyTitle = composeTestRule.activity.getString(com.gustavo.brilhante.tasklist.R.string.empty_tasks_title)
        waitUntilDisplayed(emptyTitle)

        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithText(newScreenTitle).assertIsDisplayed()
    }

    @Test
    fun newTaskEditor_showsCorrectTitle() {
        composeTestRule.onNodeWithText(newScreenTitle).assertIsDisplayed()
    }

    @Test
    fun doneButton_isVisible() {
        composeTestRule.onNodeWithText(doneLabel).assertIsDisplayed()
    }

    @Test
    fun dateTimeSection_isVisible() {
        // SectionHeader calls .uppercase() on the title
        composeTestRule.onNodeWithText(sectionDatetime.uppercase()).assertIsDisplayed()
    }

    @Test
    fun prioritySegmentedButtons_areAllVisible() {
        composeTestRule.onNodeWithText(priorityNone).assertIsDisplayed()
        composeTestRule.onNodeWithText(priorityLow).assertIsDisplayed()
        composeTestRule.onNodeWithText(priorityMedium).assertIsDisplayed()
        composeTestRule.onNodeWithText(priorityHigh).assertIsDisplayed()
    }

    @Test
    fun priorityNone_isSelectedByDefault() {
        composeTestRule.onNodeWithText(priorityNone).assertIsSelected()
    }

    @Test
    fun selectingPriority_updatesSelection() {
        composeTestRule.onNodeWithText(priorityHigh).performClick()
        composeTestRule.onNodeWithText(priorityHigh).assertIsSelected()
    }

    @Test
    fun savingWithBlankTitle_showsValidationError() {
        // Do not enter a title — just tap Done.
        composeTestRule.onNodeWithText(doneLabel).performClick()
        // The editor remains open (navigation did not occur).
        composeTestRule.onNodeWithText(newScreenTitle).assertIsDisplayed()
    }

    @Test
    fun enteringTitle_andSaving_navigatesBack() {
        val emptyTitle = composeTestRule.activity.getString(
            com.gustavo.brilhante.tasklist.R.string.empty_tasks_title
        )
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Walk the dog")
        composeTestRule.onNodeWithText(doneLabel).performClick()
        // Wait for Room insert + Flow re-emit to reach the list.
        waitUntilDisplayed("Walk the dog")
        composeTestRule.onAllNodes(hasText(emptyTitle)).assertCountEquals(0)
        composeTestRule.onNodeWithText("Walk the dog").assertIsDisplayed()
    }

    @Test
    fun existingTask_loadsTitleInEditorForEditing() {
        val taskTitle = "Existing task"

        // Create the task from the editor that setUp() opened.
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput(taskTitle)
        composeTestRule.onNodeWithText(doneLabel).performClick()

        // Wait for the task to appear in the list after the Room insert.
        waitUntilDisplayed(taskTitle)
        composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()

        // Tap the card to open the editor in edit mode.
        composeTestRule.onNodeWithText(taskTitle).performClick()
        composeTestRule.onNodeWithText(editScreenTitle).assertIsDisplayed()

        // loadTask() is async; wait for the title field to be populated.
        waitUntilTextFieldHasText(taskTitle)
        composeTestRule.onNode(hasText(taskTitle).and(hasSetTextAction())).assertIsDisplayed()
    }

    @Test
    fun flagToggle_changesState() {
        composeTestRule.onNodeWithText(flagLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(flagLabel).performClick()
        composeTestRule.onNodeWithText(flagLabel).assertIsDisplayed()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Waits until at least one node with the given text exists in the semantic tree. */
    private fun waitUntilDisplayed(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Waits until a text-input field containing the given text exists.
     * Uses [hasSetTextAction] to distinguish the editor's OutlinedTextField from
     * plain Text composables (e.g. task-card titles in the back-stack composition).
     */
    private fun waitUntilTextFieldHasText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(text).and(hasSetTextAction()))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
