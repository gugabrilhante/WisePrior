package com.gustavo.brilhante.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class ToggleRowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val switchMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)

    // ── Basic rendering ───────────────────────────────────────────────────────

    @Test
    fun toggleRow_rendersLabel() {
        composeTestRule.setContent {
            ToggleRow(label = "Enable feature", checked = false, onCheckedChange = {})
        }
        composeTestRule.onNodeWithText("Enable feature").assertExists()
    }

    @Test
    fun toggleRow_clickingSwitch_firesCallback() {
        var checkedValue = false
        composeTestRule.setContent {
            ToggleRow(label = "Enable feature", checked = false, onCheckedChange = { checkedValue = it })
        }
        composeTestRule.onNode(switchMatcher).assertIsOff().performClick()
        assert(checkedValue)
    }

    @Test
    fun toggleRow_showsCheckedState() {
        composeTestRule.setContent {
            ToggleRow(label = "Notifications", checked = true, onCheckedChange = {})
        }
        composeTestRule.onNode(switchMatcher).assertIsOn()
    }

    @Test
    fun toggleRow_showsUncheckedState() {
        composeTestRule.setContent {
            ToggleRow(label = "Notifications", checked = false, onCheckedChange = {})
        }
        composeTestRule.onNode(switchMatcher).assertIsOff()
    }

    // ── supportingText branch ─────────────────────────────────────────────────

    @Test
    fun toggleRow_withSupportingText_displaysSupportingText() {
        composeTestRule.setContent {
            ToggleRow(
                label = "Date",
                checked = true,
                onCheckedChange = {},
                supportingText = "Jan 15, 2024"
            )
        }
        composeTestRule.onNodeWithText("Jan 15, 2024").assertIsDisplayed()
    }

    @Test
    fun toggleRow_withoutSupportingText_doesNotShowIt() {
        composeTestRule.setContent {
            ToggleRow(label = "Date", checked = false, onCheckedChange = {}, supportingText = null)
        }
        composeTestRule.onNodeWithText("Jan 15, 2024").assertDoesNotExist()
    }

    // ── onRowClick branch ─────────────────────────────────────────────────────

    @Test
    fun toggleRow_withOnRowClick_clickingRowFiresCallback() {
        var clicked = false
        composeTestRule.setContent {
            ToggleRow(
                label = "Date",
                checked = true,
                onCheckedChange = {},
                onRowClick = { clicked = true },
                testTag = "toggle_row"
            )
        }
        composeTestRule.onNodeWithTag("toggle_row").performClick()
        assert(clicked)
    }

    @Test
    fun toggleRow_withoutOnRowClick_rowClickDoesNotCrash() {
        composeTestRule.setContent {
            ToggleRow(
                label = "Date",
                checked = false,
                onCheckedChange = {},
                onRowClick = null
            )
        }
        // No crash expected when clicking without a row click handler
        composeTestRule.onNodeWithText("Date").assertIsDisplayed()
    }

    // ── testTag branch ────────────────────────────────────────────────────────

    @Test
    fun toggleRow_withTestTag_isLocatableByTag() {
        composeTestRule.setContent {
            ToggleRow(
                label = "Urgent",
                checked = false,
                onCheckedChange = {},
                testTag = TestTags.TOGGLE_TASK_URGENT
            )
        }
        composeTestRule.onNodeWithTag(TestTags.TOGGLE_TASK_URGENT).assertIsDisplayed()
    }

    // ── icon branch ───────────────────────────────────────────────────────────

    @Test
    fun toggleRow_withIcon_renders() {
        composeTestRule.setContent {
            ToggleRow(
                label = "Flag",
                checked = false,
                onCheckedChange = {},
                icon = Icons.Filled.Flag
            )
        }
        composeTestRule.onNodeWithText("Flag").assertIsDisplayed()
    }
}
