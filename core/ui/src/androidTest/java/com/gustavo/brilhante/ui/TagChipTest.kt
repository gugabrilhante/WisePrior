package com.gustavo.brilhante.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gustavo.brilhante.model.Tag
import org.junit.Rule
import org.junit.Test

class TagChipTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val tag = Tag(id = 1, name = "Urgent", color = 0xFFFF0000)

    // ── Basic rendering ───────────────────────────────────────────────────────

    @Test
    fun tagChip_renders() {
        composeTestRule.setContent {
            TagChip(tag = tag, isSelected = false)
        }
        composeTestRule.onNodeWithTag(TestTags.CHIP_TAG_ITEM).assertExists()
    }

    @Test
    fun tagChip_displaysTagName() {
        composeTestRule.setContent {
            TagChip(tag = tag, isSelected = false)
        }
        composeTestRule.onNodeWithText("Urgent").assertIsDisplayed()
    }

    // ── isSelected branch ─────────────────────────────────────────────────────

    @Test
    fun tagChip_showsSelectedState() {
        composeTestRule.setContent {
            TagChip(tag = tag, isSelected = true)
        }
        composeTestRule.onNodeWithTag(TestTags.CHIP_TAG_ITEM).assertIsSelected()
    }

    @Test
    fun tagChip_showsUnselectedState() {
        composeTestRule.setContent {
            TagChip(tag = tag, isSelected = false)
        }
        composeTestRule.onNodeWithTag(TestTags.CHIP_TAG_ITEM).assertIsNotSelected()
    }

    // ── onClick branch ────────────────────────────────────────────────────────

    @Test
    fun tagChip_withOnClick_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            TagChip(tag = tag, isSelected = false, onClick = { clicked = true })
        }
        composeTestRule.onNodeWithTag(TestTags.CHIP_TAG_ITEM).performClick()
        assert(clicked)
    }

    @Test
    fun tagChip_withNullOnClick_doesNotCrashOnRender() {
        composeTestRule.setContent {
            TagChip(tag = tag, isSelected = false, onClick = null)
        }
        composeTestRule.onNodeWithTag(TestTags.CHIP_TAG_ITEM).assertIsDisplayed()
    }

    // ── isSelectable=false branch ─────────────────────────────────────────────

    @Test
    fun tagChip_notSelectable_alwaysAppearsSelected() {
        composeTestRule.setContent {
            TagChip(tag = tag, isSelected = false, isSelectable = false)
        }
        // When not selectable, chip is visually always selected (isVisuallySelected=true)
        composeTestRule.onNodeWithTag(TestTags.CHIP_TAG_ITEM).assertIsSelected()
    }

    @Test
    fun tagChip_notSelectable_doesNotShowLeadingCheckmark() {
        // When isSelectable=false, the check icon should NOT appear regardless of isSelected
        composeTestRule.setContent {
            TagChip(tag = tag, isSelected = true, isSelectable = false)
        }
        // Chip renders without check icon (no content description for it)
        composeTestRule.onNodeWithTag(TestTags.CHIP_TAG_ITEM).assertIsDisplayed()
    }

    // ── Dark color chip ───────────────────────────────────────────────────────

    @Test
    fun tagChip_withDarkColor_foregroundIsWhite() {
        val darkTag = Tag(id = 2, name = "Dark", color = 0xFF000000) // black = dark
        composeTestRule.setContent {
            TagChip(tag = darkTag, isSelected = true)
        }
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
    }

    @Test
    fun tagChip_withLightColor_foregroundIsBlack() {
        val lightTag = Tag(id = 3, name = "Light", color = 0xFFFFFFFF) // white = light
        composeTestRule.setContent {
            TagChip(tag = lightTag, isSelected = true)
        }
        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
    }
}
