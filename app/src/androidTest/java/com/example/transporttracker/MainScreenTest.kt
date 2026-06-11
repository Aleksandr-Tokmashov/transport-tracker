package com.example.transporttracker

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.transporttracker.ui.home.HomeScreen
import com.example.transporttracker.ui.home.HomeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun start_tracking_button_exists_and_is_clickable() {
        composeTestRule.setContent {
            HomeScreen(
                state = HomeUiState(isTracking = false, tripsCount = 0, needsPermission = false),
                onStartTracking = {},
                onStopTracking = {},
                onRequestPermission = {}
            )
        }

        composeTestRule
            .onNodeWithTag("start_tracking_button")
            .assertExists()

        composeTestRule
            .onNodeWithTag("start_tracking_button")
            .performClick()
    }

    @Test
    fun stop_tracking_button_shown_when_tracking() {
        composeTestRule.setContent {
            HomeScreen(
                state = HomeUiState(isTracking = true, tripsCount = 3, needsPermission = false),
                onStartTracking = {},
                onStopTracking = {},
                onRequestPermission = {}
            )
        }

        composeTestRule
            .onNodeWithTag("stop_tracking_button")
            .assertExists()
    }
}
