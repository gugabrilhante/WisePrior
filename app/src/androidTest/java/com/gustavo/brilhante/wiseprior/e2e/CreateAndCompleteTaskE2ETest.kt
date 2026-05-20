package com.gustavo.brilhante.wiseprior.e2e

import android.Manifest
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
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
    private lateinit var editScreenTitle: String
    private lateinit var backCd: String
    private lateinit var markCompleteCd: String
    private lateinit var markIncompleteCd: String

    @Before
    fun setUp() {
        hiltRule.inject()
        // Wait for activity to be ready and avoid IllegalStateException accessing it too early
        composeTestRule.waitForIdle()
        val activity = composeTestRule.activity
        addReminderCd = activity.getString(com.gustavo.brilhante.tasklist.R.string.add_task_button_description)
        emptyStateTitle = activity.getString(com.gustavo.brilhante.tasklist.R.string.empty_tasks_title)
        editScreenTitle = activity.getString(com.gustavo.brilhante.taskeditor.R.string.editor_title_edit)
        backCd = activity.getString(com.gustavo.brilhante.taskeditor.R.string.editor_back)
        markCompleteCd = activity.getString(com.gustavo.brilhante.ui.R.string.task_card_mark_complete)
        markIncompleteCd = activity.getString(com.gustavo.brilhante.ui.R.string.task_card_mark_incomplete)
    }

    /**
     * Full lifecycle: create → verify → edit title & priority → verify changes → complete → verify.
     */
    @Test
    fun fullTaskLifecycle_createEditComplete() {
        val originalTitle = "Buy coffee"
        val updatedTitle = "Buy coffee and milk"

        // ── Step 1: App launches with empty state ─────────────────────────────
        waitUntilDisplayed(emptyStateTitle)
        composeTestRule.onNodeWithText(emptyStateTitle).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(addReminderCd).assertIsDisplayed()

        // ── Step 2: Open task editor for a new task ───────────────────────────
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithTag(TestTags.SCREEN_TASK_EDITOR).assertIsDisplayed()

        // ── Step 3: Fill in title and save ────────────────────────────────────
        composeTestRule.onNodeWithTag(TestTags.INPUT_TASK_EDITOR_TITLE).performTextInput(originalTitle)
        composeTestRule.onNodeWithTag(TestTags.BTN_TASK_EDITOR_DONE).performClick()

        waitUntilDisplayed(originalTitle)
        composeTestRule.onNodeWithText(originalTitle).assertIsDisplayed()
        composeTestRule.onAllNodes(hasText(emptyStateTitle)).assertCountEquals(0)

        // ── Step 4: Open the task in edit mode ───────────────────────────────
        composeTestRule.onNodeWithText(originalTitle).performClick()
        waitUntilTextFieldHasText(originalTitle)
        composeTestRule.onNodeWithText(editScreenTitle).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag(TestTags.INPUT_TASK_EDITOR_TITLE).and(hasText(originalTitle))).assertIsDisplayed()

        // ── Step 5: Change title and set priority ────────────────────────────
        composeTestRule.onNodeWithTag(TestTags.INPUT_TASK_EDITOR_TITLE).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.INPUT_TASK_EDITOR_TITLE).performTextInput(updatedTitle)
        composeTestRule.onNodeWithTag(TestTags.SEGMENT_PRIORITY_HIGH).performClick()
        composeTestRule.onNodeWithTag(TestTags.BTN_TASK_EDITOR_DONE).performClick()

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
     */
    @Test
    fun emptyTitle_preventsNavigation_andKeepsEditorOpen() {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithTag(TestTags.SCREEN_TASK_EDITOR).assertIsDisplayed()

        composeTestRule.onNodeWithTag(TestTags.BTN_TASK_EDITOR_DONE).performClick()

        composeTestRule.onNodeWithTag(TestTags.SCREEN_TASK_EDITOR).assertIsDisplayed()
    }

    /**
     * Validates that the back button from the editor never creates a task.
     */
    @Test
    fun backWithoutSaving_doesNotCreateTask() {
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.onNodeWithTag(TestTags.INPUT_TASK_EDITOR_TITLE).performTextInput("Unsaved task")
        composeTestRule.onNodeWithContentDescription(backCd).performClick()

        waitUntilDisplayed(emptyStateTitle)
        composeTestRule.onNodeWithText(emptyStateTitle).assertIsDisplayed()
        composeTestRule.onAllNodes(hasText("Unsaved task")).assertCountEquals(0)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun waitUntilDisplayed(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitUntilTextFieldHasText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule.onAllNodes(hasTestTag(TestTags.INPUT_TASK_EDITOR_TITLE).and(hasText(text)))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitUntilCdExists(contentDesc: String) {
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule.onAllNodes(hasContentDescription(contentDesc))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
