package com.gustavo.brilhante.ui

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gustavo.brilhante.model.Tag
import org.junit.Rule
import org.junit.Test

class TagChipTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tagChip_renders() {
        val tag = Tag(id = 1, name = "Urgent", color = 0xFFFF0000)
        composeTestRule.setContent {
            TagChip(tag = tag, isSelected = false)
        }
        composeTestRule.onNodeWithTag(TestTags.CHIP_TAG_ITEM).assertExists()
    }

    @Test
    fun tagChip_triggersOnClick() {
        var clicked = false
        val tag = Tag(id = 1, name = "Urgent", color = 0xFFFF0000)
        composeTestRule.setContent {
            TagChip(tag = tag, isSelected = false, onClick = { clicked = true })
        }
        composeTestRule.onNodeWithTag(TestTags.CHIP_TAG_ITEM).performClick()
        assert(clicked)
    }

    @Test
    fun tagChip_showsSelectedState() {
        val tag = Tag(id = 1, name = "Urgent", color = 0xFFFF0000)
        composeTestRule.setContent {
            TagChip(tag = tag, isSelected = true)
        }
        composeTestRule.onNodeWithTag(TestTags.CHIP_TAG_ITEM).assertIsSelected()
    }
}
