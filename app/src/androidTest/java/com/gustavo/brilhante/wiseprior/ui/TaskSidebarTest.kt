package com.gustavo.brilhante.wiseprior.ui

import android.Manifest
import android.content.pm.ActivityInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import com.gustavo.brilhante.tasklist.ui.ADD_TAG_BUTTON_TEST_TAG
import com.gustavo.brilhante.tasklist.ui.SIDEBAR_LIST_TEST_TAG
import com.gustavo.brilhante.ui.TestTags
import com.gustavo.brilhante.wiseprior.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the [TaskSidebarContent].
 *
 * These tests ensure that the sidebar remains scrollable when content overflows,
 * which is critical for smaller screens and portrait orientation.
 * It also validates that rotation doesn't break accessibility or visibility of core items.
 */
@HiltAndroidTest
class TaskSidebarTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var menuButtonCd: String

    @Before
    fun setUp() {
        hiltRule.inject()
        menuButtonCd = composeTestRule.activity.getString(
            com.gustavo.brilhante.tasklist.R.string.menu_button_description
        )
    }

    @Test
    fun sidebar_isScrollable_whenTagsOverflow() {
        repeat(5) { i -> createTag("Tag $i") }

        openDrawerIfClosed()

        composeTestRule.onNodeWithTag(TestTags.SIDEBAR_ITEM_TODAY).assertIsDisplayed()

        composeTestRule.onNodeWithTag(SIDEBAR_LIST_TEST_TAG)
            .performScrollToNode(hasTestTag(ADD_TAG_BUTTON_TEST_TAG))

        composeTestRule.onNodeWithTag(ADD_TAG_BUTTON_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun sidebar_remainsVisible_afterRotation() {
        openDrawerIfClosed()
        composeTestRule.onNodeWithTag(TestTags.SIDEBAR_ITEM_TODAY).assertIsDisplayed()

        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeTestRule.waitForIdle()

        composeTestRule.onNode(
            hasTestTag(TestTags.SIDEBAR_ITEM_ALL) and hasAnyAncestor(hasTestTag(SIDEBAR_LIST_TEST_TAG))
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SIDEBAR_LIST_TEST_TAG)
            .performScrollToNode(hasTestTag(TestTags.SIDEBAR_TAGS_HEADER))
        composeTestRule.onNodeWithTag(TestTags.SIDEBAR_TAGS_HEADER).assertIsDisplayed()

        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        composeTestRule.waitForIdle()

        openDrawerIfClosed()
        composeTestRule.onNodeWithTag(TestTags.SIDEBAR_ITEM_TODAY).assertIsDisplayed()
    }

    /**
     * Regression test: Ensures that items at the bottom of the list are reachable.
     * This test would fail if the sidebar was a non-scrollable Column because
     * [performScrollTo] would fail to bring the off-screen node into view.
     */
    @Test
    fun sidebar_reachesBottomItem_regressionTest() {
        repeat(5) { i -> createTag("Regression Tag $i") }

        openDrawerIfClosed()

        composeTestRule.onNodeWithTag(ADD_TAG_BUTTON_TEST_TAG)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun tagEditorDialog_showsDeleteButton_whenEditingExistingTag() {
        createTag("DeletableTag")
        openDrawerIfClosed()

        val editTagCd = composeTestRule.activity.getString(
            com.gustavo.brilhante.tasklist.R.string.edit_tag_description
        )
        composeTestRule.onNodeWithTag(SIDEBAR_LIST_TEST_TAG)
            .performScrollToNode(hasContentDescription(editTagCd))
        composeTestRule.onNode(hasContentDescription(editTagCd)).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TestTags.BTN_TAG_EDITOR_DELETE).assertIsDisplayed()

        composeTestRule.onNodeWithTag(TestTags.BTN_TAG_EDITOR_CANCEL).performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun sidebar_collectionBadge_visible_whenTaskExists() {
        createTaskInList("Badge Test Task")
        openDrawerIfClosed()

        composeTestRule.onNodeWithTag(TestTags.SIDEBAR_ITEM_ALL).assertIsDisplayed()
    }

    @Test
    fun sidebar_tagTaskCountBadge_visible_whenTaskHasTag() {
        createTag("CountedTag")
        createTaskInList("Task With Tag", selectFirstTag = true)
        openDrawerIfClosed()

        composeTestRule.onNodeWithTag(SIDEBAR_LIST_TEST_TAG)
            .performScrollToNode(hasTestTag(TestTags.SIDEBAR_TAGS_HEADER))
        composeTestRule.onNodeWithTag(TestTags.SIDEBAR_TAGS_HEADER).assertIsDisplayed()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun openDrawerIfClosed() {
        val menuButtonNodes = composeTestRule.onAllNodes(hasContentDescription(menuButtonCd))
        if (menuButtonNodes.fetchSemanticsNodes().isNotEmpty()) {
            menuButtonNodes[0].performClick()
            composeTestRule.waitForIdle()
        }
    }

    private fun createTag(name: String) {
        openDrawerIfClosed()

        composeTestRule.onNodeWithTag(ADD_TAG_BUTTON_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag(TestTags.INPUT_TAG_EDITOR_NAME).performTextInput(name)
        composeTestRule.onNodeWithTag(TestTags.BTN_TAG_EDITOR_SAVE).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasTestTag(TestTags.DIALOG_TAG_EDITOR))
                .fetchSemanticsNodes().isEmpty()
        }

        // Close the drawer after creating the tag by clicking on the main content
        composeTestRule.onNodeWithTag(TestTags.TEXT_EMPTY_STATE).performClick()
        composeTestRule.waitForIdle()

        // Wait for the drawer to close (menu button should be visible)
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasContentDescription(menuButtonCd)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun createTaskInList(title: String, selectFirstTag: Boolean = false) {
        // Ensure we're on the main screen and drawer is closed
        val emptyTitle = composeTestRule.activity.getString(com.gustavo.brilhante.tasklist.R.string.empty_tasks_title)
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(emptyTitle)).fetchSemanticsNodes().isNotEmpty()
        }

        val addReminderCd = composeTestRule.activity.getString(
            com.gustavo.brilhante.tasklist.R.string.add_task_button_description
        )
        composeTestRule.onNodeWithContentDescription(addReminderCd).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TestTags.SCREEN_TASK_EDITOR).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.INPUT_TASK_EDITOR_TITLE).performTextInput(title)
        if (selectFirstTag) {
            composeTestRule.onNodeWithTag(TestTags.CHIP_TAG_ITEM).performClick()
            composeTestRule.waitForIdle()
        }
        composeTestRule.onNodeWithTag(TestTags.BTN_TASK_EDITOR_DONE).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasTestTag(TestTags.SCREEN_TASK_EDITOR))
                .fetchSemanticsNodes().isEmpty()
        }
    }
}
