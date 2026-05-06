package com.gustavo.brilhante.ui

import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class SectionHeaderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sectionHeader_displaysUppercasedTitle() {
        composeTestRule.setContent {
            SectionHeader(title = "details")
        }
        // SectionHeader uppercases the title
        composeTestRule.onNodeWithText("DETAILS").assertIsDisplayed()
    }

    @Test
    fun sectionHeader_withAlreadyUppercaseTitle_displays() {
        composeTestRule.setContent {
            SectionHeader(title = "PRIORITY")
        }
        composeTestRule.onNodeWithText("PRIORITY").assertIsDisplayed()
    }

    @Test
    fun sectionHeader_withTestTag_isLocatableByTag() {
        composeTestRule.setContent {
            SectionHeader(title = "Tags", modifier = androidx.compose.ui.Modifier.testTag(TestTags.SECTION_TASK_EDITOR_TAGS))
        }
        composeTestRule.onNodeWithTag(TestTags.SECTION_TASK_EDITOR_TAGS).assertIsDisplayed()
    }
}
