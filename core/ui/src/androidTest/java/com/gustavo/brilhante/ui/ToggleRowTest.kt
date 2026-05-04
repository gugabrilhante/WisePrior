package com.gustavo.brilhante.ui

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRole
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.Role
import org.junit.Rule
import org.junit.Test

class ToggleRowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun toggleRow_rendersLabelAndToggles() {
        var checkedValue = false
        composeTestRule.setContent {
            ToggleRow(
                label = "Enable feature",
                checked = false,
                onCheckedChange = { checkedValue = it }
            )
        }
        
        composeTestRule.onNodeWithText("Enable feature").assertExists()
        composeTestRule.onRole(Role.Switch).assertIsOff().performClick()
        
        assert(checkedValue)
    }

    @Test
    fun toggleRow_showsCheckedState() {
        composeTestRule.setContent {
            ToggleRow(
                label = "Notifications",
                checked = true,
                onCheckedChange = {}
            )
        }
        composeTestRule.onRole(Role.Switch).assertIsOn()
    }
}
