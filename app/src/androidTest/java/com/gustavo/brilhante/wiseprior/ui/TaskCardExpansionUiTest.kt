package com.gustavo.brilhante.wiseprior.ui

import android.Manifest
import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertIsDisplayed
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
 * Instrumented tests validating that TaskCard expansion state is preserved across
 * configuration changes (screen rotation).
 *
 * Before this refactor [TaskCard] held expansion state in `remember { mutableStateOf }`,
 * which caused it to be silently discarded on rotation. The state now lives in
 * [TaskListViewModel.expandedTaskIds], which survives the configuration change.
 */
@HiltAndroidTest
class TaskCardExpansionUiTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var addReminderCd: String
    private lateinit var doneLabel: String
    private lateinit var expandCd: String
    private lateinit var collapseCd: String
    private lateinit var emptyTitle: String

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.activity.run {
            addReminderCd = getString(com.gustavo.brilhante.tasklist.R.string.add_task_button_description)
            doneLabel = getString(com.gustavo.brilhante.taskeditor.R.string.editor_done)
            expandCd = getString(com.gustavo.brilhante.ui.R.string.task_card_expand)
            collapseCd = getString(com.gustavo.brilhante.ui.R.string.task_card_collapse)
            emptyTitle = getString(com.gustavo.brilhante.tasklist.R.string.empty_tasks_title)
        }
    }

    /**
     * Expand a card in portrait → rotate to landscape → card must still be expanded.
     *
     * This test FAILS if expansion state lives in `remember { mutableStateOf }` because
     * the activity is recreated on rotation and local Compose state is lost.
     */
    @Test
    fun expandedCard_survivesRotationToLandscape() {
        val notes = "These notes prove the card is expanded"
        createTaskWithNotes(title = "Rotate me", notes = notes)

        // Force portrait to start from a known orientation
        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        composeTestRule.waitForIdle()

        // Expand the card
        waitUntilCdExists(expandCd)
        composeTestRule.onNodeWithContentDescription(expandCd).performClick()
        
        // Wait for notes to appear (expansion animation)
        waitUntilDisplayed(notes)
        composeTestRule.onNodeWithText(notes).assertIsDisplayed()

        // Rotate to landscape — ViewModel survives, local remember state would not
        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeTestRule.waitForIdle()

        // Notes must still be visible because expansion state lives in the ViewModel
        // We use waitUntilDisplayed again to account for recreation time
        waitUntilDisplayed(notes)
        composeTestRule.onNodeWithText(notes).assertIsDisplayed()
    }

    /**
     * Collapse a card in portrait → rotate to landscape → card must still be collapsed.
     */
    @Test
    fun collapsedCard_survivesRotationToLandscape() {
        val notes = "Notes visible only when expanded"
        createTaskWithNotes(title = "Stay collapsed", notes = notes)

        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        composeTestRule.waitForIdle()

        // Expand then collapse
        waitUntilCdExists(expandCd)
        composeTestRule.onNodeWithContentDescription(expandCd).performClick()
        waitUntilDisplayed(notes)
        
        waitUntilCdExists(collapseCd)
        composeTestRule.onNodeWithContentDescription(collapseCd).performClick()
        
        // Wait for notes to disappear
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(notes)).fetchSemanticsNodes().isEmpty()
        }

        // Rotate to landscape
        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeTestRule.waitForIdle()

        // Notes must NOT be visible — collapsed state survived rotation
        val notesNodes = composeTestRule.onAllNodes(hasText(notes)).fetchSemanticsNodes()
        assert(notesNodes.isEmpty()) { "Expected notes to be hidden after collapse + rotation" }
    }

    /**
     * Regression guard: if the expand button is missing (e.g. the ExpandButton composable
     * was accidentally removed), this test will fail before the rotation assertions are reached.
     */
    @Test
    fun expandButton_isVisibleForTaskWithNotes() {
        createTaskWithNotes(title = "Expandable task", notes = "Notes make this expandable")

        waitUntilCdExists(expandCd)
        composeTestRule.onNodeWithContentDescription(expandCd).assertIsDisplayed()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun createTaskWithNotes(title: String, notes: String) {
        waitUntilDisplayed(emptyTitle)
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()

        val fields = composeTestRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput(title)
        fields[1].performTextInput(notes)

        composeTestRule.onNodeWithText(doneLabel).performClick()

        waitUntilDisplayed(title)
    }

    private fun waitUntilDisplayed(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitUntilCdExists(contentDesc: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasContentDescription(contentDesc))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
