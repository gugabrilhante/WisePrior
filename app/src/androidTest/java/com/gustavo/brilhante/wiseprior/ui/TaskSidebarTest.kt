package com.gustavo.brilhante.wiseprior.ui

import android.Manifest
import android.content.pm.ActivityInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import com.gustavo.brilhante.tasklist.ui.SIDEBAR_LIST_TEST_TAG
import com.gustavo.brilhante.tasklist.ui.TaskSidebarContent
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
    private lateinit var addTagLabel: String
    private lateinit var todayLabel: String
    private lateinit var allLabel: String
    private lateinit var tagHeaderLabel: String
    private lateinit var tagNameLabel: String
    private lateinit var saveButtonLabel: String

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.activity.run {
            menuButtonCd = getString(com.gustavo.brilhante.tasklist.R.string.menu_button_description)
            addTagLabel = getString(com.gustavo.brilhante.tasklist.R.string.add_tag_label)
            todayLabel = getString(com.gustavo.brilhante.tasklist.R.string.sidebar_today)
            allLabel = getString(com.gustavo.brilhante.tasklist.R.string.sidebar_all)
            tagHeaderLabel = getString(com.gustavo.brilhante.tasklist.R.string.sidebar_tags_header)
            tagNameLabel = getString(com.gustavo.brilhante.tasklist.R.string.tag_name_label)
            saveButtonLabel = getString(com.gustavo.brilhante.tasklist.R.string.save_button)
        }
    }

    @Test
    fun sidebar_isScrollable_whenTagsOverflow() {
        // Create multiple tags to ensure the sidebar list exceeds the screen height
        // (especially in landscape or on smaller devices).
        repeat(5) { i ->
            createTag("Tag $i")
        }

        openDrawerIfClosed()

        // 1. Verify the top item is visible initially
        composeTestRule.onNodeWithText(todayLabel).assertIsDisplayed()

        // 2. Scroll to the bottom item ("New Tag" button)
        // We use the testTag defined in TaskSidebar.kt for precision.
        composeTestRule.onNodeWithTag(SIDEBAR_LIST_TEST_TAG)
            .performScrollToNode(hasText(addTagLabel))

        // 3. Assert the bottom element is now visible
        composeTestRule.onNodeWithText(addTagLabel).assertIsDisplayed()
    }

    @Test
    fun sidebar_remainsVisible_afterRotation() {
        openDrawerIfClosed()
        composeTestRule.onNodeWithText(todayLabel).assertIsDisplayed()

        // Rotate to landscape
        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeTestRule.waitForIdle()

        // In landscape, check core elements. On many tablets/large screens, 
        // this might switch to a PermanentNavigationDrawer.
        composeTestRule.onNodeWithText(allLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(tagHeaderLabel).assertIsDisplayed()

        // Rotate back to portrait
        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        composeTestRule.waitForIdle()

        openDrawerIfClosed()
        composeTestRule.onNodeWithText(todayLabel).assertIsDisplayed()
    }

    /**
     * Regression test: Ensures that items at the bottom of the list are reachable.
     * This test would fail if the sidebar was a non-scrollable Column because
     * [performScrollTo] would fail to bring the off-screen node into view.
     */
    @Test
    fun sidebar_reachesBottomItem_regressionTest() {
        repeat(5) { i ->
            createTag("Regression Tag $i")
        }

        openDrawerIfClosed()

        // performScrollTo() works on any node that is a descendant of a scrollable container.
        composeTestRule.onNodeWithText(addTagLabel)
            .performScrollTo()
            .assertIsDisplayed()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun openDrawerIfClosed() {
        // In portrait mode, the Drawer starts closed and the Menu button is visible.
        // In landscape (expanded width), PermanentNavigationDrawer is used and there is NO Menu button.
        val menuButtonNodes = composeTestRule.onAllNodes(hasContentDescription(menuButtonCd))
        
        if (menuButtonNodes.fetchSemanticsNodes().isNotEmpty()) {
            menuButtonNodes[0].performClick()
            // Wait for drawer to open animation
            composeTestRule.waitForIdle()
        }
    }

    private fun createTag(name: String) {
        openDrawerIfClosed()

        // Click "New Tag" - might need to scroll if already many tags
        composeTestRule.onNodeWithText(addTagLabel)
            .performScrollTo()
            .performClick()

        // Fill dialog
        composeTestRule.onNodeWithText(tagNameLabel).performTextInput(name)
        composeTestRule.onNodeWithText(saveButtonLabel).performClick()

        // Wait for dialog dismissal
        composeTestRule.onNodeWithText(saveButtonLabel).assertDoesNotExist()
    }
}
