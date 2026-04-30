package com.gustavo.brilhante.wiseprior.e2e

import android.Manifest
import androidx.compose.ui.test.assertCountEquals
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
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.gustavo.brilhante.wiseprior.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * End-to-End test covering the full task lifecycle:
 *   Open app → Create task → Edit task → Mark complete → Verify UI state
 *
 * Uses an in-memory Room database so there is no real data on disk and no
 * cross-test pollution. The flow exercises the real Hilt DI graph, ViewModel,
 * use cases, and Room DAOs — only the persistence layer is swapped for in-memory.
 */
@HiltAndroidTest
class CreateAndCompleteTaskE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var addReminderCd: String
    private lateinit var emptyStateTitle: String
    private lateinit var newScreenTitle: String
    private lateinit var editScreenTitle: String
    private lateinit var backCd: String
    private lateinit var doneLabel: String
    private lateinit var markCompleteCd: String
    private lateinit var markIncompleteCd: String
    private lateinit var priorityHigh: String

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.activity.run {
            addReminderCd = getString(com.gustavo.brilhante.tasklist.R.string.add_task_button_description)
            emptyStateTitle = getString(com.gustavo.brilhante.tasklist.R.string.empty_tasks_title)
            newScreenTitle = getString(com.gustavo.brilhante.taskeditor.R.string.editor_title_new)
            editScreenTitle = getString(com.gustavo.brilhante.taskeditor.R.string.editor_title_edit)
            backCd = getString(com.gustavo.brilhante.taskeditor.R.string.editor_back)
            doneLabel = getString(com.gustavo.brilhante.taskeditor.R.string.editor_done)
            markCompleteCd = getString(com.gustavo.brilhante.ui.R.string.task_card_mark_complete)
            markIncompleteCd = getString(com.gustavo.brilhante.ui.R.string.task_card_mark_incomplete)
            priorityHigh = getString(com.gustavo.brilhante.taskeditor.R.string.priority_high)
        }
    }

    /**
     * Full lifecycle: create → verify → edit title & priority → verify changes → complete → verify.
     *
     * Step 1: App opens → empty state is shown.
     * Step 2: Tap FAB → task editor opens for a new task.
     * Step 3: Enter title "Buy coffee" → tap Done → task appears in list.
     * Step 4: Tap the task card → task editor opens in edit mode with title pre-filled.
     * Step 5: Change title to "Buy coffee and milk" and set priority to High → tap Done.
     * Step 6: Updated title appears in list.
     * Step 7: Tap the checkbox → task is marked complete.
     * Step 8: Checkbox switches to the "mark incomplete" content description.
     */
    @Test
    fun fullTaskLifecycle_createEditComplete() {
        val originalTitle = "Buy coffee"
        val updatedTitle = "Buy coffee and milk"

        // ── Step 1: App launches with empty state ─────────────────────────────
        // isLoading starts as true; wait for Room's first emission to clear it.
        waitUntilDisplayed(emptyStateTitle)
        composeTestRule.onNodeWithText(emptyStateTitle).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(addReminderCd).assertIsDisplayed()

        // ── Step 2: Open task editor for a new task ───────────────────────────
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithText(newScreenTitle).assertIsDisplayed()

        // ── Step 3: Fill in title and save ────────────────────────────────────
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput(originalTitle)
        composeTestRule.onNodeWithText(doneLabel).performClick()

        // Wait for Room insert + Flow re-emit to reach the list.
        waitUntilDisplayed(originalTitle)
        composeTestRule.onNodeWithText(originalTitle).assertIsDisplayed()
        composeTestRule.onAllNodes(hasText(emptyStateTitle)).assertCountEquals(0)

        // ── Step 4: Open the task in edit mode ───────────────────────────────
        composeTestRule.onNodeWithText(originalTitle).performClick()
        composeTestRule.onNodeWithText(editScreenTitle).assertIsDisplayed()
        // loadTask() is async; wait for the title field to be populated.
        waitUntilTextFieldHasText(originalTitle)
        composeTestRule.onNode(hasText(originalTitle).and(hasSetTextAction())).assertIsDisplayed()

        // ── Step 5: Change title and set priority ────────────────────────────
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextClearance()
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput(updatedTitle)
        composeTestRule.onNodeWithText(priorityHigh).performClick()
        composeTestRule.onNodeWithText(doneLabel).performClick()

        // ── Step 6: Updated title is visible; original title is gone ─────────
        waitUntilDisplayed(updatedTitle)
        composeTestRule.onNodeWithText(updatedTitle).assertIsDisplayed()
        composeTestRule.onAllNodes(hasText(originalTitle)).assertCountEquals(0)

        // ── Step 7: Complete the task via checkbox ────────────────────────────
        composeTestRule.onNodeWithContentDescription(markCompleteCd).assertIsOff()
        composeTestRule.onNodeWithContentDescription(markCompleteCd).performClick()

        // ── Step 8: Wait for Room update + re-emit; checkbox is now "incomplete" ──
        waitUntilCdExists(markIncompleteCd)
        composeTestRule.onNodeWithContentDescription(markIncompleteCd).assertIsOn()
    }

    /**
     * Validates that saving with an empty title keeps the user on the editor screen.
     * Regression guard: we must not create a task without a title.
     */
    @Test
    fun emptyTitle_preventsNavigation_andKeepsEditorOpen() {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithText(newScreenTitle).assertIsDisplayed()

        composeTestRule.onNodeWithText(doneLabel).performClick()

        composeTestRule.onNodeWithText(newScreenTitle).assertIsDisplayed()
    }

    /**
     * Validates that the back button from the editor never creates a task.
     */
    @Test
    fun backWithoutSaving_doesNotCreateTask() {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Unsaved task")
        composeTestRule.onNodeWithContentDescription(backCd).performClick()

        // Back in the list — wait for the empty state to be visible.
        waitUntilDisplayed(emptyStateTitle)
        composeTestRule.onNodeWithText(emptyStateTitle).assertIsDisplayed()
        composeTestRule.onAllNodes(hasText("Unsaved task")).assertCountEquals(0)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Waits until at least one node with the given text exists in the semantic tree. */
    private fun waitUntilDisplayed(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Waits until a text-input field (hasSetTextAction) containing the given text exists.
     * More specific than [waitUntilDisplayed] — ignores non-editable Text nodes such as
     * task-card titles that may linger in the back-stack composition.
     */
    private fun waitUntilTextFieldHasText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(text).and(hasSetTextAction()))
                .fetchSemanticsNodes().isNotEmpty()
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
