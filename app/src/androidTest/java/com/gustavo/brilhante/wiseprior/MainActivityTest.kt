package com.gustavo.brilhante.wiseprior

import android.Manifest
import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.gustavo.brilhante.notifications.EXTRA_TASK_ID
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    // Pre-grant notification permission so the system dialog never appears,
    // which would prevent the activity from reaching DESTROYED during teardown.
    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun mainActivity_launches() {
        // Basic launch test to ensure activity starts correctly
    }

    @Test
    fun testOnNewIntent_updatesPendingTaskId() {
        val intent = Intent().apply {
            putExtra(EXTRA_TASK_ID, 123L)
        }
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onNewIntent(intent)
        }
        // Verification of navigation can be added if WisePriorNavHost is observable
    }
}
